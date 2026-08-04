package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

/**
 * Marker trait: a block carrying this is a decoration plant that survives on <em>any</em> block that is
 * solid on the attachment face OR is in {@code minecraft:leaves} - the shared "placeable on any solid block
 * or leaves" rule, defined once in
 * {@link org.betterx.bclib.util.BlocksHelper#isDecorationSupport}. Rather than only on the blocks a
 * {@link SurvivesOnBlockTrait} lists.
 * <p>
 * Two independent things read this marker, kept in lockstep by attaching it to exactly the blocks whose
 * class uses the decoration rule:
 * <ul>
 *     <li>{@code VegetationBlockMixin} makes a {@link net.minecraft.world.level.block.VegetationBlock}
 *     carrying it place via {@code isDecorationSupport} (up-face);</li>
 *     <li>{@link #appendHoverText} emits the auto-generated "Survives on: any solid block or leaves"
 *     tooltip line (translation key {@code tooltip.bclib.place_on_solid_or_leaves}) - no per-block string.</li>
 * </ul>
 * Wall- and ceiling-plant classes (e.g. {@code BaseWallPlantBlock}) apply the rule in their own
 * {@code canSurvive}/{@code updateShape} and carry this marker purely for the matching tooltip. Carries no
 * data.
 */
public class SurvivesOnSolidTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "survives_on_solid");
    public static final SurvivesOnSolidTrait DEFAULT = new SurvivesOnSolidTrait();

    private SurvivesOnSolidTrait() {
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    /**
     * Appends the auto-generated "any solid block or leaves" placement hint for {@code block} if it carries
     * this marker. The line is a single shared, localized translation key - never a per-block hardcoded
     * string - so it always describes the real {@code isDecorationSupport} rule.
     *
     * @param block    the (registered) block to describe
     * @param consumer receives the tooltip line
     */
    @Environment(EnvType.CLIENT)
    public static void appendHoverText(Block block, Consumer<Component> consumer) {
        if (!Configs.CLIENT_CONFIG.survivesOnHint()) return;
        if (BlockTrait.hasRuntimeTrait(block, KEY)) {
            consumer.accept(Component.translatable("tooltip.bclib.place_on_solid_or_leaves")
                                     .withStyle(ChatFormatting.GREEN));
        }
    }
}
