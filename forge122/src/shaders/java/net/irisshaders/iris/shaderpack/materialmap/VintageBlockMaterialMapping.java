package net.irisshaders.iris.shaderpack.materialmap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.embeddedt.embeddium.compat.iris.IBlockEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.irisshaders.iris.IrisLogging.IRIS_LOGGER;

public class VintageBlockMaterialMapping {
    public static Object2IntMap<IBlockState> createBlockStateIdMap(Int2ObjectMap<List<IBlockEntry>> blockPropertiesMap) {
        Object2IntMap<IBlockState> blockStateIds = new Object2IntOpenHashMap<>();
        blockStateIds.defaultReturnValue(-1);

        blockPropertiesMap.forEach((intId, entries) -> {
            for (IBlockEntry entry : entries) {
                addBlockStates(entry, blockStateIds, intId);
            }
        });

        return blockStateIds;
    }

    private static void addBlockStates(IBlockEntry entry, Object2IntMap<IBlockState> idMap, int intId) {
        if (entry.isTag()) {
            entry.expandEntries().forEach(nested -> addBlockStates(nested, idMap, intId));
            return;
        }

        for (ResourceLocation location : resolveLocations(entry.id(), entry, intId)) {
            Block block = Block.REGISTRY.getObject(location);
            if (block == null || (block == Blocks.AIR && !"air".equals(location.getPath()))) {
                continue;
            }

            addBlockStates(block, location, entry, idMap, intId);
        }
    }

    private static void addBlockStates(Block block, ResourceLocation location, IBlockEntry entry,
                                       Object2IntMap<IBlockState> idMap, int intId) {
        Map<IProperty<?>, String> properties = resolveProperties(block, location, entry, intId);
        boolean isLiquidBlock = isLiquidBlock(block);

        if (!entry.metadataIds().isEmpty()) {
            for (int metadata : entry.metadataIds()) {
                IBlockState state = block.getStateFromMeta(metadata);
                if (isLiquidBlock || properties.isEmpty() || checkState(state, properties)) {
                    idMap.putIfAbsent(state, intId);
                }
            }
            return;
        }

        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (isLiquidBlock || properties.isEmpty() || checkState(state, properties)) {
                idMap.putIfAbsent(state, intId);
            }
        }
    }

    private static boolean isLiquidBlock(Block block) {
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA || block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            return true;
        }

        try {
            return block.getDefaultState().getMaterial().isLiquid();
        } catch (Exception e) {
            return false;
        }
    }

    private static List<ResourceLocation> resolveLocations(NamespacedId id, IBlockEntry entry, int intId) {
        List<ResourceLocation> locations = new ArrayList<>();
        String namespace = id.getNamespace();
        String name = id.getName();

        if (isModernGrassPlantAlias(namespace, name, entry, intId)) {
            addLocation(locations, namespace, "tallgrass");
            return locations;
        }

        addLocation(locations, namespace, name);

        if ("minecraft".equals(namespace)) {
            switch (name) {
                case "water" -> addLocation(locations, namespace, "flowing_water");
                case "flowing_water" -> addLocation(locations, namespace, "water");
                case "lava" -> addLocation(locations, namespace, "flowing_lava");
                case "flowing_lava" -> addLocation(locations, namespace, "lava");
                case "grass_block" -> addLocation(locations, namespace, "grass");
                case "short_grass" -> addLocation(locations, namespace, "tallgrass");
                case "tall_grass" -> addLocation(locations, namespace, "double_plant");
                case "dead_bush" -> addLocation(locations, namespace, "deadbush");
                case "sugar_cane" -> addLocation(locations, namespace, "reeds");
                case "lily_pad" -> addLocation(locations, namespace, "waterlily");
                case "cobweb" -> addLocation(locations, namespace, "web");
                case "redstone_lamp" -> addLocation(locations, namespace, "lit_redstone_lamp");
                case "oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves" ->
                        addLocation(locations, namespace, "leaves");
                case "acacia_leaves", "dark_oak_leaves" ->
                        addLocation(locations, namespace, "leaves2");
            }
        }

        return locations;
    }

    private static boolean isModernGrassPlantAlias(String namespace, String name, IBlockEntry entry, int intId) {
        if (!"minecraft".equals(namespace) || !"grass".equals(name)) {
            return false;
        }

        if (!entry.metadataIds().isEmpty() || !entry.propertyPredicates().isEmpty()) {
            return false;
        }

        return intId != 2 && intId != 10000 && intId != 10001 && intId != 10124 && intId != 10132;
    }

    private static void addLocation(List<ResourceLocation> locations, String namespace, String name) {
        ResourceLocation location = new ResourceLocation(namespace, name);
        if (!locations.contains(location)) {
            locations.add(location);
        }
    }

    private static Map<IProperty<?>, String> resolveProperties(Block block, ResourceLocation location,
                                                               IBlockEntry entry, int intId) {
        Map<IProperty<?>, String> properties = new HashMap<>();

        entry.propertyPredicates().forEach((key, value) -> {
            IProperty<?> property = findProperty(block, key);
            if (property == null) {
                IRIS_LOGGER.warn("Warning while parsing block.{}: block {} has no property named {}", intId, location, key);
            } else {
                properties.put(property, value);
            }
        });

        String leafVariant = resolveLegacyLeafVariant(location, entry.id());
        if (leafVariant != null) {
            IProperty<?> variantProperty = findProperty(block, "variant");
            if (variantProperty != null) {
                properties.putIfAbsent(variantProperty, leafVariant);
            }
        }

        return properties;
    }

    private static String resolveLegacyLeafVariant(ResourceLocation location, NamespacedId entryId) {
        if (!"minecraft".equals(location.getNamespace()) || !"minecraft".equals(entryId.getNamespace())) {
            return null;
        }

        String legacyName = location.getPath();
        String modernName = entryId.getName();
        return switch (modernName) {
            case "oak_leaves" -> "leaves".equals(legacyName) ? "oak" : null;
            case "spruce_leaves" -> "leaves".equals(legacyName) ? "spruce" : null;
            case "birch_leaves" -> "leaves".equals(legacyName) ? "birch" : null;
            case "jungle_leaves" -> "leaves".equals(legacyName) ? "jungle" : null;
            case "acacia_leaves" -> "leaves2".equals(legacyName) ? "acacia" : null;
            case "dark_oak_leaves" -> "leaves2".equals(legacyName) ? "dark_oak" : null;
            default -> null;
        };
    }

    private static IProperty<?> findProperty(Block block, String key) {
        for (IProperty<?> property : block.getBlockState().getProperties()) {
            if (property.getName().equals(key)) {
                return property;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean checkState(IBlockState state, Map<IProperty<?>, String> expectedValues) {
        for (Map.Entry<IProperty<?>, String> condition : expectedValues.entrySet()) {
            IProperty property = condition.getKey();
            String actualValue = property.getName((Comparable) state.getValue(property));

            if (!condition.getValue().equals(actualValue)) {
                return false;
            }
        }

        return true;
    }
}
