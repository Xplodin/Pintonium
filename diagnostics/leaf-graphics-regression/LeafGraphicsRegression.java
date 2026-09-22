import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

/** Copies selected real method bytecode into boot-free collaborator shells. */
public class LeafGraphicsRegression {
    private static final String MC_BLOCK = "net/minecraft/block/Block";
    private static final String MC_LEAF = "net/minecraft/block/BlockLeaves";
    private static final String SETTINGS = "net/minecraft/client/settings/GameSettings";
    private static final String OPTIONS = "org/taumc/celeritas/impl/gui/SodiumGameOptions";
    private static final String MIXIN = "org/taumc/celeritas/mixin/features/options/MixinRenderGlobal";

    public static void main(String[] args) throws Exception {
        Path classes = Path.of(args[0]), dev = Path.of(args[2]);
        Map<String, String> names = new HashMap<>();
        names.put(MC_BLOCK, "LeafGraphicsCases$Block");
        names.put(MC_LEAF, "LeafGraphicsCases$Leaf");
        names.put(MC_BLOCK + "$1", "LeafGraphicsCases$Switches");
        names.put("net/minecraft/block/state/IBlockState", "LeafGraphicsCases$State");
        names.put("net/minecraft/world/IBlockAccess", "LeafGraphicsCases$World");
        names.put("net/minecraft/util/math/BlockPos", "LeafGraphicsCases$Pos");
        names.put("net/minecraft/util/math/AxisAlignedBB", "LeafGraphicsCases$Box");
        names.put("net/minecraft/util/EnumFacing", "LeafGraphicsCases$Facing");
        names.put("net/minecraft/util/BlockRenderLayer", "LeafGraphicsCases$Layer");
        names.put(SETTINGS, "LeafGraphicsCases$Settings");
        names.put(OPTIONS, "LeafGraphicsCases$Options");
        names.put(OPTIONS + "$QualitySettings", "LeafGraphicsCases$QualitySettings");
        names.put(OPTIONS + "$GraphicsQuality", "LeafGraphicsCases$Quality");
        names.put("org/taumc/celeritas/CeleritasVintage", "LeafGraphicsCases$OptionsHolder");
        names.put(MIXIN, "LeafGraphicsCases$Mixin");
        Remapper remapper = new SimpleRemapper(names);
        Map<String, byte[]> patched = new HashMap<>();
        try (JarFile minecraft = new JarFile(args[1]); JarFile production = new JarFile(args[3])) {
            ClassNode renderGlobal = read(minecraft, "net/minecraft/client/renderer/RenderGlobal");
            MethodNode reload = method(renderGlobal, "loadRenderers", "()V");
            int matches = 0, updates = 0;
            for (AbstractInsnNode insn : reload.instructions) {
                if (insn instanceof FieldInsnNode f && f.getOpcode() == Opcodes.GETFIELD &&
                        f.owner.equals(SETTINGS) && f.name.equals("fancyGraphics") && f.desc.equals("Z")) matches++;
                if (insn instanceof MethodInsnNode m && m.owner.equals(MC_LEAF) && m.name.equals("setGraphicsLevel")) updates++;
            }
            require(matches == 2 && updates == 2, "Real loadRenderers must update both vanilla leaf blocks from exactly two field reads");
            ClassNode devMixin = read(Files.readAllBytes(dev.resolve(MIXIN + ".class")));
            verifyRedirect(devMixin, "loadRenderers", "L" + SETTINGS + ";fancyGraphics:Z");
            ClassNode packedMixin = read(production, MIXIN);
            verifyRedirect(packedMixin, "Lnet/minecraft/client/renderer/RenderGlobal;func_72712_a()V", "L" + SETTINGS + ";field_74347_j:Z");
            var config = production.getJarEntry("mixins.celeritas.json");
            require(config != null && new String(production.getInputStream(config).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                    .contains("org.taumc.celeritas.mixin.CeleritasVintageMixinPlugin"), "Production configuration must use the dynamic mixin discovery plugin");
            require(packedMixin.invisibleAnnotations.stream().anyMatch(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")), "Packed class must retain its discovery annotation");
            System.out.println("PASS: compiled development redirect, production SRG mapping/discovery annotation, and both vanilla reload reads match.");
            copy(classes, read(minecraft, MC_BLOCK), "Block", Set.of("isOpaqueCube", "doesSideBlockRendering", "shouldSideBeRendered"), remapper, patched);
            copy(classes, read(minecraft, MC_LEAF), "Leaf", Set.of("setGraphicsLevel", "isOpaqueCube", "getRenderLayer"), remapper, patched);
            ClassNode quality = read(Files.readAllBytes(dev.resolve(OPTIONS + "$GraphicsQuality.class")));
            quality.methods.removeIf(m -> !m.name.equals("isFancy") || !m.desc.equals("(Z)Z"));
            copy(classes, quality, "Quality", Set.of("isFancy"), remapper, patched);
            copy(classes, devMixin, "Mixin", Set.of("redirectGetFancyLeaves"), remapper, patched);
        }
        ClassLoader loader = new ClassLoader(LeafGraphicsRegression.class.getClassLoader()) {
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.startsWith("LeafGraphicsCases")) return super.loadClass(name, resolve);
                Class<?> found = findLoadedClass(name);
                if (found == null) {
                    try {
                        byte[] bytes = patched.get(name);
                        if (bytes == null) bytes = Files.readAllBytes(classes.resolve(name + ".class"));
                        found = defineClass(name, bytes, 0, bytes.length);
                    } catch (Exception e) { throw new ClassNotFoundException(name, e); }
                }
                if (resolve) resolveClass(found);
                return found;
            }
        };
        loader.loadClass("LeafGraphicsCases").getMethod("run").invoke(null);
    }

    private static void copy(Path classes, ClassNode source, String suffix, Set<String> selected,
                             Remapper remapper, Map<String, byte[]> output) throws Exception {
        String target = "LeafGraphicsCases$" + suffix;
        ClassNode shell = read(Files.readAllBytes(classes.resolve(target + ".class")));
        int copied = 0;
        for (MethodNode original : source.methods) {
            if (!selected.contains(original.name)) continue;
            String desc = remapper.mapMethodDesc(original.desc);
            require(shell.methods.removeIf(m -> m.name.equals(original.name) && m.desc.equals(desc)), "Missing shell for " + original.name + desc);
            MethodNode replacement = new MethodNode(Opcodes.ACC_PUBLIC, original.name, desc, null, null);
            original.accept(new MethodRemapper(replacement, remapper));
            replacement.visibleAnnotations = replacement.invisibleAnnotations = null;
            shell.methods.add(replacement);
            copied++;
        }
        require(copied == selected.size(), "Did not extract all selected methods from " + source.name);
        ClassWriter writer = new ClassWriter(0);
        shell.accept(writer);
        output.put(target, writer.toByteArray());
    }

    private static void verifyRedirect(ClassNode mixin, String selector, String field) {
        MethodNode redirect = method(mixin, "redirectGetFancyLeaves", "(L" + SETTINGS + ";)Z");
        AnnotationNode annotation = redirect.visibleAnnotations.stream()
                .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/injection/Redirect;"))
                .findFirst().orElseThrow();
        require(((List<?>) value(annotation, "method")).equals(List.of(selector)), "Unexpected reload method selector");
        AnnotationNode at = (AnnotationNode) value(annotation, "at");
        require(value(at, "value").equals("FIELD") && value(at, "target").equals(field) &&
                value(at, "opcode").equals(Opcodes.GETFIELD), "Unexpected field redirect target");
    }
    private static Object value(AnnotationNode annotation, String key) {
        for (int i = 0; i < annotation.values.size(); i += 2)
            if (annotation.values.get(i).equals(key)) return annotation.values.get(i + 1);
        throw new AssertionError("Missing annotation field " + key);
    }
    private static MethodNode method(ClassNode owner, String name, String desc) {
        return owner.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findFirst().orElseThrow();
    }
    private static ClassNode read(JarFile jar, String name) throws Exception {
        try (var stream = jar.getInputStream(jar.getJarEntry(name + ".class"))) { return read(stream.readAllBytes()); }
    }
    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
