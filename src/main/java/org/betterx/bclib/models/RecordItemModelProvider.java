package org.betterx.bclib.models;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

/**
 * This was just added because the constructor of a RecordItem from 1.19 to 1.19.1 changed
 * and we only need to add the {@link ItemModelProvider} interface.
 * <p>
 * In order to keep cross version compat for bclib we choose to use reflection to instanciate
 * RecordItem, but we need an additional registry that will provide the {@link ItemModelProvider}
 * for RecordItems.
 * <p>
 * This class (and and all according changes in
 * {@link org.betterx.bclib.client.models.CustomModelBakery#loadCustomModels(ResourceManager)} can
 * be abandoned when we drop support for 1.19.
 */
@ApiStatus.Internal
public class RecordItemModelProvider {
    @ApiStatus.Internal
    public static final ItemModelProvider DEFAULT_PROVIDER = new ItemModelProvider() {
    };
    private static final Map<Item, ItemModelProvider> PROIVDER_MAPPPING = new HashMap<>();

    @ApiStatus.Internal
    public static void add(RecordItem record) {
        if (record != null) {
            PROIVDER_MAPPPING.put(record, DEFAULT_PROVIDER);
        }
    }

    @ApiStatus.Internal
    public static boolean has(Item i) {
        return PROIVDER_MAPPPING.containsKey(i);
    }

    @ApiStatus.Internal
    public static ItemModelProvider get(Item i) {
        return PROIVDER_MAPPPING.get(i);
    }
}
