package org.betterx.bclib.mixin.common;

import org.betterx.bclib.blocks.BaseAnvilBlock;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;
import org.betterx.bclib.recipes.AnvilRecipe;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
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
    private List<AnvilRecipe> be_recipes = Collections.emptyList();
    private AnvilRecipe be_currentRecipe;
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
                if (anvilBlock instanceof LeveledAnvilBlock) {
                    return ((LeveledAnvilBlock) anvilBlock).getCraftingLevel();
                }
                return 1;
            }, 1);
            anvilLevel.set(level);
        } else {
            anvilLevel.set(1);
        }
    }

    @Shadow
    public abstract void createResult();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    protected void be_canTakeOutput(Player player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (be_currentRecipe != null) {
            info.setReturnValue(be_currentRecipe.checkHammerDurability(inputSlots, player));
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    protected void bclib_onTakeAnvilOutput(Player player, ItemStack stack, CallbackInfo info) {
        if (be_currentRecipe != null) {
            inputSlots.getItem(0).shrink(be_currentRecipe.getInputCount());
            stack = be_currentRecipe.craft(inputSlots, player);
            slotsChanged(inputSlots);
            access.execute((world, blockPos) -> {
                final BlockState anvilState = world.getBlockState(blockPos);
                final Block anvilBlock = anvilState.getBlock();
                if (anvilBlock instanceof BaseAnvilBlock) {
                    final BaseAnvilBlock anvil = (BaseAnvilBlock) anvilBlock;
                    if (!player.getAbilities().instabuild && anvilState.is(BlockTags.ANVIL) && player.getRandom()
                                                                                                     .nextDouble() < 0.1) {
                        BlockState damagedState = anvil.damageAnvilUse(anvilState, player.getRandom());
                        if (damagedState == null) {
                            world.removeBlock(blockPos, false);
                            world.levelEvent(1029, blockPos, 0);
                        } else {
                            world.setBlock(blockPos, damagedState, 2);
                            world.levelEvent(1030, blockPos, 0);
                        }
                    } else {
                        world.levelEvent(1030, blockPos, 0);
                    }
                }
            });
            info.cancel();
            return;
        }

        this.access.execute((level, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.getBlock() instanceof BaseAnvilBlock) {
                info.cancel();
                if (!player.getAbilities().instabuild) {
                    player.giveExperienceLevels(-this.cost.get());
                }

                this.inputSlots.setItem(0, ItemStack.EMPTY);
                if (this.repairItemCountCost > 0) {
                    ItemStack itemStack2 = this.inputSlots.getItem(1);
                    if (!itemStack2.isEmpty() && itemStack2.getCount() > this.repairItemCountCost) {
                        itemStack2.shrink(this.repairItemCountCost);
                        this.inputSlots.setItem(1, itemStack2);
                    } else {
                        this.inputSlots.setItem(1, ItemStack.EMPTY);
                    }
                } else {
                    this.inputSlots.setItem(1, ItemStack.EMPTY);
                }

                this.cost.set(0);

                if (!player.getAbilities().instabuild && blockState.is(BlockTags.ANVIL) && player.getRandom()
                                                                                                 .nextFloat() < 0.12F) {
                    BaseAnvilBlock anvil = (BaseAnvilBlock) blockState.getBlock();
                    BlockState damaged = anvil.damageAnvilUse(blockState, player.getRandom());
                    if (damaged == null) {
                        level.removeBlock(blockPos, false);
                        level.levelEvent(1029, blockPos, 0);
                    } else {
                        level.setBlock(blockPos, damaged, 2);
                        level.levelEvent(1030, blockPos, 0);
                    }
                } else {
                    level.levelEvent(1030, blockPos, 0);
                }
            }
        });
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void be_updateOutput(CallbackInfo info) {
        RecipeManager recipeManager = this.player.level.getRecipeManager();
        be_recipes = recipeManager.getRecipesFor(AnvilRecipe.TYPE, inputSlots, player.level);
        if (be_recipes.size() > 0) {
            int anvilLevel = this.anvilLevel.get();
            be_recipes = be_recipes.stream()
                                   .filter(recipe -> anvilLevel >= recipe.getAnvilLevel())
                                   .collect(Collectors.toList());
            if (be_recipes.size() > 0) {
                if (be_currentRecipe == null || !be_recipes.contains(be_currentRecipe)) {
                    be_currentRecipe = be_recipes.get(0);
                }
                be_updateResult();
                info.cancel();
            } else {
                be_currentRecipe = null;
            }
        }
    }

    @Inject(method = "setItemName", at = @At("HEAD"), cancellable = true)
    public void be_setNewItemName(String string, CallbackInfo info) {
        if (be_currentRecipe != null) {
            info.cancel();
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

    private void be_updateResult() {
        if (be_currentRecipe == null) return;
        resultSlots.setItem(0, be_currentRecipe.assemble(inputSlots));
        broadcastChanges();
    }

    @Override
    public void be_updateCurrentRecipe(AnvilRecipe recipe) {
        this.be_currentRecipe = recipe;
        be_updateResult();
    }

    @Override
    public AnvilRecipe be_getCurrentRecipe() {
        return be_currentRecipe;
    }

    @Override
    public List<AnvilRecipe> be_getRecipes() {
        return be_recipes;
    }
}
