package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blockentities.BaseFurnaceBlockEntity;
import org.betterx.bclib.blockentities.DynamicBlockEntityType;
import org.betterx.bclib.blockentities.DynamicBlockEntityType.BlockEntitySupplier;
import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.furniture.entity.EntityChair;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BaseBlockEntities {
    public static final DynamicBlockEntityType<BaseFurnaceBlockEntity> FURNACE = registerBlockEntityType(
            BCLib.makeID(
                    "furnace"), BaseFurnaceBlockEntity::new
    );

    public static final EntityType<EntityChair> CHAIR = registerEntity(
            BCLib.makeID("chair"), EntityType.Builder
                    .of(EntityChair::new, MobCategory.MISC)
                    .sized(0.5F, 0.8F)
                    .fireImmune()
                    .noSummon()
    );


    public static <T extends Entity> EntityType<T> registerEntity(
            ResourceLocation id,
            EntityType.Builder<T> entityBuilder
    ) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        var entity = entityBuilder.build(key);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, key, entity);
        return entity;
    }

    public static <T extends BlockEntity> DynamicBlockEntityType<T> registerBlockEntityType(
            ResourceLocation typeId,
            BlockEntitySupplier<? extends T> supplier
    ) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, typeId, new DynamicBlockEntityType<>(supplier));
    }

    public static void register() {
    }

    public static Block[] getFurnaces() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(block -> block instanceof BaseFurnaceBlock)
                .toArray(Block[]::new);
    }
}
