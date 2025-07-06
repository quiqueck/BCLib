package org.betterx.bclib.items;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.SpawnEggItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

public class BaseSpawnEggItem extends SpawnEggItem implements ItemModelProvider {
    public BaseSpawnEggItem(EntityType<? extends Mob> type, Properties settings) {
        super(type, settings);
    }

    /**
     * @deprecated Use {@link #BaseSpawnEggItem(EntityType, Properties)} instead.
     * This constructor is deprecated and will be removed in future versions.
     */
    @Deprecated(forRemoval = true)
    public BaseSpawnEggItem(EntityType<? extends Mob> type, int primaryColor, int secondaryColor, Properties settings) {
        this(type, settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_SPAWN_EGG, resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }
}
