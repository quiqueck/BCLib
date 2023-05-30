package org.betterx.bclib.commands;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;
import org.betterx.bclib.blocks.BaseStairsBlock;
import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.Taskbar.Feature;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DumpDatapack {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> bnContext) {
        return bnContext
                .then(Commands.literal("dump_datapack")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .executes(DumpDatapack::dumpDatapack)
                );
    }

    private record Dumper<T, C extends Codec<? extends T>, H extends Holder<? extends T>>(
            Function<T, C> codecFunction,
            Function<Holder<T>, T> contentTransform
    ) {
        Dumper(Function<T, C> codecFunction) {
            this(codecFunction, h -> h.value());
        }
    }

    private static final Map<ResourceLocation, Dumper> DUMPERS = new HashMap<>();

    static int dumpDatapack(CommandContext<CommandSourceStack> ctx) {
        File base = new File(System.getProperty("user.dir"), "bclib_datapack_dump");
        dumpDatapack(base, ctx.getSource().getLevel().registryAccess(), ctx);


        ctx.getSource().sendSuccess(
                () -> Component.literal("Succesfully written to:\n    ").append(
                        Component.literal(base.toString()).setStyle(Style.EMPTY.withUnderlined(true))
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    public static void dumpDatapack(File base, RegistryAccess registryAccess, CommandContext<CommandSourceStack> ctx) {
        final RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder = gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        registryAccess.registries().forEach(r -> dump(base, r, registryOps, gson));

        BCLib.LOGGER.info("- Serializing Dimensions ");

        for (ServerLevel serverLevel : ctx.getSource().getLevel().getServer().getAllLevels()) {
            File f1 = new File(base, serverLevel.dimension().location().getNamespace());
            f1 = new File(f1, "dimension");
            f1 = new File(f1, serverLevel.dimension().location().getPath() + ".json");
            f1.getParentFile().mkdirs();

            try {
                LevelStem stem = new LevelStem(
                        serverLevel.dimensionTypeRegistration(),
                        serverLevel.getChunkSource().getGenerator()
                );
                Codec codec = LevelStem.CODEC;
                var o = codec
                        .encodeStart(registryOps, stem)
                        .result()
                        .orElse(new JsonObject());

                String content = gson.toJson(o);
                try {
                    Files.writeString(f1.toPath(), content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    BCLib.LOGGER.error("        ->> Unable to WRITE: " + e.getMessage());
                }
            } catch (Exception e) {
                BCLib.LOGGER.error("      ->> Unable to encode: " + e.getMessage());
            }
        }

    }


    private static <T> void dump(
            File base, RegistryAccess.RegistryEntry<T> registry,
            RegistryOps<JsonElement> registryOps,
            Gson gson
    ) {
        BCLib.LOGGER.info("- Serializing: " + registry.key().toString());
        DUMPERS.clear();
        DUMPERS.put(Registries.BIOME.location(), new Dumper<>((Biome v) -> Biome.DIRECT_CODEC));
        DUMPERS.put(
                Registries.CONFIGURED_FEATURE.location(),
                new Dumper<>((ConfiguredFeature v) -> ConfiguredFeature.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.WORLD_PRESET.location(),
                new Dumper<>((WorldPreset v) -> WorldPreset.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.NOISE_SETTINGS.location(),
                new Dumper<>((NoiseGeneratorSettings v) -> NoiseGeneratorSettings.DIRECT_CODEC)
        );
        DUMPERS.put(Registries.STRUCTURE.location(), new Dumper<>((Structure v) -> Structure.DIRECT_CODEC));
        DUMPERS.put(
                Registries.DIMENSION_TYPE.location(),
                new Dumper<>((DimensionType v) -> DimensionType.DIRECT_CODEC)
        );
        DUMPERS.put(BCLBiomeRegistry.BCL_BIOMES_REGISTRY.location(), new Dumper<>((BCLBiome v) -> v.codec().codec()));
        DUMPERS.put(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY.location(),
                new Dumper<>((AssignedSurfaceRule v) -> AssignedSurfaceRule.CODEC)
        );
        DUMPERS.put(
                Registries.CONFIGURED_CARVER.location(),
                new Dumper<>((ConfiguredWorldCarver v) -> ConfiguredWorldCarver.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.PROCESSOR_LIST.location(),
                new Dumper<>((StructureProcessorList v) -> StructureProcessorType.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.FLAT_LEVEL_GENERATOR_PRESET.location(),
                new Dumper<>((FlatLevelGeneratorPreset v) -> FlatLevelGeneratorPreset.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.DENSITY_FUNCTION.location(),
                new Dumper<>((DensityFunction v) -> DensityFunction.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.PLACED_FEATURE.location(),
                new Dumper<>((PlacedFeature v) -> PlacedFeature.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.NOISE.location(),
                new Dumper<>((NormalNoise.NoiseParameters v) -> NormalNoise.NoiseParameters.DIRECT_CODEC)
        );

        DUMPERS.put(
                Registries.TEMPLATE_POOL.location(),
                new Dumper<>((StructureTemplatePool v) -> StructureTemplatePool.DIRECT_CODEC)
        );
        DUMPERS.put(
                Registries.STRUCTURE_SET.location(),
                new Dumper<>((StructureSet v) -> StructureSet.DIRECT_CODEC)
        );


        Dumper d = DUMPERS.getOrDefault(registry.key().location(), new Dumper(v -> registry.value().byNameCodec()));
        //Dumper d = DUMPERS.get(registry.key().location());
        if (d != null)
            dump(base, registry, registryOps, gson, d.codecFunction, d.contentTransform);
        else
            BCLib.LOGGER.warning("    No Codec Found");

    }

    private static <T, S> void dump(
            File base, RegistryAccess.RegistryEntry<T> registry,
            RegistryOps<JsonElement> registryOps,
            Gson gson,
            Function<Object, Codec<?>> codecFunction,
            Function<Holder<T>, Object> contentTransform
    ) {
        BCLib.LOGGER.info("   - Serializing Tags");
        dumpTags(base, registry, registryOps, gson);

        BCLib.LOGGER.info("   - Serializing Content");
        int[] count = {0, 0};
        registry
                .value()
                .entrySet()
                .stream()
                .map(e -> e.getKey()).map(key -> registry.value().getHolder(key).get())
                .forEach(holder -> {
                    File f1 = new File(base, holder.unwrapKey().get().location().getNamespace());
                    f1 = new File(f1, registry.key().location().getPath());
                    f1.mkdirs();
                    f1 = new File(f1, holder.unwrapKey().get().location().getPath() + ".json");
                    f1.getParentFile().mkdirs();
                    //BCLib.LOGGER.info("     - " + f1);

                    Object obj = contentTransform.apply(holder);
                    try {
                        Codec codec = codecFunction.apply(obj);
                        var o = codec
                                .encodeStart(registryOps, obj)
                                .result()
                                .orElse(new JsonObject());

                        String content = gson.toJson(o);
                        try {
                            Files.writeString(f1.toPath(), content, StandardCharsets.UTF_8);
                            count[0]++;
                        } catch (IOException e) {
                            count[1]++;
                            BCLib.LOGGER.error("        ->> Unable to WRITE: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        count[1]++;
                        BCLib.LOGGER.error("      ->> Unable to encode: " + e.getMessage());
                    }
                });
        BCLib.LOGGER.info("     -> Wrote " + count[0] + " files (" + count[1] + " errors)");
    }


    private static <T> void dumpDatapackOld(
            File base,
            RegistryAccess.RegistryEntry<T> registry,
            RegistryOps<JsonElement> registryOps,
            Gson gson
    ) {
        BCLib.LOGGER.info(registry.key().toString());
        dumpTags(base, registry, registryOps, gson);

        registry
                .value()
                .entrySet()
                .stream()
                .map(e -> e.getKey()).map(key -> registry.value().getHolder(key).get())
                .forEach(holder -> {
                    File f1 = new File(base, holder.unwrapKey().get().location().getNamespace());
                    f1 = new File(f1, registry.key().location().getPath());
                    f1.mkdirs();
                    f1 = new File(f1, holder.unwrapKey().get().location().getPath() + ".json");
                    f1.getParentFile().mkdirs();

                    Codec[] codec = {null};

                    //BCLib.LOGGER.info("   - " + f1);
                    Object obj = holder;

                    while (obj instanceof Holder<?>) {
                        obj = ((Holder<?>) obj).value();
                    }

                    if (obj instanceof BiomeSource || obj instanceof Feature) {
                        System.out.print("");
                    }
                    System.out.println(obj.getClass());

                    if (obj instanceof Structure s) {
                        codec[0] = s.type().codec();
                    } else if (obj instanceof BCLBiome s) {
                        codec[0] = BiomeData.CODEC;
                    } else if (obj instanceof StructureProcessorList s) {
                        codec[0] = StructureProcessorType.LIST_OBJECT_CODEC;
                    } else if (obj instanceof GameEvent) {
                        return;
                    } else if (obj instanceof Fluid) {
                        return;
                    } else if (obj instanceof MobEffect) {
                        return;
                    } else if (obj instanceof BaseStairsBlock) {
                        return;
                    } else if (obj instanceof RuleTestType<?>) {
                        codec[0] = registry.value().byNameCodec();
                    } else if (obj instanceof PosRuleTestType<?>) {
                        codec[0] = registry.value().byNameCodec();
                    } else if (obj instanceof WorldGenSettings) {
                        codec[0] = registry.value().byNameCodec();
                    } else if (obj instanceof LevelStem) {
                        codec[0] = registry.value().byNameCodec();
                    }

                    if (codec[0] == null) {
                        for (Method m : obj.getClass().getMethods()) {
                            if (!Modifier.isStatic(m.getModifiers())) {
                                m.setAccessible(true);
                                if (m.getParameterTypes().length == 0) {
                                    if (Codec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = (Codec) m.invoke(obj);
                                            BCLib.LOGGER.debug("      Got Codec from " + m);
                                            break;
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    } else if (KeyDispatchCodec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = ((KeyDispatchCodec) m.invoke(obj)).codec();
                                            BCLib.LOGGER.debug("      Got Codec from " + m);
                                            break;
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    } else if (KeyDispatchDataCodec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = ((KeyDispatchDataCodec) m.invoke(obj)).codec();
                                            BCLib.LOGGER.debug("      Got Codec from " + m);
                                            break;
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (codec[0] == null) {
                        //Try to find DIRECT_CODEC field
                        for (Field f : obj.getClass().getFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if ("DIRECT_CODEC".equals(f.getName())) {
                                    f.setAccessible(true);
                                    try {
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.debug("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("      !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    //Try to find CODEC field
                    if (codec[0] == null) {
                        for (Field f : obj.getClass().getFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if ("CODEC".equals(f.getName())) {
                                    try {
                                        f.setAccessible(true);
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.debug("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("     !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    //Try to find any Codec field
                    if (codec[0] == null) {
                        for (Field f : obj.getClass().getFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if (Codec.class.isAssignableFrom(f.getType())) {
                                    f.setAccessible(true);
                                    try {
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.debug("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("     !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    if (codec[0] == null) {
                        codec[0] = registry.value().byNameCodec();
                    }

                    if (codec[0] == null) {
                        codec[0] = registry.value().byNameCodec();
                    }


                    if (codec[0] != null) {
                        try {
                            var o = codec[0]
                                    .encodeStart(registryOps, obj)
                                    .result()
                                    .orElse(new JsonObject());

                            String content = gson.toJson(o);
                            try {
                                Files.writeString(f1.toPath(), content, StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                BCLib.LOGGER.error("      ->> Unable to WRITE: " + e.getMessage());
                            }
                        } catch (Exception e) {
                            BCLib.LOGGER.error("      ->> Unable to encode: " + e.getMessage());
                        }
                    } else {
                        BCLib.LOGGER.error("     !!! Could not determine Codec: " + obj.getClass());
                    }
                });

    }

    private static <T> void dumpTags(
            File base,
            RegistryAccess.RegistryEntry<T> registry,
            RegistryOps<JsonElement> registryOps,
            Gson gson
    ) {
        // Tag Output
        registry.value()
                .getTagNames()
                .map(tagKey -> registry.value().getTag(tagKey))
                .filter(tag -> tag.isPresent())
                .map(tag -> tag.get())
                .forEach(tag -> {
                    File f1 = new File(base, tag.key().location().getNamespace());
                    f1 = new File(f1, "tags");
                    f1 = new File(f1, registry.key().location().getPath());
                    f1 = new File(f1, tag.key().location().getPath() + ".json");
                    f1.getParentFile().mkdirs();

                    TagFile tf = new TagFile(
                            tag.stream()
                               .map(holder -> holder.unwrapKey())
                               .filter(k -> k.isPresent())
                               .map(k -> TagEntry.element(k.get().location()))
                               .toList(),
                            true
                    );
                    var o = TagFile.CODEC
                            .encodeStart(registryOps, tf)
                            .result()
                            .orElse(new JsonObject());
                    String content = gson.toJson(o);
                    try {
                        Files.writeString(f1.toPath(), content, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        BCLib.LOGGER.error("      ->> Unable to WRITE: " + e.getMessage());
                    }
                });
    }
}
