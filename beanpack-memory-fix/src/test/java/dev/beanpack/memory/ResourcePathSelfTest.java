package dev.beanpack.memory;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Executes the actual Minecraft method before/after rewriting in isolated classes. */
public final class ResourcePathSelfTest implements Opcodes {
    public static final class Location {
        final String namespace, path;
        public Location(String namespace, String path) { this.namespace = namespace; this.path = path; }
        public String namespace() { return namespace; }
        public String path() { return path; }
    }
    private static final class Loader extends ClassLoader {
        Loader() { super(ResourcePathSelfTest.class.getClassLoader()); }
        Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
    }
    public static void main(String[] args) throws Exception {
        byte[] original;
        try (ZipFile zip = new ZipFile(args[0]);
                InputStream stream = zip.getInputStream(zip.getEntry("net/minecraft/client/resources/AbstractResourcePack.class"))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int n; (n = stream.read(buffer)) >= 0;) out.write(buffer, 0, n);
            original = out.toByteArray();
        }
        byte[] patched = ResourcePathPatcher.patch(original);
        require(patched != original, "Actual Minecraft class must be patched");
        require(ResourcePathPatcher.patch(patched) == patched, "Patch must be idempotent");
        ClassNode before = parse(original), after = parse(patched);
        require(before.methods.size() == after.methods.size(), "Class schema changed");
        MethodNode formatter = null;
        int changed = 0;
        for (int i = 0; i < before.methods.size(); i++) {
            MethodNode a = before.methods.get(i), b = after.methods.get(i);
            if (!Arrays.equals(standalone(a), standalone(b))) {
                changed++;
                formatter = a;
                require(countCalls(a, "java/lang/String", "format") == 1, "Wrong source method");
                require(countCalls(b, "java/lang/String", "format") == 0, "Formatter remains");
            }
        }
        require(changed == 1, "Exactly one existing method must change");

        MethodNode fixture = new MethodNode(ASM9, formatter.access, formatter.name, formatter.desc, null, null);
        formatter.accept(fixture);
        String sourceOwner = Type.getArgumentTypes(fixture.desc)[0].getInternalName();
        String fixtureOwner = Type.getInternalName(Location.class);
        fixture.access = ACC_PUBLIC | ACC_STATIC;
        fixture.name = "path";
        fixture.desc = "(L" + fixtureOwner + ";)Ljava/lang/String;";
        fixture.localVariables = null;
        int getter = 0;
        for (AbstractInsnNode insn : fixture.instructions) {
            if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).owner.equals(sourceOwner)) {
                MethodInsnNode call = (MethodInsnNode) insn;
                call.owner = fixtureOwner;
                call.name = getter++ == 0 ? "namespace" : "path";
            }
        }
        require(getter == 2, "Expected two resource-location getters");
        byte[] fixtureBytes = standalone(fixture);
        byte[] patchedFixture = ResourcePathPatcher.patch(fixtureBytes);
        require(patchedFixture != fixtureBytes, "Fixture not patched");
        Method oldMethod = new Loader().define(fixtureBytes).getMethod("path", Location.class);
        Method newMethod = new Loader().define(patchedFixture).getMethod("path", Location.class);
        List<Location> locations = new ArrayList<>();
        String[] special = {"minecraft", "hbm", "textures/blocks/stone.png", "", "a/b/c", "%s/$/\\", "I\u0130\u0131", "\u65e5\u672c\u8a9e", "\ud83c\udf0e", null};
        for (String namespace : special) for (String path : special) locations.add(new Location(namespace, path));
        Random random = new Random(12345);
        for (int i = 0; i < 5000; i++) locations.add(new Location(randomString(random), randomString(random)));
        Locale saved = Locale.getDefault(Locale.Category.FORMAT);
        int comparisons = 0;
        try {
            for (Locale locale : new Locale[]{Locale.ROOT, Locale.US, new Locale("tr", "TR")}) {
                Locale.setDefault(Locale.Category.FORMAT, locale);
                for (Location location : locations) {
                    Object expected = oldMethod.invoke(null, location);
                    require(expected.equals(newMethod.invoke(null, location)), "Path behavior changed");
                    comparisons++;
                }
            }
        } finally { Locale.setDefault(Locale.Category.FORMAT, saved); }
        require(nullFailure(oldMethod) == NullPointerException.class && nullFailure(newMethod) == NullPointerException.class,
                "Null argument behavior changed");

        ClassNode altered = parse(original);
        for (MethodNode method : altered.methods) for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof LdcInsnNode && "assets".equals(((LdcInsnNode) insn).cst)) ((LdcInsnNode) insn).cst = "data";
        }
        byte[] alteredBytes = write(altered);
        require(ResourcePathPatcher.patch(alteredBytes) == alteredBytes, "Unknown semantics must stay untouched");
        ClassNode extra = parse(fixtureBytes);
        extra.methods.get(0).instructions.insert(new InsnNode(NOP));
        byte[] extraBytes = write(extra);
        require(ResourcePathPatcher.patch(extraBytes) == extraBytes, "Extra instructions must stay untouched");
        ResourcePathTransformer transformer = new ResourcePathTransformer();
        require(transformer.transform("unrelated", "unrelated", original) == original, "Unrelated classes changed");
        require(transformer.transform("x", "net.minecraft.client.resources.AbstractResourcePack", original) != original,
                "Coremod dispatch failed");
        require(transformer.transform("x", "x", null) == null, "Null class handling");
        System.out.println("PASS: real Minecraft bytecode; one method changed; " + comparisons
                + " matching paths; null handling; locale independence; idempotence; conservative matching; coremod dispatch.");
    }
    private static String randomString(Random random) {
        StringBuilder value = new StringBuilder();
        for (int i = random.nextInt(100); i > 0; i--) value.append((char) random.nextInt(65536));
        return value.toString();
    }
    private static Class<?> nullFailure(Method method) throws Exception {
        try { method.invoke(null, new Object[]{null}); return null; }
        catch (InvocationTargetException exception) { return exception.getCause().getClass(); }
    }
    private static int countCalls(MethodNode method, String owner, String name) {
        int count = 0;
        for (AbstractInsnNode insn : method.instructions) if (insn instanceof MethodInsnNode) {
            MethodInsnNode call = (MethodInsnNode) insn;
            if (call.owner.equals(owner) && call.name.equals(name)) count++;
        }
        return count;
    }
    private static ClassNode parse(byte[] bytes) {
        ClassNode node = new ClassNode(ASM9);
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
    private static byte[] write(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
    private static byte[] standalone(MethodNode method) {
        ClassNode node = new ClassNode(ASM9);
        node.version = V1_8;
        node.access = ACC_PUBLIC;
        node.name = "dev/beanpack/memory/ExtractedVanillaMethod";
        node.superName = "java/lang/Object";
        node.methods.add(method);
        return write(node);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
