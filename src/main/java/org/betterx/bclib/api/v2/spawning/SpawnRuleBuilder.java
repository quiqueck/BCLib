package org.betterx.bclib.api.v2.spawning;

import org.betterx.bclib.entity.BCLEntityWrapper;
import org.betterx.bclib.interfaces.SpawnRule;
import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Supplier;

public class SpawnRuleBuilder<M extends Mob> {
    private static final Map<String, SpawnRuleEntry> RULES_CACHE = Maps.newHashMap();
    private static final SpawnRuleBuilder INSTANCE = new SpawnRuleBuilder();
    private final List<SpawnRuleEntry> rules = Lists.newArrayList();
    private SpawnRuleEntry entryInstance;
    private EntityType<M> entityType;

    private SpawnRuleBuilder() {
    }

    /**
     * Starts new rule building process.
     *
     * @param entityType The entity you want to build a rule for
     * @return prepared {@link SpawnRuleBuilder} instance.
     */
    public static SpawnRuleBuilder start(EntityType<? extends Mob> entityType) {
        INSTANCE.entityType = entityType;
        INSTANCE.rules.clear();
        return INSTANCE;
    }

    /**
     * Starts new rule building process.
     *
     * @param wrapper The entity you want to build a rule for
     * @return prepared {@link SpawnRuleBuilder} instance.
     */
    public static SpawnRuleBuilder start(BCLEntityWrapper<? extends Mob> wrapper) {
        SpawnRuleBuilder builder = start(wrapper.type());
        if (!wrapper.canSpawn()) {
            builder.preventSpawn();
        }
        return builder;
    }

    /**
     * Stop entity spawn entierly
     *
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder preventSpawn() {
        entryInstance = getFromCache("prevent", () -> {
            return new SpawnRuleEntry(-1, (type, world, spawnReason, pos, random) -> false, "Prevent Spawn");
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Stop entity spawn on peaceful {@link Difficulty}
     *
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder notPeaceful() {
        entryInstance = getFromCache("not_peaceful", () -> {
            return new SpawnRuleEntry(
                    0,
                    (type, world, spawnReason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL,
                    "Not Peaceful"
            );
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Restricts entity spawn above world surface (flying mobs).
     *
     * @param minHeight minimal spawn height.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder aboveGround(int minHeight) {
        entryInstance = getFromCache("above_ground", () -> {
            return new SpawnRuleEntry(0, (type, world, spawnReason, pos, random) -> {
                if (pos.getY() < world.getMinBuildHeight() + 2) {
                    return false;
                }
                return BlocksHelper.findSurfaceBelow(
                                           world,
                                           pos,
                                           pos.getY() - minHeight,
                                           (bs) -> !BlocksHelper.isFree(bs)
                                   )
                                   .isEmpty();
                //return pos.getY() > world.getHeight(Types.WORLD_SURFACE, pos.getX(), pos.getZ()) + minHeight;
            },
                    "Above Ground"
            );
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Restricts entity spawn below world logical height (useful for Nether mobs).
     *
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder belowMaxHeight() {
        entryInstance = getFromCache("below_max_height", () -> {
            return new SpawnRuleEntry(
                    0,
                    (type, world, spawnReason, pos, random) -> pos.getY() < world.dimensionType()
                                                                                 .logicalHeight(),
                    "Below Max Height"
            );
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Restricts spawning only to vanilla valid blocks.
     *
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder onlyOnValidBlocks() {
        entryInstance = getFromCache("only_on_valid_blocks", () -> {
            return new SpawnRuleEntry(0, (type, world, spawnReason, pos, random) -> {
                BlockPos below = pos.below();
                return world.getBlockState(below).isValidSpawn(world, below, type);
            }, "Only On Valid Blocks");
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Restricts spawning only to specified blocks.
     *
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder onlyOnBlocks(Block... blocks) {
        final Block[] floorBlocks = blocks;
        Arrays.sort(floorBlocks, Comparator.comparing(Block::getDescriptionId));

        StringBuilder builder = new StringBuilder("only_on_blocks");
        for (Block block : floorBlocks) {
            builder.append('_');
            builder.append(block.getDescriptionId());
        }

        entryInstance = getFromCache(builder.toString(), () -> {
            return new SpawnRuleEntry(0, (type, world, spawnReason, pos, random) -> {
                Block below = world.getBlockState(pos.below()).getBlock();
                for (Block floor : floorBlocks) {
                    if (floor == below) {
                        return true;
                    }
                }
                return false;
            }, "Only On Blocks");
        });

        rules.add(entryInstance);
        return this;
    }

    /**
     * Will spawn entity with 1 / chance probability (randomly).
     *
     * @param chance probability limit.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder withChance(int chance) {
        entryInstance = getFromCache("with_chance_" + chance, () -> {
            return new SpawnRuleEntry(
                    1,
                    (type, world, spawnReason, pos, random) -> random.nextInt(chance) == 0,
                    "With Chance"
            );
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Will spawn entity only below specified brightness value.
     *
     * @param lightLevel light level upper limit.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder belowBrightness(int lightLevel) {
        entryInstance = getFromCache("below_brightness_" + lightLevel, () -> {
            return new SpawnRuleEntry(
                    2,
                    (type, world, spawnReason, pos, random) -> world.getMaxLocalRawBrightness(pos) <= lightLevel,
                    "Below Brightness"
            );
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Will spawn entity only above specified brightness value.
     *
     * @param lightLevel light level lower limit.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder aboveBrightness(int lightLevel) {
        entryInstance = getFromCache("above_brightness_" + lightLevel, () -> {
            return new SpawnRuleEntry(
                    2,
                    (type, world, spawnReason, pos, random) -> world.getMaxLocalRawBrightness(pos) >= lightLevel,
                    "Above Brightness"
            );
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Entity spawn will follow common vanilla spawn rules - spawn in darkness and not on peaceful level.
     *
     * @param lightLevel light level upper limit.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder hostile(int lightLevel) {
        return notPeaceful().belowBrightness(lightLevel);
    }

    /**
     * Entity spawn will follow common vanilla spawn rules - spawn in darkness (below light level 7) and not on peaceful level.
     *
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder vanillaHostile() {
        return hostile(7);
    }

    /**
     * Will spawn entity only if count of nearby entities will be lower than specified.
     *
     * @param selectorType selector {@link EntityType} to search.
     * @param count        max entity count.
     * @param side         side of box to search in.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder maxNearby(EntityType<?> selectorType, int count, int side) {
        final Class<? extends Entity> baseClass = selectorType.getBaseClass();
        entryInstance = getFromCache("max_nearby_" + selectorType.getDescriptionId() + "_" + count + "_" + side, () -> {
            return new SpawnRuleEntry(3, (type, world, spawnReason, pos, random) -> {
                try {
                    final AABB box = new AABB(pos).inflate(side, world.getHeight(), side);
                    final List<?> list = world.getEntitiesOfClass(baseClass, box, (entity) -> true);
                    return list.size() < count;
                } catch (Exception e) {
                    return true;
                }
            }, "Max Nearby " + count + "/" + side);
        });
        rules.add(entryInstance);
        return this;
    }

    /**
     * Will spawn entity only if count of nearby entities with same type will be lower than specified.
     *
     * @param count max entity count.
     * @param side  side of box to search in.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder maxNearby(int count, int side) {
        return maxNearby(entityType, count, side);
    }

    /**
     * Will spawn entity only if count of nearby entities with same type will be lower than specified.
     *
     * @param count max entity count.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder maxNearby(int count) {
        return maxNearby(entityType, count, 256);
    }

    /**
     * Allows to add custom spawning rule for specific entities.
     *
     * @param rule {@link SpawnRule} rule, can be a lambda expression.
     * @return same {@link SpawnRuleBuilder} instance.
     */
    public SpawnRuleBuilder customRule(SpawnRule rule) {
        rules.add(new SpawnRuleEntry(7, rule, "Custom Rule"));
        return this;
    }

