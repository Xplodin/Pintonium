package com.bean.beanutils.mixin;

import java.util.List;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "com.sosnitzka.taiga.util.Generator", remap = false)
public abstract class MixinTaigaDescendingGeneration {
    /**
     * @author BeanUtils contributors
     * @reason TAIGA's two-argument setBlockState sends neighbor notifications during
     * world generation, which can generate adjacent chunks recursively. Preserve
     * the installed TAIGA descending search and RNG calls, changing only placement flags.
     */
    @Overwrite(remap = false)
    public static void generateOreDescending(List<IBlockState> replaceBlockList,
                                            IBlockState replacementBlock, Random random,
                                            int chunkX, int chunkZ, World world,
                                            int count, int minY, int maxY) {
        for (int i = 0; i < count; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            BlockPos cPos = new BlockPos(posX, maxY, posZ);
            if (!replaceBlockList.contains(world.getBlockState(cPos))
                    || !replaceBlockList.contains(world.getBlockState(cPos.up()))) {
                if (replaceBlockList.contains(world.getBlockState(cPos))
                        && !replaceBlockList.contains(world.getBlockState(cPos.up()))) {
                    world.setBlockState(cPos, replacementBlock,
                            Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
                }

                while (!replaceBlockList.contains(world.getBlockState(cPos.down())) && cPos.getY() > minY) {
                    cPos = cPos.down();
                }

                if (replaceBlockList.contains(world.getBlockState(cPos.down()))) {
                    world.setBlockState(cPos.down(), replacementBlock,
                            Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS);
                }
            }
        }
    }
}
