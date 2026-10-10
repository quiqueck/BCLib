package org.betterx.bclib.integration.jei;

import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.RecipeViewerStations;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;

import java.util.List;

/**
 * BCLib {@link AnvilRecipe}s: material (with the count one craft consumes - the anvil picks a different
 * result for 1 or 3 ingots) and hammer in, part out, plus the anvils that are good enough for the
 * recipe's level and the durability the hammer loses.
 */
@Environment(EnvType.CLIENT)
public class AnvilCategory extends AbstractRecipeCategory<RecipeHolder<AnvilRecipe>> {
    public static final IRecipeHolderType<AnvilRecipe> TYPE = IRecipeHolderType.create(AnvilRecipe.TYPE);

    private static final int WIDTH = 112;
    private static final int HEIGHT = 48;
    private static final int TEXT_COLOR = 0xFF808080;
    private static final String[] ROMAN = {"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    public AnvilCategory(IGuiHelper guiHelper, ItemLike icon) {
        super(
                TYPE,
                Component.translatable("category.bclib.jei.anvil"),
                guiHelper.createDrawableItemLike(icon),
                WIDTH,
                HEIGHT
        );
    }

    static String levelName(int level) {
        return level >= 0 && level < ROMAN.length ? ROMAN[level] : Integer.toString(level);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AnvilRecipe> holder, IFocusGroup focuses) {
        final AnvilRecipe recipe = holder.value();
        final int inputCount = recipe.getInputCount();
        final Level level = Minecraft.getInstance().level;

        // the stack size is what the slot shows, so 1 vs. 3 ingots is visible without hovering
        final List<ItemStack> materials = recipe.getMainIngredient()
                                                .items()
                                                .map(item -> new ItemStack(item, inputCount))
                                                .toList();
        builder.addInputSlot(1, 5)
               .setStandardSlotBackground()
               .addItemStacks(materials)
               .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                       Component.translatable("category.bclib.jei.anvil.input_count", inputCount)
               ));

        final List<Item> hammers = RecipeViewerStations.hammersFor(
                recipe,
                level == null ? null : level.registryAccess()
        );
        builder.addInputSlot(21, 5)
               .setStandardSlotBackground()
               .addItemStacks(hammers.stream().map(ItemStack::new).toList())
               .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                       Component.translatable("category.bclib.jei.anvil.damage", recipe.getDamage())
               ));

        builder.addOutputSlot(WIDTH - 22, 5)
               .setOutputSlotBackground()
               .add(recipe.assemble(null));

        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 1, 29)
               .setStandardSlotBackground()
               .addItemStacks(RecipeViewerStations.anvilsFor(recipe.getAnvilLevel())
                                                  .stream()
                                                  .map(ItemStack::new)
                                                  .toList());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<AnvilRecipe> holder, IFocusGroup focuses) {
        final AnvilRecipe recipe = holder.value();

        builder.addRecipeArrow().setPosition(50, 5);
        builder.addText(
                List.of(
                        Component.translatable("category.bclib.jei.anvil.level", levelName(recipe.getAnvilLevel())),
                        Component.translatable("category.bclib.jei.anvil.damage", recipe.getDamage())
                ),
                WIDTH - 22, 20
        ).setPosition(22, 28).setColor(TEXT_COLOR);
    }

    @Override
    public Codec<RecipeHolder<AnvilRecipe>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return codecHelper.getRecipeHolderCodec();
    }
}
