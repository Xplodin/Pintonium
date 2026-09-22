package sayys.depthsupdate.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

public class ScanCommand extends CommandBase {
   private static final int DEFAULT_RADIUS = 5;
   private static final int MAX_RADIUS = 16;
   private static final int EXAMPLES_PER_ISSUE = 5;

   public String func_71517_b() {
      return "depthsupdate";
   }

   public List<String> func_71514_a() {
      return Arrays.asList("du");
   }

   public String func_71518_a(ICommandSender sender) {
      return "/depthsupdate scan [radius in chunks, 0-16]";
   }

   public int func_82362_a() {
      return 2;
   }

   public List<String> func_184883_a(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
      return args.length == 1 ? func_71530_a(args, new String[]{"scan"}) : Collections.emptyList();
   }

   public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length != 0 && "scan".equalsIgnoreCase(args[0])) {
         int radius = args.length > 1 ? func_175764_a(args[1], 0, 16) : 5;
         World world = sender.func_130014_f_();
         HeightContext ctx = HeightManager.get(world);
         BlockPos origin = sender.func_180425_c();
         int minY = ctx.minY();
         int maxY = Math.min(ctx.seaLevel(), ctx.maxY() - 1);
         int originChunkX = origin.func_177958_n() >> 4;
         int originChunkZ = origin.func_177952_p() >> 4;
         ScanCommand.Report report = new ScanCommand.Report();
         long started = System.nanoTime();
         int chunksScanned = 0;

         for (int cx = originChunkX - radius; cx <= originChunkX + radius; cx++) {
            for (int cz = originChunkZ - radius; cz <= originChunkZ + radius; cz++) {
               if (world.func_175667_e(new BlockPos((cx << 4) + 8, minY, (cz << 4) + 8))) {
                  this.scanChunk(world, world.func_72964_e(cx, cz), cx, cz, minY, maxY, report);
                  chunksScanned++;
               }
            }
         }

         long elapsedMs = (System.nanoTime() - started) / 1000000L;
         long blocks = chunksScanned * 256L * (maxY - minY + 1);
         sender.func_145747_a(
            new TextComponentString(
               String.format(
                  "%sDepths Update scan%s: %d chunks, Y %d..%d (%,d blocks) in %d ms",
                  TextFormatting.AQUA,
                  TextFormatting.RESET,
                  chunksScanned,
                  minY,
                  maxY,
                  blocks,
                  elapsedMs
               )
            )
         );
         if (chunksScanned == 0) {
            sender.func_145747_a(new TextComponentString(TextFormatting.YELLOW + "  no loaded chunks in range"));
         } else {
            for (ScanCommand.Issue issue : ScanCommand.Issue.values()) {
               int count = report.count(issue);
               TextFormatting colour = count == 0 ? TextFormatting.GREEN : TextFormatting.RED;
               StringBuilder line = new StringBuilder(String.format("  %s%-18s %5d%s", colour, issue.label, count, TextFormatting.RESET));
               if (count == 0) {
                  line.append(TextFormatting.DARK_GRAY).append("  (").append(issue.detail).append(')');
               } else {
                  line.append(TextFormatting.GRAY).append("  e.g. ");
                  List<BlockPos> examples = report.examples(issue);

                  for (int i = 0; i < examples.size(); i++) {
                     BlockPos pos = examples.get(i);
                     if (i > 0) {
                        line.append(", ");
                     }

                     line.append(pos.func_177958_n()).append(' ').append(pos.func_177956_o()).append(' ').append(pos.func_177952_p());
                  }

                  if (count > examples.size()) {
                     line.append(String.format(" (+%d more)", count - examples.size()));
                  }
               }

               sender.func_145747_a(new TextComponentString(line.toString()));
            }
         }
      } else {
         throw new WrongUsageException(this.func_71518_a(sender), new Object[0]);
      }
   }

   private void scanChunk(World world, Chunk chunk, int chunkX, int chunkZ, int minY, int maxY, ScanCommand.Report report) {
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;
      int bedrockCeiling = minY + 4;
      int fluidCeiling = HeightManager.get(world).seaLevel() - 2;
      MutableBlockPos pos = new MutableBlockPos();

      for (int dx = 0; dx < 16; dx++) {
         for (int dz = 0; dz < 16; dz++) {
            int x = baseX + dx;
            int z = baseZ + dz;

            for (int y = minY; y <= maxY; y++) {
               pos.func_181079_c(x, y, z);
               IBlockState state = chunk.func_177435_g(pos);
               if (state.func_177230_c() == Blocks.field_150357_h) {
                  if (y > bedrockCeiling) {
                     report.record(ScanCommand.Issue.STRAY_BEDROCK, x, y, z);
                  }
               } else {
                  if (y <= fluidCeiling && state.func_177230_c() instanceof BlockLiquid && (Integer)state.func_177229_b(BlockLiquid.field_176367_b) == 0) {
                     this.checkFluidSupport(world, pos, x, y, z, minY, report);
                  }

                  this.checkLight(world, chunk, pos, state, x, y, z, minY, maxY, report);
               }
            }
         }
      }
   }

   private void checkFluidSupport(World world, MutableBlockPos pos, int x, int y, int z, int minY, ScanCommand.Report report) {
      if (y - 1 >= minY) {
         pos.func_181079_c(x, y - 1, z);
         if (world.func_175623_d(pos)) {
            report.record(ScanCommand.Issue.UNSUPPORTED_FLUID, x, y, z);
            pos.func_181079_c(x, y, z);
            return;
         }
      }

      for (EnumFacing facing : EnumFacing.field_176754_o) {
         pos.func_181079_c(x + facing.func_82601_c(), y, z + facing.func_82599_e());
         if (world.func_175667_e(pos) && world.func_175623_d(pos)) {
            report.record(ScanCommand.Issue.EXPOSED_FLUID, x, y, z);
            break;
         }
      }

      pos.func_181079_c(x, y, z);
   }

   private void checkLight(World world, Chunk chunk, MutableBlockPos pos, IBlockState state, int x, int y, int z, int minY, int maxY, ScanCommand.Report report) {
      int light = chunk.func_177413_a(EnumSkyBlock.BLOCK, pos);
      if (light > 0 && state.getLightValue(world, pos) < light) {
         int brightestNeighbour = 0;

         for (EnumFacing facing : EnumFacing.field_82609_l) {
            int ny = y + facing.func_96559_d();
            if (ny < minY || ny > maxY) {
               return;
            }

            pos.func_181079_c(x + facing.func_82601_c(), ny, z + facing.func_82599_e());
            if (!world.func_175667_e(pos)) {
               pos.func_181079_c(x, y, z);
               return;
            }

            brightestNeighbour = Math.max(brightestNeighbour, world.func_175642_b(EnumSkyBlock.BLOCK, pos));
         }

         pos.func_181079_c(x, y, z);
         if (light >= brightestNeighbour) {
            report.record(ScanCommand.Issue.ORPHAN_LIGHT, x, y, z);
         }
      }
   }

   private static enum Issue {
      UNSUPPORTED_FLUID("unsupported fluid", "source block with air directly below"),
      EXPOSED_FLUID("exposed fluid", "source block with air horizontally adjacent"),
      STRAY_BEDROCK("stray bedrock", "bedrock above the world floor"),
      ORPHAN_LIGHT("orphan light", "block light exceeding every neighbour");

      private final String label;
      private final String detail;

      private Issue(String label, String detail) {
         this.label = label;
         this.detail = detail;
      }
   }

   private static final class Report {
      private final Map<ScanCommand.Issue, Integer> counts = new EnumMap<>(ScanCommand.Issue.class);
      private final Map<ScanCommand.Issue, List<BlockPos>> examples = new EnumMap<>(ScanCommand.Issue.class);

      void record(ScanCommand.Issue issue, int x, int y, int z) {
         this.counts.merge(issue, 1, Integer::sum);
         List<BlockPos> found = this.examples.computeIfAbsent(issue, key -> new ArrayList<>());
         if (found.size() < 5) {
            found.add(new BlockPos(x, y, z));
         }
      }

      int count(ScanCommand.Issue issue) {
         return this.counts.getOrDefault(issue, 0);
      }

      List<BlockPos> examples(ScanCommand.Issue issue) {
         return this.examples.getOrDefault(issue, Collections.emptyList());
      }
   }
}
