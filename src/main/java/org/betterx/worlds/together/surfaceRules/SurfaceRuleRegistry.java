package org.betterx.worlds.together.surfaceRules;

import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class SurfaceRuleRegistry {
    public static final ResourceKey<Registry<SurfaceRules.RuleSource>> SURFACE_RULES_REGISTRY =
            createRegistryKey(WorldsTogether.makeID("worldgen/surface_rules"));

    public static Registry<SurfaceRules.RuleSource> SURFACE_RULES;

    private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
        return ResourceKey.createRegistryKey(location);
    }

    public static Holder<SurfaceRules.RuleSource> bootstrap(Registry<SurfaceRules.RuleSource> registry) {
        return BuiltinRegistries.register(
                registry,
                WorldsTogether.makeID("test"),
                SurfaceRules.state(Blocks.SCULK.defaultBlockState())
        );
    }

    public static void ensureStaticallyLoaded() {

    }

}
