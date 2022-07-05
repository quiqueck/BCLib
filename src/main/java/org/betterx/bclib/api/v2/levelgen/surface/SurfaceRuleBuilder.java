package org.betterx.bclib.api.v2.levelgen.surface;

import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.api.v2.levelgen.surface.rules.Conditions;
import org.betterx.bclib.api.v2.levelgen.surface.rules.DoubleBlockSurfaceNoiseCondition;
import org.betterx.bclib.api.v2.levelgen.surface.rules.NoiseCondition;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SurfaceRuleBuilder {
    private static final Map<String, SurfaceRuleEntry> RULES_CACHE = Maps.newHashMap();
    private static final SurfaceRuleBuilder INSTANCE = new SurfaceRuleBuilder();
    private final List<SurfaceRuleEntry> rules = Lists.newArrayList();
    private SurfaceRuleEntry entryInstance;
    private ResourceKey<Biome> biomeKey;

    private SurfaceRuleBuilder() {
    }

    public static SurfaceRuleBuilder start() {
        INSTANCE.biomeKey = null;
        INSTANCE.rules.clear();
        return INSTANCE;
    }

    /**
     * Restricts surface to only one biome.
     *
     * @param biomeKey {@link ResourceKey} for the {@link Biome}.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder biome(ResourceKey<Biome> biomeKey) {
        this.biomeKey = biomeKey;
        return this;
    }

    /**
     * Restricts surface to only one biome.
     *
     * @param biome {@link Biome}.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder biome(Biome biome) {
        return biome(BiomeAPI.getBiomeKey(biome));
    }

    /**
     * Set biome surface with specified {@link BlockState}. Example - block of grass in the Overworld biomes
     *
     * @param state {@link BlockState} for the ground cover.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder surface(BlockState state) {
        entryInstance = getFromCache("surface_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            rule = SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, rule);
            return new SurfaceRuleEntry(2, rule);
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome subsurface with specified {@link BlockState}. Example - dirt in the Overworld biomes.
     *
     * @param state {@link BlockState} for the subterrain layer.
     * @param depth block layer depth.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder subsurface(BlockState state, int depth) {
        entryInstance = getFromCache("subsurface_" + depth + "_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            rule = SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(depth, false, CaveSurface.FLOOR), rule);
            return new SurfaceRuleEntry(3, rule);
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome filler with specified {@link BlockState}. Example - stone in the Overworld biomes. The rule is added with priority 10.
     *
     * @param state {@link BlockState} for filling.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder filler(BlockState state) {
        entryInstance = getFromCache(
                "fill_" + state.toString(),
                () -> new SurfaceRuleEntry(10, SurfaceRules.state(state))
        );
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome floor with specified {@link BlockState}. Example - underside of a gravel floor. The rule is added with priority 3.
     *
     * @param state {@link BlockState} for the ground cover.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder floor(BlockState state) {
        entryInstance = getFromCache("floor_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            return new SurfaceRuleEntry(3, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, rule));
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome floor material with specified {@link BlockState} and height. The rule is added with priority 3.
     *
     * @param state  {@link BlockState} for the subterrain layer.
     * @param height block layer height.
     * @param noise  The noise object that is applied
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder belowFloor(BlockState state, int height, NoiseCondition noise) {
        entryInstance = getFromCache(
                "below_floor_" + height + "_" + state.toString() + "_" + noise.getClass()
                                                                              .getSimpleName(),
                () -> {
                    RuleSource rule = SurfaceRules.state(state);
                    rule = SurfaceRules.ifTrue(
                            SurfaceRules.stoneDepthCheck(
                                    height,
                                    false,
                                    CaveSurface.FLOOR
                            ),
                            SurfaceRules.ifTrue(noise, rule)
                    );
                    return new SurfaceRuleEntry(3, rule);
                }
        );
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome floor material with specified {@link BlockState} and height. The rule is added with priority 3.
     *
     * @param state  {@link BlockState} for the subterrain layer.
     * @param height block layer height.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder belowFloor(BlockState state, int height) {
        entryInstance = getFromCache("below_floor_" + height + "_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            rule = SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(height, false, CaveSurface.FLOOR), rule);
            return new SurfaceRuleEntry(3, rule);
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome ceiling with specified {@link BlockState}. Example - block of sandstone in the Overworld desert in air pockets. The rule is added with priority 3.
     *
     * @param state {@link BlockState} for the ground cover.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder ceil(BlockState state) {
        entryInstance = getFromCache("ceil_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            return new SurfaceRuleEntry(3, SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, rule));
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Set biome ceiling material with specified {@link BlockState} and height. Example - sandstone in the Overworld deserts. The rule is added with priority 3.
     *
     * @param state  {@link BlockState} for the subterrain layer.
     * @param height block layer height.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder aboveCeil(BlockState state, int height) {
        entryInstance = getFromCache("above_ceil_" + height + "_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            rule = SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(height, false, CaveSurface.CEILING), rule);
            return new SurfaceRuleEntry(3, rule);
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Will cover steep areas (with large terrain angle). Example - Overworld mountains.
     *
     * @param state {@link BlockState} for the steep layer.
     * @param depth layer depth
     * @return
     */
    public SurfaceRuleBuilder steep(BlockState state, int depth) {
        entryInstance = getFromCache("steep_" + depth + "_" + state.toString(), () -> {
            RuleSource rule = SurfaceRules.state(state);
            rule = SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(depth, false, CaveSurface.FLOOR), rule);
            rule = SurfaceRules.ifTrue(SurfaceRules.steep(), rule);
            int priority = depth < 1 ? 0 : 1;
            return new SurfaceRuleEntry(priority, rule);
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Allows to add custom rule.
     *
     * @param priority rule priority, lower values = higher priority (rule will be applied before others).
     * @param rule     custom {@link SurfaceRules.RuleSource}.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder rule(int priority, SurfaceRules.RuleSource rule) {
        rules.add(new SurfaceRuleEntry(priority, rule));
        return this;
    }

    /**
     * Allows to add custom rule.
     *
     * @param rule custom {@link SurfaceRules.RuleSource}.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder rule(SurfaceRules.RuleSource rule) {
        return rule(7, rule);
    }

    /**
     * Set biome floor with specified {@link BlockState} and the {@link DoubleBlockSurfaceNoiseCondition}. The rule is added with priority 3.
     *
     * @param surfaceBlockA {@link BlockState} for the ground cover.
     * @param surfaceBlockB {@link BlockState} for the alternative ground cover.
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder chancedFloor(BlockState surfaceBlockA, BlockState surfaceBlockB) {
        return chancedFloor(surfaceBlockA, surfaceBlockB, Conditions.DOUBLE_BLOCK_SURFACE_NOISE);
    }

    /**
     * Set biome floor with specified {@link BlockState} and the given Noise Function.  The rule is added with priority 3.
     *
     * @param surfaceBlockA {@link BlockState} for the ground cover.
     * @param surfaceBlockB {@link BlockState} for the alternative ground cover.
     * @param noise         The {@link NoiseCondition}
     * @return same {@link SurfaceRuleBuilder} instance.
     */
    public SurfaceRuleBuilder chancedFloor(BlockState surfaceBlockA, BlockState surfaceBlockB, NoiseCondition noise) {
        entryInstance = getFromCache(
                "chancedFloor_" + surfaceBlockA + "_" + surfaceBlockB + "_" + noise.getClass()
                                                                                   .getSimpleName(),
                () -> {
                    RuleSource rule =
                            SurfaceRules.ifTrue(
                                    SurfaceRules.ON_FLOOR,
                                    SurfaceRules.sequence(
                                            SurfaceRules.ifTrue(
                                                    noise,
                                                    SurfaceRules.state(
                                                            surfaceBlockA)
                                            ),
                                            SurfaceRules.state(surfaceBlockB)
                                    )
                            );
                    return new SurfaceRuleEntry(4, rule);
                }
        );
        rules.add(entryInstance);
        return this;
    }

    public SurfaceRuleBuilder chancedFloor(BlockState surfaceBlockA, RuleSource surfaceBlockB, NoiseCondition noise) {
        entryInstance = getFromCache(
                "chancedFloor_" + surfaceBlockA + "_" + surfaceBlockB + "_" + noise.getClass()
                                                                                   .getSimpleName(),
                () -> {
                    RuleSource rule =
                            SurfaceRules.ifTrue(
                                    SurfaceRules.ON_FLOOR,
                                    SurfaceRules.sequence(
                                            SurfaceRules.ifTrue(
                                                    noise,
                                                    SurfaceRules.state(
                                                            surfaceBlockA)
                                            ),
                                            surfaceBlockB
                                    )
                            );
                    return new SurfaceRuleEntry(4, rule);
                }
        );
        rules.add(entryInstance);
        return this;
    }

    /**
     * Finalise rule building process.
     *
     * @return {@link SurfaceRules.RuleSource}.
     */
    public SurfaceRules.RuleSource build() {
        Collections.sort(rules);
        List<SurfaceRules.RuleSource> ruleList = rules.stream().map(entry -> entry.getRule()).toList();
        SurfaceRules.RuleSource[] ruleArray = ruleList.toArray(new SurfaceRules.RuleSource[ruleList.size()]);
        SurfaceRules.RuleSource rule = SurfaceRules.sequence(ruleArray);
        if (biomeKey != null) {
            rule = SurfaceRules.ifTrue(SurfaceRules.isBiome(biomeKey), rule);
        }
        return rule;
    }

    /**
     * Internal function, will take entry from cache or create it if necessary.
     *
     * @param name     {@link String} entry internal name.
     * @param supplier {@link Supplier} for {@link SurfaceRuleEntry}.
     * @return new or existing {@link SurfaceRuleEntry}.
     */
    private static SurfaceRuleEntry getFromCache(String name, Supplier<SurfaceRuleEntry> supplier) {
        SurfaceRuleEntry entry = RULES_CACHE.get(name);
        if (entry == null) {
            entry = supplier.get();
            RULES_CACHE.put(name, entry);
        }
        return entry;
    }
}
