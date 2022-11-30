package org.betterx.worlds.together.surfaceRules;

import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.jetbrains.annotations.ApiStatus;

public class SurfaceRuleRegistry {
    public static final ResourceKey<Registry<AssignedSurfaceRule>> SURFACE_RULES_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/betterx/surface_rules"));
    public static final Predicate<ResourceKey<LevelStem>> NON_MANAGED_DIMENSIONS = dim -> dim != LevelStem.NETHER && dim != LevelStem.END;
    public static final Predicate<ResourceKey<LevelStem>> ALL_DIMENSIONS = dim -> true;

    //public static Registry<AssignedSurfaceRule> BUILTIN_SURFACE_RULES;

    private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
        return ResourceKey.createRegistryKey(location);
    }

    @ApiStatus.Internal
    public static void bootstrap(BootstapContext<AssignedSurfaceRule> ctx) {
//        ctx.register(
//                ResourceKey.create(
//                        SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
//                        WorldsTogether.makeID("dummy")
//                ),
//                new AssignedSurfaceRule(
//                        SurfaceRules.state(Blocks.YELLOW_CONCRETE.defaultBlockState()),
//                        WorldsTogether.makeID("none")
//                )
//        );
        for (var entry : KNOWN.entrySet()) {
            ctx.register(entry.getKey(), entry.getValue());
        }
    }

    private static Map<ResourceKey<AssignedSurfaceRule>, AssignedSurfaceRule> KNOWN = new HashMap<>();

    public static ResourceKey<AssignedSurfaceRule> registerRule(
            ResourceLocation ruleID,
            SurfaceRules.RuleSource rules,
            ResourceLocation biomeID
    ) {
        final ResourceKey<AssignedSurfaceRule> key = ResourceKey.create(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                ruleID
        );
        KNOWN.put(
                key,
                new AssignedSurfaceRule(
                        SurfaceRules.ifTrue(
                                SurfaceRules.isBiome(ResourceKey.create(Registries.BIOME, biomeID)),
                                rules
                        ), biomeID
                )
        );

        return key;
    }

    public static void ensureStaticallyLoaded() {

    }

}
