package sayys.depthsupdate.mixin.mod.rltweaker;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(remap = false, targets = "com.charles445.rltweaker.hook.HookWorld")
public class MixinRLTweakerHookWorld {
   @Inject(at = @At("HEAD"), cancellable = true, remap = false, method = "getCollisionBoxes")
   private static void depthsupdate$extendedCollisionBoxes(
      World world, Entity entity, AxisAlignedBB aabb, boolean stopOnFirst, List<AxisAlignedBB> list, CallbackInfoReturnable<Boolean> cir
   ) {
      if (HeightManager.isExtended(world)) {
         if (MathHelper.func_76128_c(aabb.field_72338_b) - 1 < 0 || MathHelper.func_76128_c(aabb.field_72337_e) + 1 > 255) {
            cir.setReturnValue(depthsupdate$collect(world, entity, aabb, stopOnFirst, list));
         }
      }
   }

   private static boolean depthsupdate$collect(World world, Entity entity, AxisAlignedBB aabb, boolean stopOnFirst, List<AxisAlignedBB> list) {
      if (stopOnFirst) {
         if (aabb.field_72340_a < -3.0E7 || aabb.field_72336_d > 3.0E7 || aabb.field_72339_c < -3.0E7 || aabb.field_72334_f > 3.0E7) {
            return true;
         }

         MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(world, entity, aabb, list));
         if (!list.isEmpty()) {
            return true;
         }
      } else if (entity != null && entity.func_174832_aS() == world.func_191503_g(entity)) {
         entity.func_174821_h(!entity.func_174832_aS());
      }

      HeightContext ctx = HeightManager.get(world);
      int minX = MathHelper.func_76128_c(aabb.field_72340_a) - 1;
      int maxX = MathHelper.func_76128_c(aabb.field_72336_d) + 1;
      int minZ = MathHelper.func_76128_c(aabb.field_72339_c) - 1;
      int maxZ = MathHelper.func_76128_c(aabb.field_72334_f) + 1;
      int minY = Math.max(MathHelper.func_76128_c(aabb.field_72338_b) - 1, ctx.minY());
      int maxY = Math.min(MathHelper.func_76128_c(aabb.field_72337_e) + 1, ctx.maxY() - 1);
      if (minY > maxY) {
         return !list.isEmpty();
      } else {
         WorldBorder border = world.func_175723_af();
         boolean checkBorder = !stopOnFirst
            && entity != null
            && !entity.func_174832_aS()
            && (minX < border.func_177726_b() || maxX + 1 > border.func_177728_d() || minZ < border.func_177736_c() || maxZ + 1 > border.func_177733_e());
         MutableBlockPos pos = new MutableBlockPos();

         for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            int x0 = Math.max(minX, chunkX << 4);
            int x1 = Math.min(maxX, chunkX << 4 | 15);

            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
               Chunk chunk = world.func_72863_F().func_186026_b(chunkX, chunkZ);
               if (chunk != null) {
                  int z0 = Math.max(minZ, chunkZ << 4);
                  int z1 = Math.min(maxZ, chunkZ << 4 | 15);
                  ExtendedBlockStorage[] sections = chunk.func_76587_i();

                  for (int chunkY = minY >> 4; chunkY <= maxY >> 4; chunkY++) {
                     int index = ctx.toStorageIndex(chunkY << 4);
                     if (index >= 0 && index < sections.length) {
                        ExtendedBlockStorage section = sections[index];
                        if (section != Chunk.field_186036_a) {
                           int y0 = Math.max(minY, chunkY << 4);
                           int y1 = Math.min(maxY, chunkY << 4 | 15);

                           for (int x = x0; x <= x1; x++) {
                              boolean xBorder = x == minX || x == maxX;

                              for (int z = z0; z <= z1; z++) {
                                 boolean zBorder = z == minZ || z == maxZ;
                                 if (!xBorder || !zBorder) {
                                    for (int y = y0; y <= y1; y++) {
                                       if (!xBorder && !zBorder || y != minY && y != maxY) {
                                          pos.func_181079_c(x, y, z);
                                          IBlockState state = checkBorder && !border.func_177746_a(pos)
                                             ? Blocks.field_150348_b.func_176223_P()
                                             : section.func_177485_a(x & 15, y & 15, z & 15);
                                          state.func_185908_a(world, pos, aabb, list, entity, false);
                                          if (stopOnFirst && !list.isEmpty()) {
                                             return true;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return !list.isEmpty();
      }
   }
}
