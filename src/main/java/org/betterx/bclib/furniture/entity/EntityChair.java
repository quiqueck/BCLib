package org.betterx.bclib.furniture.entity;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.furniture.block.AbstractChair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class EntityChair extends Entity {
    public EntityChair(EntityType<? extends EntityChair> type, Level world) {
        super(type, world);
    }

    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public void tick() {
        if (this.level().getBlockState(this.blockPosition()).getBlock() instanceof AbstractChair)
            localTick();
        else {
            BCLib.LOGGER.info("Chair Block was deleted -> ejecting");
            this.ejectPassengers();
            this.discard();
        }
    }

    protected void localTick() {
        super.tick();
        List<Entity> pushableEntities = this.level().getEntities(
                this,
                this.getBoundingBox().inflate(0.7f, -0.01f, 0.7f),
                EntitySelector.pushableBy(this)
        );

        if (!pushableEntities.isEmpty()) {
            boolean free = !this.level().isClientSide && !(this.getControllingPassenger() instanceof Player);
            for (int j = 0; j < pushableEntities.size(); ++j) {
                Entity entity = pushableEntities.get(j);
                if (entity.hasPassenger(this)) continue;
                if (free
                        && this.getPassengers().size() < this.getMaxPassengers()
                        && !entity.isPassenger()
                        //&& entity.getBbWidth() < this.getBbWidth()
                        && entity instanceof LivingEntity
                        && !(entity instanceof WaterAnimal)
                        && !(entity instanceof Player)
                ) {
                    entity.startRiding(this);
                    continue;
                }
                this.push(entity);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < this.getMaxPassengers();
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        for (Entity e : getPassengers()) {
            if (e instanceof LivingEntity le) return le;
        }
        return null;
    }

    @Override
    public void push(Entity entity) {
        //Do nothing. Should not be pushable
    }


    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return Vec3.ZERO;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    public static AttributeSupplier getAttributeContainer() {
        return AttributeSupplier.builder().build();
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }
}
