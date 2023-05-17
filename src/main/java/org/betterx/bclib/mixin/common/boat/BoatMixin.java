package org.betterx.bclib.mixin.common.boat;

import org.betterx.bclib.items.boat.BoatTypeOverride;
import org.betterx.bclib.items.boat.CustomBoatTypeOverride;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class BoatMixin extends Entity implements CustomBoatTypeOverride {
    private BoatTypeOverride bcl_type = null;
    @Shadow
    @Final
    private static EntityDataAccessor<Integer> DATA_ID_TYPE;

    public BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public void setCustomType(BoatTypeOverride type) {
        bcl_type = type;
        if (type == null)
            this.entityData.set(DATA_ID_TYPE, Boat.Type.OAK.ordinal());
        else
            this.entityData.set(DATA_ID_TYPE, bcl_type.ordinal());
    }

    public BoatTypeOverride bcl_getCustomType() {
        bcl_type = BoatTypeOverride.byId(this.entityData.get(DATA_ID_TYPE));
        return bcl_type;
    }

    @Inject(method = "setVariant", at = @At("HEAD"), cancellable = true)
    void bcl_setType(Boat.Type type, CallbackInfo ci) {
        if (bcl_type != null) {
            this.entityData.set(DATA_ID_TYPE, bcl_type.ordinal());
            ci.cancel();
        }
    }

    @Inject(method = "getVariant", at = @At("HEAD"), cancellable = true)
    void bcl_getBoatType(CallbackInfoReturnable<Boat.Type> cir) {
        BoatTypeOverride type = BoatTypeOverride.byId(this.entityData.get(DATA_ID_TYPE));
        if (type != null) {
            bcl_type = type;
            cir.setReturnValue(Boat.Type.OAK);
        }
    }


    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    void bcl_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        BoatTypeOverride type = this.bcl_getCustomType();
        if (type != null) {
            compoundTag.putString("cType", type.name());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    void bcl_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.contains("cType")) {
            this.setCustomType(BoatTypeOverride.byName(compoundTag.getString("cType")));
        } else {
            this.setCustomType(null);
        }
    }

    @Inject(method = "getDropItem", at = @At("HEAD"), cancellable = true)
    void bcl_getDropItem(CallbackInfoReturnable<Item> cir) {
        BoatTypeOverride type = this.bcl_getCustomType();
        if (type != null) {
            BoatItem boat = type.getBoatItem();
            if (boat != null) {
                cir.setReturnValue(boat);
            }
        }
    }

    @Inject(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;kill()V", shift = At.Shift.AFTER), cancellable = true)
    void bcl_checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
        BoatTypeOverride type = this.bcl_getCustomType();
        if (type != null) {
            if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                for (int i = 0; i < 3; ++i) {
                    this.spawnAtLocation(type.getPlanks());
                }
                for (int i = 0; i < 2; ++i) {
                    this.spawnAtLocation(Items.STICK);
                }

                this.resetFallDistance();
                ci.cancel();
            }


        }
    }

}
