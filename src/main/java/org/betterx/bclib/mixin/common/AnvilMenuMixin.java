package org.betterx.bclib.mixin.common;

import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.AnvilRecipeInput;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Unique
    private List<RecipeHolder<AnvilRecipe>> bcl_recipes = Collections.emptyList();
    @Unique
    private RecipeHolder<AnvilRecipe> bcl_currentRecipe;


    @Unique
    private DataSlot bcl_anvilLevel;

    /**
     * Synced to the client, which has no access to the recipes themselves, so the
     * anvil screen can tell whether (and how many) BCL recipes are available.
     */
    @Unique
    private DataSlot bcl_recipeCount;

    public AnvilMenuMixin(
            @Nullable MenuType<?> menuType,
            int i,
            Inventory inventory,
            ContainerLevelAccess containerLevelAccess,
            ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition
    ) {
        super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
    }


    @Unique
    private AnvilRecipeInput bcl_AnvilRecipeInput(TagKey<Item> allowedTools) {
        return new AnvilRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1), allowedTools);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    public void be_initAnvilLevel(int syncId, Inventory inventory, ContainerLevelAccess context, CallbackInfo info) {
        this.bcl_anvilLevel = addDataSlot(DataSlot.standalone());
        this.bcl_recipeCount = addDataSlot(DataSlot.standalone());
        if (context != ContainerLevelAccess.NULL) {
            int level = context.evaluate(
                    (world, blockPos) -> {
                        Block anvilBlock = world.getBlockState(blockPos).getBlock();
                        return LeveledAnvilBlock.getAnvilCraftingLevel(anvilBlock);
                    }, 0
            );
            bcl_anvilLevel.set(level);
        } else {
            bcl_anvilLevel.set(0);
        }
    }

    @Shadow
    public abstract void createResult();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    protected void bcl_canTakeOutput(Player player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (bcl_currentRecipe != null) {
            AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(bcl_currentRecipe.value().getAllowedTools());
            info.setReturnValue(bcl_currentRecipe.value().checkHammerDurability(recipeInput, player));
        }
    }

    @Inject(method = "method_24922", at = @At(value = "HEAD"), cancellable = true)
    private static void bcl_onDamageAnvil(Player player, Level level, BlockPos blockPos, CallbackInfo ci) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!player.getAbilities().instabuild
                && blockState.getBlock() instanceof LeveledAnvilBlock anvil
                && player.getRandom().nextDouble() < 0.12) {
            BlockState damaged = anvil.damageAnvilUse(blockState);
            LeveledAnvilBlock.destroyWhenNull(level, blockPos, damaged);
            ci.cancel();
        }
    }


    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    protected void bcl_onTakeAnvilOutput(Player player, ItemStack stack, CallbackInfo info) {
        if (bcl_currentRecipe != null) {
            AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(bcl_currentRecipe.value().getAllowedTools());
            recipeInput.getIngredient().shrink(bcl_currentRecipe.value().getInputCount());
            stack = bcl_currentRecipe.value().craft(recipeInput, player);
            slotsChanged(inputSlots);

            access.execute((level, blockPos) -> {
                final BlockState anvilState = level.getBlockState(blockPos);
                final Block anvilBlock = anvilState.getBlock();
                if (anvilBlock instanceof LeveledAnvilBlock anvil) {
                    if (!player.getAbilities().instabuild
                            && anvilState.is(BlockTags.ANVIL)
                            && player.getRandom().nextDouble() < 0.1) {
                        BlockState damagedState = anvil.damageAnvilUse(anvilState);
                        LeveledAnvilBlock.destroyWhenNull(level, blockPos, damagedState);
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
        if (this.player.level() instanceof ServerLevel level) {
            final AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(CommonItemTags.HAMMERS);
            final int anvilLevel = this.bcl_anvilLevel.get();
            bcl_recipes = level.recipeAccess()
                               .getAllMatches(AnvilRecipe.TYPE, recipeInput, level)
                               .filter(recipe -> anvilLevel >= recipe.value().getAnvilLevel())
                               .collect(Collectors.toList());
            bcl_recipeCount.set(bcl_recipes.size());
            if (!bcl_recipes.isEmpty()) {
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

    @Unique
    private void bcl_updateResult() {
        if (bcl_currentRecipe == null) return;
        AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(bcl_currentRecipe.value().getAllowedTools());
        resultSlots.setItem(0, bcl_currentRecipe.value().assemble(recipeInput, this.player.level().registryAccess()));
        broadcastChanges();
    }

    @Override
    public void bcl_updateCurrentRecipe(RecipeHolder<AnvilRecipe> recipe) {
        this.bcl_currentRecipe = recipe;
        bcl_updateResult();
    }

    @Override
    public RecipeHolder<AnvilRecipe> bcl_getCurrentRecipe() {
        return bcl_currentRecipe;
    }

    @Override
    public List<RecipeHolder<AnvilRecipe>> bcl_getRecipes() {
        return bcl_recipes;
    }

    @Override
    public int bcl_getRecipeCount() {
        return bcl_recipeCount == null ? 0 : bcl_recipeCount.get();
    }
}
