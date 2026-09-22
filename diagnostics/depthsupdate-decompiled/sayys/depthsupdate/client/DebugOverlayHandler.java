package sayys.depthsupdate.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@EventBusSubscriber(value = Side.CLIENT, modid = "depthsupdate")
public final class DebugOverlayHandler {
   private static final String OUTSIDE = "Outside of world...";

   private DebugOverlayHandler() {
   }

   @SubscribeEvent
   public static void onDebugText(Text event) {
      List<String> left = event.getLeft();
      int index = left.indexOf("Outside of world...");
      if (index >= 0) {
         Minecraft mc = Minecraft.func_71410_x();
         World world = mc.field_71441_e;
         Entity view = mc.func_175606_aa();
         if (world != null && view != null && HeightManager.isExtended(world)) {
            BlockPos pos = new BlockPos(view.field_70165_t, view.func_174813_aQ().field_72338_b, view.field_70161_v);
            HeightContext ctx = HeightManager.get(world);
            if (pos.func_177956_o() >= ctx.minY() && pos.func_177956_o() < ctx.maxY() && world.func_175667_e(pos)) {
               Chunk chunk = world.func_175726_f(pos);
               if (!chunk.func_76621_g()) {
                  left.set(index, "Biome: " + chunk.func_177411_a(pos, world.func_72959_q()).func_185359_l());
                  left.add(
                     index + 1,
                     "Light: "
                        + chunk.func_177443_a(pos, 0)
                        + " ("
                        + chunk.func_177413_a(EnumSkyBlock.SKY, pos)
                        + " sky, "
                        + chunk.func_177413_a(EnumSkyBlock.BLOCK, pos)
                        + " block)"
                  );
                  DifficultyInstance difficulty = world.func_175649_E(pos);
                  if (mc.func_71387_A() && mc.func_71401_C() != null && mc.field_71439_g != null) {
                     EntityPlayerMP serverPlayer = mc.func_71401_C().func_184103_al().func_177451_a(mc.field_71439_g.func_110124_au());
                     if (serverPlayer != null) {
                        difficulty = serverPlayer.field_70170_p.func_175649_E(new BlockPos(serverPlayer));
                     }
                  }

                  left.add(
                     index + 2,
                     String.format(
                        "Local Difficulty: %.2f // %.2f (Day %d)", difficulty.func_180168_b(), difficulty.func_180170_c(), world.func_72820_D() / 24000L
                     )
                  );
               }
            }
         }
      }
   }
}