    /**
     * Finalize spawning rule creation.
     *
     * @param spawnType     {@link Type} of spawn.
     * @param heightmapType {@link Types} heightmap type.
     */
    public void build(Type spawnType, Types heightmapType) {
        final List<SpawnRuleEntry> rulesCopy = Lists.newArrayList(this.rules);
        Collections.sort(rulesCopy);

        SpawnPredicate<M> predicate = (entityType, serverLevelAccessor, mobSpawnType, blockPos, random) -> {
            for (SpawnRuleEntry rule : rulesCopy) {
                if (!rule.canSpawn(entityType, serverLevelAccessor, mobSpawnType, blockPos, random)) {
//                    BCLib.LOGGER.info("Rejected Spawn of "
//                            + entityType.getDescriptionId()
//                            + " at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ()
//                            + " because " + rule.debugName
//                            + " at " + serverLevelAccessor.getBlockState(blockPos)
//                            + " above " + serverLevelAccessor.getBlockState(blockPos.below())
//                    );

                    return false;
                }
            }
//            BCLib.LOGGER.info("Spawning " + entityType.getDescriptionId() + " at " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ() + "");
            return true;
        };

        SpawnPlacements.register(entityType, spawnType, heightmapType, predicate);
    }

    /**
     * Finalize spawning rule creation with No Restrictions spawn type, useful for flying entities.
     *
     * @param heightmapType {@link Types} heightmap type.
     */
    public void buildNoRestrictions(Types heightmapType) {
        build(Type.NO_RESTRICTIONS, heightmapType);
    }

    /**
     * Finalize spawning rule creation with On Ground spawn type, useful for common entities.
     *
     * @param heightmapType {@link Types} heightmap type.
     */
    public void buildOnGround(Types heightmapType) {
        build(Type.ON_GROUND, heightmapType);
    }

    /**
     * Finalize spawning rule creation with In Water spawn type, useful for water entities.
     *
     * @param heightmapType {@link Types} heightmap type.
     */
    public void buildInWater(Types heightmapType) {
        build(Type.IN_WATER, heightmapType);
    }

    /**
     * Finalize spawning rule creation with In Lava spawn type, useful for lava entities.
     *
     * @param heightmapType {@link Types} heightmap type.
     */
    public void buildInLava(Types heightmapType) {
        build(Type.IN_LAVA, heightmapType);
    }

    /**
     * Internal function, will take entry from cache or create it if necessary.
     *
     * @param name     {@link String} entry internal name.
     * @param supplier {@link Supplier} for {@link SpawnRuleEntry}.
     * @return new or existing {@link SpawnRuleEntry}.
     */
    private static SpawnRuleEntry getFromCache(String name, Supplier<SpawnRuleEntry> supplier) {
        return supplier.get();
//        SpawnRuleEntry entry = RULES_CACHE.get(name);
//        if (entry == null) {
//            entry = supplier.get();
//            RULES_CACHE.put(name, entry);
//        }
//        return entry;
    }
}
