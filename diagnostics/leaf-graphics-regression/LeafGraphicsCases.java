/** Small collaborators for running extracted game methods without Forge/OpenGL boot. */
public class LeafGraphicsCases {
    public enum Facing {
        DOWN, UP, NORTH, SOUTH, WEST, EAST;
        public Facing getOpposite() { return values()[ordinal() ^ 1]; }
    }
    public static class Switches {
        public static final int[] $SwitchMap$net$minecraft$util$EnumFacing = {1, 2, 3, 4, 5, 6};
    }
    public record Pos(int x, int y, int z) {
        public Pos offset(Facing face) {
            return switch (face) {
                case DOWN -> new Pos(x, y - 1, z);
                case UP -> new Pos(x, y + 1, z);
                case NORTH -> new Pos(x, y, z - 1);
                case SOUTH -> new Pos(x, y, z + 1);
                case WEST -> new Pos(x - 1, y, z);
                case EAST -> new Pos(x + 1, y, z);
            };
        }
    }
    public static class Box {
        public double minX = 0, minY = 0, minZ = 0, maxX = 1, maxY = 1, maxZ = 1;
    }
    public interface World { State getBlockState(Pos pos); }
    public interface State {
        Box getBoundingBox(World world, Pos pos);
        boolean isOpaqueCube();
        boolean doesSideBlockRendering(World world, Pos pos, Facing face);
    }
    public record BlockState(Block block) implements State {
        public Box getBoundingBox(World world, Pos pos) { return new Box(); }
        public boolean isOpaqueCube() { return block.isOpaqueCube(this); }
        public boolean doesSideBlockRendering(World world, Pos pos, Facing face) {
            return block.doesSideBlockRendering(this, world, pos, face);
        }
    }
    public static class Block {
        public boolean isOpaqueCube(State state) { throw unpatched(); }
        public boolean doesSideBlockRendering(State state, World world, Pos pos, Facing face) { throw unpatched(); }
        public boolean shouldSideBeRendered(State state, World world, Pos pos, Facing face) { throw unpatched(); }
    }
    public enum Layer { SOLID, CUTOUT_MIPPED }
    public static class Leaf extends Block {
        public boolean leavesFancy;
        public void setGraphicsLevel(boolean fancy) { throw unpatched(); }
        public boolean isOpaqueCube(State state) { throw unpatched(); }
        public Layer getRenderLayer() { throw unpatched(); }
    }
    public enum Quality {
        DEFAULT, FANCY, FAST;
        public boolean isFancy(boolean globalFancy) { throw unpatched(); }
    }
    public static class Settings { public boolean fancyGraphics; }
    public static class QualitySettings { public Quality leavesQuality; }
    public static class Options { public QualitySettings quality = new QualitySettings(); }
    public static class OptionsHolder {
        public static final Options OPTIONS = new Options();
        public static Options options() { return OPTIONS; }
    }
    public static class Mixin {
        public boolean redirectGetFancyLeaves(Settings settings) { throw unpatched(); }
    }
    private static AssertionError unpatched() { return new AssertionError("Real compiled method was not installed"); }

    public static void run() {
        int beforeFailures = 0, afterFailures = 0, faceChecks = 0;
        // Explicit independent oracle: DEFAULT follows global; FANCY/FAST override it.
        boolean[][] expectedFancy = {{false, true, false}, {true, true, false}};
        Mixin mixin = new Mixin();
        Block ground = new Block();
        BlockState groundState = new BlockState(ground);
        Pos groundPos = new Pos(0, 0, 0);
        Leaf[] vanillaLeaves = {new Leaf(), new Leaf()}; // LEAVES and LEAVES2 use these inherited methods.
        for (boolean globalFancy : new boolean[]{false, true}) {
            for (Quality quality : Quality.values()) {
                Settings settings = new Settings();
                settings.fancyGraphics = globalFancy;
                OptionsHolder.OPTIONS.quality.leavesQuality = quality;
                boolean expected = expectedFancy[globalFancy ? 1 : 0][quality.ordinal()];
                int before = 0, after = 0;
                for (Leaf leaf : vanillaLeaves) {
                    BlockState leafState = new BlockState(leaf);
                    leaf.setGraphicsLevel(globalFancy);
                    if (leafState.isOpaqueCube() != !expected) before++;
                    leaf.setGraphicsLevel(mixin.redirectGetFancyLeaves(settings));
                    if (leafState.isOpaqueCube() != !expected) after++;
                    if (leaf.getRenderLayer() != (expected ? Layer.CUTOUT_MIPPED : Layer.SOLID)) after++;
                    for (Facing face : Facing.values()) {
                        Pos leafPos = groundPos.offset(face);
                        World world = pos -> {
                            if (!pos.equals(leafPos)) throw new AssertionError("Unexpected neighbor query " + pos);
                            return leafState;
                        };
                        // Real Forge Block.shouldSideBeRendered -> doesSideBlockRendering -> real leaf opacity.
                        if (ground.shouldSideBeRendered(groundState, world, groundPos, face) != expected) after++;
                        faceChecks++;
                    }
                }
                beforeFailures += before;
                afterFailures += after;
                System.out.printf("global=%-5s leaves=%-7s expected=%-14s before=%s after=%s%n",
                        globalFancy ? "Fancy" : "Fast", quality,
                        expected ? "transparent" : "opaque",
                        before == 0 ? "PASS" : "FAIL (opacity mismatch)", after == 0 ? "PASS" : "FAIL");
            }
        }
        if (beforeFailures != 4) throw new AssertionError("Expected the two conflicting option combinations to fail for both vanilla leaf blocks, got " + beforeFailures);
        if (afterFailures != 0) throw new AssertionError("Patched behavior failed " + afterFailures + " checks");
        System.out.println("PASS: baseline reproduced both option conflicts; patched 6 combinations / 2 leaf blocks / " + faceChecks + " neighbor faces.");
    }
}
