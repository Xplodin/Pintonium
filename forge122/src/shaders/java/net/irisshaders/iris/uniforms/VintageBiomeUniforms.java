package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.parsing.BiomeCategories;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class VintageBiomeUniforms implements BiomeUniforms {
    @Override
    public void addBiomeUniforms(UniformHolder uniforms) {
        uniforms
                .uniform1i(PER_TICK, "biome", playerI(player -> Biome.getIdForBiome(currentBiome(player))))
                .uniform1i(PER_TICK, "biome_category", playerI(player -> categoryOf(currentBiome(player)).ordinal()))
                .uniform1i(PER_TICK, "biome_precipitation", playerI(player -> {
                    Biome biome = currentBiome(player);
                    if (biome.isSnowyBiome()) {
                        return 2;
                    }
                    return biome.canRain() ? 1 : 0;
                }))
                .uniform1f(PER_TICK, "rainfall", playerF(player -> currentBiome(player).getRainfall()))
                .uniform1f(PER_TICK, "temperature", playerF(player -> currentBiome(player).getTemperature(player.getPosition())));
    }

    private static BiomeCategories categoryOf(Biome biome) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)) {
            return BiomeCategories.THE_END;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) {
            return BiomeCategories.NETHER;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
            return BiomeCategories.OCEAN;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)) {
            return BiomeCategories.RIVER;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)) {
            return BiomeCategories.BEACH;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MUSHROOM)) {
            return BiomeCategories.MUSHROOM;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA)) {
            return BiomeCategories.MESA;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) {
            return BiomeCategories.JUNGLE;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) {
            return BiomeCategories.SAVANNA;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) {
            return BiomeCategories.TAIGA;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
            return BiomeCategories.SWAMP;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY)) {
            return BiomeCategories.DESERT;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)) {
            return BiomeCategories.ICY;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)) {
            return BiomeCategories.FOREST;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.HILLS)) {
            return BiomeCategories.EXTREME_HILLS;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS)) {
            return BiomeCategories.PLAINS;
        }

        return BiomeCategories.NONE;
    }

    private static Biome currentBiome(EntityPlayerSP player) {
        BlockPos pos = player.getPosition();
        return player.world.getBiome(pos);
    }

    private static IntSupplier playerI(ToIntFunction<EntityPlayerSP> function) {
        return () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            return player == null ? 0 : function.applyAsInt(player);
        };
    }

    private static FloatSupplier playerF(ToFloatFunction<EntityPlayerSP> function) {
        return () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            return player == null ? 0.0f : function.applyAsFloat(player);
        };
    }

    @FunctionalInterface
    private interface ToFloatFunction<T> {
        float applyAsFloat(T value);
    }
}
