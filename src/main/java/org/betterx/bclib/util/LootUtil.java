package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.LootPoolAccessor;
import de.ambertation.wover.tag.api.TagManager;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;
import de.ambertation.wover.tag.api.predefined.MineableTags;
import de.ambertation.wover.tag.api.predefined.ToolTags;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
        ResourceKey<LootTable> tableID = block.getLootTable().orElse(null);
        if (tableID == null) {
            return Optional.empty();
        }

        final LootParams ctx = builder.withParameter(LootContextParams.BLOCK_STATE, state)
                                      .create(LootContextParamSets.BLOCK);
        final ServerLevel level = ctx.getLevel();
        final LootTable table = level.getServer().reloadableRegistries().getLootTable(tableID);

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
                        Object first = list.getFirst();
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

    public static boolean isShear(ItemStack tool) {
        return tool.is(Items.SHEARS) | tool.is(CommonItemTags.SHEARS) || TagManager.isToolWithMineableTag(
                tool,
                MineableTags.SHEARS
        );
    }

    public static boolean isShear(ItemStack itemStack, Item item) {
        if (item == Items.SHEARS) {
            return itemStack.is(item) | itemStack.is(CommonItemTags.SHEARS);
        } else {
            return itemStack.is(item);
        }
    }

    public static boolean isCorrectTool(ItemLike block, BlockState state, ItemStack tool) {
        if (tool == null) return false;
        if (state != null && tool.isCorrectToolForDrops(state)) return true;

        // The mineable/* block tags now carry the information the AddMineable* marker interfaces used to.
        // Query them on the block's state (the given one, or the block's default state).
        final BlockState tagState = state != null
                ? state
                : (block instanceof Block b ? b.defaultBlockState() : null);
        if (tagState == null) return false;

        if (tagState.is(MineableTags.AXE)) {
            if (tool.is(ItemTags.AXES) || tool.is(ToolTags.FABRIC_AXES)) return true;
        }
        if (tagState.is(MineableTags.PICKAXE)) {
            if (tool.is(ItemTags.PICKAXES) || tool.is(ToolTags.FABRIC_PICKAXES)) return true;
        }
        if (tagState.is(MineableTags.HOE)) {
            if (tool.is(ItemTags.HOES) || tool.is(ToolTags.FABRIC_HOES)) return true;
        }
        if (tagState.is(MineableTags.SHOVEL)) {
            if (tool.is(ItemTags.SHOVELS) || tool.is(ToolTags.FABRIC_SHOVELS)) return true;
        }
        if (tagState.is(MineableTags.SWORD)) {
            if (tool.is(ItemTags.SWORDS) || tool.is(ToolTags.FABRIC_SWORDS)) return true;
        }
        if (tagState.is(MineableTags.SHEARS)) {
            if (isShear(tool)) return true;
        }
        if (tagState.is(MineableTags.HAMMER)) {
            return tool.is(CommonItemTags.HAMMERS);
        }
        return false;
    }
}
