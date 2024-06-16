package org.betterx.bclib.registry;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.items.BaseDiscItem;
import org.betterx.bclib.items.BaseDrinkItem;
import org.betterx.bclib.items.BaseSpawnEggItem;
import org.betterx.bclib.items.ModelProviderItem;
import org.betterx.bclib.models.RecordItemModelProvider;
import org.betterx.bclib.recipes.SmithingTemplates;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;

@Deprecated(forRemoval = true)
public class ItemRegistry extends BaseRegistry<Item> {
    public ItemRegistry(PathConfig config) {
        super(config);
    }

    public Item registerDisc(ResourceLocation itemId, int power, ResourceKey<JukeboxSong> sound, int lengthInSeconds) {
        Item item = BaseDiscItem.create(sound, BehaviourBuilders.createDisc());
        if (item != null) {
            RecordItemModelProvider.add(item);
            if (!config.getBoolean("musicDiscs", itemId.getPath(), true)) {
                return item;
            }
            register(itemId, item);
            TagManager.ITEMS.add(CommonItemTags.MUSIC_DISCS, item);
        }
        return item;
    }

    public SmithingTemplateItem registerSmithingTemplateItem(
            ResourceLocation id,
            List<ResourceLocation> baseSlotEmptyIcons,
            List<ResourceLocation> additionalSlotEmptyIcons
    ) {
        final SmithingTemplateItem item = SmithingTemplates
                .create(id)
                .setBaseSlotEmptyIcons(baseSlotEmptyIcons)
                .setAdditionalSlotEmptyIcons(additionalSlotEmptyIcons)
                .build();
        register(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_smithing_template"), item);
        return item;
    }

    public Item register(ResourceLocation itemId) {
        return register(itemId, new ModelProviderItem(makeItemSettings()));
    }

    @Override
    public Item register(ResourceLocation itemId, Item item) {
        if (!config.getBoolean("items", itemId.getPath(), true)) {
            return item;
        }

        registerItem(itemId, item);

        return item;
    }

    public Item registerTool(ResourceLocation itemId, Item item) {
        if (!config.getBoolean("tools", itemId.getPath(), true)) {
            return item;
        }
        registerItem(itemId, item);

        return item;
    }

    public Item registerEgg(ResourceLocation itemId, EntityType<? extends Mob> type, int background, int dots) {
        SpawnEggItem item = new BaseSpawnEggItem(type, background, dots, makeItemSettings());

        if (!config.getBoolean("spawnEggs", itemId.getPath(), true)) {
            return item;
        }

        DefaultDispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
            public ItemStack execute(BlockSource pointer, ItemStack stack) {
                Direction direction = pointer.state().getValue(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack);
                entityType.spawn(
                        pointer.level(),
                        stack,
                        null,
                        pointer.pos().relative(direction),
                        MobSpawnType.DISPENSER,
                        direction != Direction.UP,
                        false
                );
                stack.shrink(1);
                return stack;
            }
        };
        DispenserBlock.registerBehavior(item, behavior);
        return register(itemId, item);
    }

    public Item registerFood(ResourceLocation itemId, int hunger, float saturation, MobEffectInstance... effects) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationModifier(saturation);
        for (MobEffectInstance effect : effects) {
            builder.effect(effect, 1F);
        }
        return registerFood(itemId, builder.build());
    }

    public Item registerFood(ResourceLocation itemId, FoodProperties foodComponent) {
        return register(itemId, new ModelProviderItem(makeItemSettings().food(foodComponent)));
    }

    public Item registerDrink(ResourceLocation itemId, FoodProperties foodComponent) {
        return register(itemId, new BaseDrinkItem(makeItemSettings().stacksTo(1).food(foodComponent)));
    }

    public Item registerDrink(ResourceLocation itemId, int hunger, float saturation) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationModifier(saturation);
        return registerDrink(itemId, builder.build());
    }

    @Override
    public void registerItem(ResourceLocation id, Item item) {
        if (item != null && item != Items.AIR) {
            Registry.register(BuiltInRegistries.ITEM, id, item);
            getModItems(id.getNamespace()).add(item);
        }
    }

    public Item register(ResourceLocation itemId, Item item, String category) {
        if (config.getBoolean(category, itemId.getPath(), true)) {
            registerItem(itemId, item);
        }
        return item;
    }
}
