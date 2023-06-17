package org.betterx.bclib.interfaces;

import org.betterx.bclib.config.Configs;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.util.List;

public interface SurvivesOnSpecialGround extends SurvivesOn {
    String getSurvivableBlocksString();

    default String prefixComponent() {
        return "tooltip.bclib.place_on";
    }

    @Environment(EnvType.CLIENT)
    static List<String> splitLines(String input) {
        final int MAX_LEN = 45;
        List<String> lines = Lists.newArrayList();

        while (input.length() > MAX_LEN) {
            int idx = input.lastIndexOf(",", MAX_LEN);
            if (idx >= 0) {
                lines.add(input.substring(0, idx + 1).trim());
                input = input.substring(idx + 1).trim();
            } else {
                break;
            }
        }
        lines.add(input.trim());

        return lines;
    }

    @Environment(EnvType.CLIENT)
    static void appendHoverText(SurvivesOnSpecialGround surv, List<Component> list) {
        if (!Configs.CLIENT_CONFIG.survivesOnHint()) return;
        final int MAX_LINES = 7;
        final String description = surv.getSurvivableBlocksString();
        List<String> lines = splitLines(description);
        if (lines.size() == 1) {
            list.add(Component.translatable(surv.prefixComponent(), lines.get(0)).withStyle(ChatFormatting.GREEN));
        } else if (lines.size() > 1) {
            list.add(Component.translatable(surv.prefixComponent(), "").withStyle(ChatFormatting.GREEN));
            for (int i = 0; i < Math.min(lines.size(), MAX_LINES); i++) {
                String line = lines.get(i);
                if (i == MAX_LINES - 1 && i < lines.size() - 1) line += " ...";
                list.add(Component.literal("  " + line).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static void appendHoverTextUnderwater(List<Component> list) {
        list.add(Component.translatable("tooltip.bclib.place_underwater")
                          .withStyle(ChatFormatting.GREEN));
    }

    @Environment(EnvType.CLIENT)
    public static void appendHoverTextUnderwaterInDepth(List<Component> list, int depth) {
        list.add(Component.translatable("tooltip.bclib.place_underwater_depth", depth)
                          .withStyle(ChatFormatting.GREEN));
    }

    default boolean canSurviveOnTop(LevelReader world, BlockPos pos) {
        return isSurvivable(world.getBlockState(pos.below()));
    }

    default boolean canSurviveOnBottom(LevelReader world, BlockPos pos) {
        return isSurvivable(world.getBlockState(pos.above()));
    }
    default boolean isTerrain(BlockState state) {
        return isSurvivable(state);
    }
}