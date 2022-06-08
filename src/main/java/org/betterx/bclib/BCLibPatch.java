package org.betterx.bclib;

import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.datafixer.ForcedLevelPatch;
import org.betterx.bclib.api.v2.datafixer.MigrationProfile;
import org.betterx.bclib.api.v2.generator.GeneratorOptions;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.util.MHelper;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Optional;

public final class BCLibPatch {
    public static void register() {
        // TODO separate values in config on client side (config screen)
        if (Configs.MAIN_CONFIG.repairBiomes() && (GeneratorOptions.fixEndBiomeSource() || GeneratorOptions.fixNetherBiomeSource())) {
            DataFixerAPI.registerPatch(BiomeSourcePatch::new);
        }
    }
}

final class BiomeSourcePatch extends ForcedLevelPatch {
    private static final String NETHER_BIOME_SOURCE = "bclib:nether_biome_source";
    private static final String END_BIOME_SOURCE = "bclib:end_biome_source";
    private static final String MC_NETHER = "minecraft:the_nether";
    private static final String MC_END = "minecraft:the_end";

    protected BiomeSourcePatch() {
        super(BCLib.MOD_ID, "1.2.1");
    }


    @Override
    protected Boolean runLevelDatPatch(CompoundTag root, MigrationProfile profile) {
        //make sure we have a working generators file before attempting to patch
        LevelGenUtil.migrateGeneratorSettings();

        final CompoundTag worldGenSettings = root.getCompound("Data").getCompound("WorldGenSettings");
        final CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
        final RegistryAccess registryAccess = RegistryAccess.builtinCopy();
        final RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);


        boolean result = false;

        result |= checkDimension(worldGenSettings, dimensions, registryAccess, registryOps, LevelStem.NETHER);
        result |= checkDimension(worldGenSettings, dimensions, registryAccess, registryOps, LevelStem.END);

        System.out.println("Dimensions:" + dimensions);
        return result;
//        if (root != null) return false;
//
//        boolean result = false;
//
//        if (GeneratorOptions.fixNetherBiomeSource()) {
//            if (!dimensions.contains(MC_NETHER) || !isBCLibEntry(dimensions.getCompound(MC_NETHER))) {
//                CompoundTag dimRoot = new CompoundTag();
//                dimRoot.put("generator", makeNetherGenerator(seed));
//                dimRoot.putString("type", MC_NETHER);
//                dimensions.put(MC_NETHER, dimRoot);
//                result = true;
//            }
//        }
//
//        if (GeneratorOptions.fixEndBiomeSource()) {
//            if (!dimensions.contains(MC_END) || !isBCLibEntry(dimensions.getCompound(MC_END))) {
//                CompoundTag dimRoot = new CompoundTag();
//                dimRoot.put("generator", makeEndGenerator(seed));
//                dimRoot.putString("type", MC_END);
//                dimensions.put(MC_END, dimRoot);
//                result = true;
//            }
//        }
//
//        return result;
    }

    private boolean checkDimension(
            CompoundTag worldGenSettings,
            CompoundTag dimensions,
            RegistryAccess registryAccess,
            RegistryOps<Tag> registryOps,
            ResourceKey<LevelStem> dimensionKey
    ) {

        final long seed = worldGenSettings.contains("seed")
                ? worldGenSettings.getLong("seed")
                : MHelper.RANDOM.nextLong();

        final boolean genStructures = !worldGenSettings.contains("generate_features") || worldGenSettings.getBoolean(
                "generate_features");

        final boolean genBonusChest = worldGenSettings.contains("bonus_chest") && worldGenSettings.getBoolean(
                "bonus_chest");

        boolean result = false;
        CompoundTag dimensionTag = dimensions.getCompound(dimensionKey.location().toString());
        Optional<WorldGenSettings> oWorldGen = WorldGenSettings.CODEC
                .parse(new Dynamic<>(registryOps, worldGenSettings))
                .result();

        Optional<LevelStem> oLevelStem = LevelStem.CODEC
                .parse(new Dynamic<>(registryOps, dimensionTag))
                .resultOrPartial(BCLib.LOGGER::error);

        Optional<ChunkGenerator> netherGenerator = oLevelStem.map(l -> l.generator());
        int biomeSourceVersion = LevelGenUtil.getBiomeVersionForGenerator(netherGenerator.orElse(null));
        int targetVersion = LevelGenUtil.getBiomeVersionForCurrentWorld(dimensionKey);
        if (biomeSourceVersion != targetVersion) {
            Optional<Holder<LevelStem>> refLevelStem = LevelGenUtil.referenceStemForVersion(
                    dimensionKey,
                    targetVersion,
                    registryAccess,
                    oWorldGen.map(g -> g.seed()).orElse(seed),
                    oWorldGen.map(g -> g.generateStructures()).orElse(genStructures),
                    oWorldGen.map(g -> g.generateBonusChest()).orElse(genBonusChest)
            );

            BCLib.LOGGER.warning("The world uses the BiomeSource Version " + biomeSourceVersion + " but should have " + targetVersion + ".");
            BCLib.LOGGER.warning("Dimension: " + dimensionKey);
            BCLib.LOGGER.warning("Found: " + netherGenerator);
            BCLib.LOGGER.warning("Should: " + refLevelStem.map(l -> l.value().generator()));

            if (refLevelStem.isPresent()) {
                var levelStem = refLevelStem.get();
                BCLib.LOGGER.warning("Repairing level.dat in order to ensure world continuity.");
                var codec = LevelStem.CODEC.orElse(levelStem.value());
                var encodeResult = codec.encodeStart(registryOps, levelStem.value());
                if (encodeResult.result().isPresent()) {
                    dimensions.put(dimensionKey.location().toString(), encodeResult.result().get());
                    result = true;
                } else {
                    BCLib.LOGGER.error("Unable to encode '" + dimensionKey + "' generator for level.dat.");
                }
            } else {
                BCLib.LOGGER.error("Unable to update '" + dimensionKey + "' generator in level.dat.");
            }
        }

        return result;
    }

    private boolean isBCLibEntry(CompoundTag dimRoot) {
        String type = dimRoot.getCompound("generator").getCompound("biome_source").getString("type");
        if (type.isEmpty() || type.length() < 5) {
            return false;
        }
        return type.startsWith("bclib");
    }

    public static CompoundTag makeNetherGenerator(long seed) {
        CompoundTag generator = new CompoundTag();
        generator.putString("type", "minecraft:noise");
        generator.putString("settings", "minecraft:nether");
        generator.putLong("seed", seed);

        CompoundTag biomeSource = new CompoundTag();
        biomeSource.putString("type", NETHER_BIOME_SOURCE);
        biomeSource.putLong("seed", seed);
        generator.put("biome_source", biomeSource);

        return generator;
    }

    public static CompoundTag makeEndGenerator(long seed) {
        CompoundTag generator = new CompoundTag();
        generator.putString("type", "minecraft:noise");
        generator.putString("settings", "minecraft:end");
        generator.putLong("seed", seed);

        CompoundTag biomeSource = new CompoundTag();
        biomeSource.putString("type", END_BIOME_SOURCE);
        biomeSource.putLong("seed", seed);
        generator.put("biome_source", biomeSource);

        return generator;
    }
}
