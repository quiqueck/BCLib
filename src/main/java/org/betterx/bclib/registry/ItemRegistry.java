package org.betterx.bclib.registry;

import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.items.BaseDiscItem;
import org.betterx.bclib.items.BaseDrinkItem;
import org.betterx.bclib.items.BaseSpawnEggItem;
import org.betterx.bclib.items.ModelProviderItem;
import org.betterx.bclib.items.tool.BaseAxeItem;
import org.betterx.bclib.items.tool.BaseHoeItem;
import org.betterx.bclib.items.tool.BasePickaxeItem;
import org.betterx.bclib.items.tool.BaseShearsItem;
import org.betterx.bclib.models.RecordItemModelProvider;
import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.ToolTags;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.DispenserBlock;

public class ItemRegistry extends BaseRegistry<Item> {
    public ItemRegistry(CreativeModeTab creativeTab, PathConfig config) {
        super(creativeTab, config);
    }

    public Item registerDisc(ResourceLocation itemId, int power, SoundEvent sound, int lengthInSeconds) {
        RecordItem item = BaseDiscItem.create(power, sound, makeItemSettings().stacksTo(1), lengthInSeconds);
        if (item != null) {
            RecordItemModelProvider.add(item);
            if (!config.getBoolean("musicDiscs", itemId.getPath(), true)) {
                return item;
            }
            register(itemId, item);
        }
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

        if (item instanceof ShovelItem) {
            TagManager.ITEMS.add(ToolTags.FABRIC_SHOVELS, item);
        } else if (item instanceof SwordItem) {
            TagManager.ITEMS.add(ToolTags.FABRIC_SWORDS, item);
        } else if (item instanceof BasePickaxeItem) {
            TagManager.ITEMS.add(ToolTags.FABRIC_PICKAXES, item);
        } else if (item instanceof BaseAxeItem) {
            TagManager.ITEMS.add(ToolTags.FABRIC_AXES, item);
        } else if (item instanceof BaseHoeItem) {
            TagManager.ITEMS.add(ToolTags.FABRIC_HOES, item);
        } else if (item instanceof BaseShearsItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_SHEARS, CommonItemTags.SHEARS);
            DispenserBlock.registerBehavior(item.asItem(), new ShearsDispenseItemBehavior());
        }

        return item;
    }

    public Item registerEgg(ResourceLocation itemId, EntityType<? extends Mob> type, int background, int dots) {
        SpawnEggItem item = new BaseSpawnEggItem(type, background, dots, makeItemSettings());

        if (!config.getBoolean("spawnEggs", itemId.getPath(), true)) {
            return item;
        }

        DefaultDispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
            public ItemStack execute(BlockSource pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
                entityType.spawn(
                        pointer.getLevel(),
                        stack,
                        null,
                        pointer.getPos().relative(direction),
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
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
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
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
        return registerDrink(itemId, builder.build());
    }

    @Override
    public void registerItem(ResourceLocation id, Item item) {
        if (item != null && item != Items.AIR) {
            Registry.register(Registry.ITEM, id, item);
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
