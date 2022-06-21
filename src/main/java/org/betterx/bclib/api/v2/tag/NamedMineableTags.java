package org.betterx.bclib.api.v2.tag;

import org.betterx.worlds.together.tag.v3.MineableTags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * @deprecated Replaced by {@link MineableTags}
 */
@Deprecated(forRemoval = true)
public class NamedMineableTags {
    /**
     * @deprecated use {@link MineableTags#AXE} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> AXE = MineableTags.AXE;
    /**
     * @deprecated use {@link MineableTags#HOE} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> HOE = MineableTags.HOE;
    /**
     * @deprecated use {@link MineableTags#PICKAXE} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> PICKAXE = MineableTags.PICKAXE;
    /**
     * @deprecated use {@link MineableTags#SHEARS} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> SHEARS = MineableTags.SHEARS;
    /**
     * @deprecated use {@link MineableTags#SHOVEL} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> SHOVEL = MineableTags.SHOVEL;
    /**
     * @deprecated use {@link MineableTags#SWORD} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> SWORD = MineableTags.SWORD;
    /**
     * @deprecated use {@link MineableTags#HAMMER} instead
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Block> HAMMER = MineableTags.HAMMER;
}
