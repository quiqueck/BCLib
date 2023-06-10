package org.betterx.bclib.creativetab;

import org.betterx.bclib.registry.BaseRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import java.util.LinkedList;
import java.util.List;

public class BCLCreativeTabManager {
    public final String modID;
    protected final List<BCLCreativeTab> tabs = new LinkedList<>();

    public static BCLCreativeTabManager create(String modID) {
        return new BCLCreativeTabManager(modID);
    }

    protected BCLCreativeTabManager(String modID) {
        this.modID = modID;
    }

    public BCLCreativeTab.Builder createTab(String name) {
        return new BCLCreativeTab.Builder(this, name);
    }

    public BCLCreativeTab.Builder createBlockTab(ItemLike icon) {
        return new BCLCreativeTab.Builder(this, "blocks").setIcon(icon).setPredicate(BCLCreativeTab.BLOCKS);
    }

    public BCLCreativeTab.Builder createItemsTab(ItemLike icon) {
        return new BCLCreativeTab.Builder(this, "items").setIcon(icon);
    }

    public BCLCreativeTabManager processBCLRegistry() {
        process(BaseRegistry.getModItems(modID));
        process(BaseRegistry.getModBlockItems(modID));
        return this;
    }

    public BCLCreativeTabManager process(List<Item> items) {
        for (Item item : items) {
            for (BCLCreativeTab tab : tabs) {
                if (tab.predicate.contains(item)) {
                    tab.addItem(item);
                    break;
                }
            }
        }

        return this;
    }

    public void register() {
        for (BCLCreativeTab tab : tabs) {
            var tabItem = FabricItemGroup
                    .builder()
                    .icon(() -> new ItemStack(tab.icon))
                    .title(tab.title)
                    .displayItems((featureFlagSet, output) -> {
                        output.acceptAll(tab.items.stream().map(ItemStack::new).toList());
                        //tab.items.clear();
                    }).build();

            Registry.register(
                    BuiltInRegistries.CREATIVE_MODE_TAB,
                    tab.key,
                    tabItem
            );
        }

        //this.tabs.clear();
    }
}
