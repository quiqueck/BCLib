package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.LootPoolAccessor;
import org.betterx.bclib.interfaces.tools.*;
import org.betterx.bclib.items.tool.BaseShearsItem;
import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.tag.v3.ToolTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LootUtil {
    public static Optional<List<ItemStack>> getDrops(
            BlockBehaviour block,
            BlockState state,
            LootParams.Builder builder
    ) {
        ResourceLocation tableID = block.getLootTable();
        if (tableID == BuiltInLootTables.EMPTY) {
            return Optional.empty();
        }

        final LootParams ctx = builder.withParameter(LootContextParams.BLOCK_STATE, state)
                                      .create(LootContextParamSets.BLOCK);
        final ServerLevel level = ctx.getLevel();
        final LootTable table = level.getServer().getLootData().getLootTable(tableID);

        if (table == LootTable.EMPTY) return Optional.empty();
        return Optional.of(table.getRandomItems(ctx));
    }

    public static boolean addToPool(LootTable.Builder table, int index, ArrayList<LootPoolEntryContainer> newEntries) {
        List<LootPool> pools = new ArrayList<>(0);
        try {
            for (Field f : table.getClass()
                                .getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    List<?> list = (List<?>) f.get(table);
                    if (list != null && list.size() > 0) {
                        Object first = list.get(0);
                        if (first != null && LootPool.class.isAssignableFrom(first.getClass())) {
                            pools = (List<LootPool>) list;
                            break;
                        }
                    }
                }
            }

            if (pools != null && pools.size() > index) {
                LootPool pool = pools.get(index);
                LootPoolAccessor acc = (LootPoolAccessor) pool;
                pools.set(index, acc.bcl_mergeEntries(newEntries));

                return true;
            }
        } catch (Throwable t) {
            BCLib.LOGGER.error("ERROR building loot table: " + t.getMessage());
        }

        return false;
    }

    public static boolean isCorrectTool(ItemLike block, BlockState state, ItemStack tool) {
        if (tool == null) return false;
        if (state != null && tool.isCorrectToolForDrops(state)) return true;

        if (block instanceof AddMineableAxe) {
            if (tool.is(ItemTags.AXES) || tool.is(ToolTags.FABRIC_AXES)) return true;
        }
        if (block instanceof AddMineablePickaxe) {
            if (tool.is(ItemTags.PICKAXES) || tool.is(ToolTags.FABRIC_PICKAXES)) return true;
        }
        if (block instanceof AddMineableHoe) {
            if (tool.is(ItemTags.HOES) || tool.is(ToolTags.FABRIC_HOES)) return true;
        }
        if (block instanceof AddMineableShovel) {
            if (tool.is(ItemTags.SHOVELS) || tool.is(ToolTags.FABRIC_SHOVELS)) return true;
        }
        if (block instanceof AddMineableSword) {
            if (tool.is(ItemTags.SWORDS) || tool.is(ToolTags.FABRIC_SWORDS)) return true;
        }
        if (block instanceof AddMineableShears) {
            if (BaseShearsItem.isShear(tool)) return true;
        }
        if (block instanceof AddMineableHammer) {
            if (tool.is(CommonItemTags.HAMMERS)) return true;
        }
        return false;
    }
}
