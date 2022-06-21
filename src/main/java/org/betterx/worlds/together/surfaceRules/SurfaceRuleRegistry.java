package org.betterx.worlds.together.surfaceRules;

import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.Predicate;
import org.jetbrains.annotations.ApiStatus;

public class SurfaceRuleRegistry {
    public static final ResourceKey<Registry<AssignedSurfaceRule>> SURFACE_RULES_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/surface_rules"));
    public static final Predicate<ResourceKey<LevelStem>> NON_MANAGED_DIMENSIONS = dim -> dim != LevelStem.NETHER && dim != LevelStem.END;
    public static final Predicate<ResourceKey<LevelStem>> ALL_DIMENSIONS = dim -> true;

    public static Registry<AssignedSurfaceRule> BUILTIN_SURFACE_RULES;

    private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
        return ResourceKey.createRegistryKey(location);
    }

    @ApiStatus.Internal
    public static Holder<AssignedSurfaceRule> bootstrap(Registry<AssignedSurfaceRule> registry) {
        return BuiltinRegistries.register(
                registry,
                WorldsTogether.makeID("dummy"),
                new AssignedSurfaceRule(
                        SurfaceRules.state(Blocks.YELLOW_CONCRETE.defaultBlockState()),
                        WorldsTogether.makeID("none")
                )
        );
    }

    public static ResourceKey<AssignedSurfaceRule> registerRule(
            ResourceLocation ruleID,
            SurfaceRules.RuleSource rules,
            ResourceLocation biomeID
    ) {
        ResourceKey<AssignedSurfaceRule> key = ResourceKey.create(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                ruleID
        );
        Registry.register(SurfaceRuleRegistry.BUILTIN_SURFACE_RULES, key, new AssignedSurfaceRule(
                        SurfaceRules.ifTrue(
                                SurfaceRules.isBiome(ResourceKey.create(Registry.BIOME_REGISTRY, biomeID)),
                                rules
                        ), biomeID
                )
        );
        return key;
    }

    public static void ensureStaticallyLoaded() {

    }

}
