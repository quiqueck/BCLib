package org.betterx.bclib.mixin.common;

import org.betterx.bclib.blocks.BaseAnvilBlock;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;
import org.betterx.bclib.recipes.AnvilRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu implements AnvilScreenHandlerExtended {
    private List<AnvilRecipe> bcl_recipes = Collections.emptyList();
    private AnvilRecipe bcl_currentRecipe;
    private DataSlot anvilLevel;

    @Shadow
    private int repairItemCountCost;

    @Final
    @Shadow
    private DataSlot cost;

    public AnvilMenuMixin(
            @Nullable MenuType<?> menuType,
            int i,
            Inventory inventory,
            ContainerLevelAccess containerLevelAccess
    ) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    public void be_initAnvilLevel(int syncId, Inventory inventory, ContainerLevelAccess context, CallbackInfo info) {
        this.anvilLevel = addDataSlot(DataSlot.standalone());
        if (context != ContainerLevelAccess.NULL) {
            int level = context.evaluate((world, blockPos) -> {
                Block anvilBlock = world.getBlockState(blockPos).getBlock();
                return LeveledAnvilBlock.getAnvilCraftingLevel(anvilBlock);
            }, 0);
            anvilLevel.set(level);
        } else {
            anvilLevel.set(0);
        }
    }

    @Shadow
    public abstract void createResult();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    protected void bcl_canTakeOutput(Player player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (bcl_currentRecipe != null) {
            info.setReturnValue(bcl_currentRecipe.checkHammerDurability(inputSlots, player));
        }
    }

    @Inject(method = "method_24922", at = @At(value = "HEAD"), cancellable = true)
    private static void bcl_onDamageAnvil(Player player, Level level, BlockPos blockPos, CallbackInfo ci) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!player.getAbilities().instabuild
                && blockState.getBlock() instanceof BaseAnvilBlock anvil
                && player.getRandom().nextDouble() < 0.12) {
            BlockState damaged = anvil.damageAnvilUse(blockState, player.getRandom());
            BaseAnvilBlock.destroyWhenNull(level, blockPos, damaged);
            ci.cancel();
        }
    }


    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    protected void bcl_onTakeAnvilOutput(Player player, ItemStack stack, CallbackInfo info) {
        if (bcl_currentRecipe != null) {
            final int ingredientSlot = AnvilRecipe.getIngredientSlot(inputSlots);

            inputSlots.getItem(ingredientSlot).shrink(bcl_currentRecipe.getInputCount());
            stack = bcl_currentRecipe.craft(inputSlots, player);
            slotsChanged(inputSlots);
            access.execute((level, blockPos) -> {
                final BlockState anvilState = level.getBlockState(blockPos);
                final Block anvilBlock = anvilState.getBlock();
                if (anvilBlock instanceof BaseAnvilBlock anvil) {
                    if (!player.getAbilities().instabuild
                            && anvilState.is(BlockTags.ANVIL)
                            && player.getRandom().nextDouble() < 0.1) {
                        BlockState damagedState = anvil.damageAnvilUse(anvilState, player.getRandom());
                        BaseAnvilBlock.destroyWhenNull(level, blockPos, damagedState);
                    } else {
                        level.levelEvent(LevelEvent.SOUND_ANVIL_USED, blockPos, 0);
                    }
                }
            });
            info.cancel();
        }
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void bcl_updateOutput(CallbackInfo info) {
        RecipeManager recipeManager = this.player.level().getRecipeManager();
        bcl_recipes = recipeManager.getRecipesFor(AnvilRecipe.TYPE, inputSlots, player.level());
        if (bcl_recipes.size() > 0) {
            int anvilLevel = this.anvilLevel.get();
            bcl_recipes = bcl_recipes.stream()
                                     .filter(recipe -> anvilLevel >= recipe.getAnvilLevel())
                                     .collect(Collectors.toList());
            if (bcl_recipes.size() > 0) {
                if (bcl_currentRecipe == null || !bcl_recipes.contains(bcl_currentRecipe)) {
                    bcl_currentRecipe = bcl_recipes.get(0);
                }
                bcl_updateResult();
                info.cancel();
            } else {
                bcl_currentRecipe = null;
            }
        }
    }

    @Inject(method = "setItemName", at = @At("HEAD"), cancellable = true)
    public void bcl_setNewItemName(String string, CallbackInfoReturnable<Boolean> cir) {
        if (bcl_currentRecipe != null) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            be_previousRecipe();
            return true;
        } else if (id == 1) {
            be_nextRecipe();
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    private void bcl_updateResult() {
        if (bcl_currentRecipe == null) return;
        resultSlots.setItem(0, bcl_currentRecipe.assemble(inputSlots, this.player.level().registryAccess()));
        broadcastChanges();
    }

    @Override
    public void bcl_updateCurrentRecipe(AnvilRecipe recipe) {
        this.bcl_currentRecipe = recipe;
        bcl_updateResult();
    }

    @Override
    public AnvilRecipe bcl_getCurrentRecipe() {
        return bcl_currentRecipe;
    }

    @Override
    public List<AnvilRecipe> bcl_getRecipes() {
        return bcl_recipes;
    }
}
