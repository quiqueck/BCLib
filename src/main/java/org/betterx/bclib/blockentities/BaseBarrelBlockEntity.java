package org.betterx.bclib.blockentities;

import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.registry.BaseBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BaseBarrelBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> inventory;
    private int viewerCount;

    private BaseBarrelBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
        super(type, blockPos, blockState);
        this.inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public BaseBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(BaseBlockEntities.BARREL, blockPos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.inventory);
        }

        //return tag;
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.inventory);
        }
    }

    public int getContainerSize() {
        return 27;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    protected void setItems(NonNullList<ItemStack> list) {
        this.inventory = list;
    }

    protected Component getDefaultName() {
        return Component.translatable("container.barrel");
    }

    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (viewerCount < 0) {
                viewerCount = 0;
            }

            ++viewerCount;
            BlockState blockState = this.getBlockState();
            if (!blockState.getValue(BarrelBlock.OPEN)) {
                playSound(blockState, SoundEvents.BARREL_OPEN);
                setOpen(blockState, true);
            }

            if (level != null) {
                scheduleUpdate();
            }
        }
    }

    private void scheduleUpdate() {
        level.scheduleTick(getBlockPos(), getBlockState().getBlock(), 5);
    }

    public void tick() {
        if (level != null) {
            viewerCount = ChestBlockEntity.getOpenCount(level, worldPosition);
            if (viewerCount > 0) {
                scheduleUpdate();
            } else {
                BlockState blockState = getBlockState();
                if (!(blockState.getBlock() instanceof BaseBarrelBlock)) {
                    setRemoved();
                    return;
                }
                if (blockState.getValue(BarrelBlock.OPEN)) {
                    playSound(blockState, SoundEvents.BARREL_CLOSE);
                    setOpen(blockState, false);
                }
            }
        }
    }

    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            --this.viewerCount;
        }
    }

    private void setOpen(BlockState state, boolean open) {
        if (level != null) {
            level.setBlock(this.getBlockPos(), state.setValue(BarrelBlock.OPEN, open), 3);
        }
    }

    private void playSound(BlockState blockState, SoundEvent soundEvent) {
        if (level != null) {
            Vec3i vec3i = blockState.getValue(BarrelBlock.FACING).getNormal();
            double d = (double) this.worldPosition.getX() + 0.5D + (double) vec3i.getX() / 2.0D;
            double e = (double) this.worldPosition.getY() + 0.5D + (double) vec3i.getY() / 2.0D;
            double f = (double) this.worldPosition.getZ() + 0.5D + (double) vec3i.getZ() / 2.0D;
            level.playSound(
                    null,
                    d,
                    e,
                    f,
                    soundEvent,
                    SoundSource.BLOCKS,
                    0.5F,
                    this.level.random.nextFloat() * 0.1F + 0.9F
            );
        }
    }
}