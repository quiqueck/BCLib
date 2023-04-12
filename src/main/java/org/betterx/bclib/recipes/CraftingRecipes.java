package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class CraftingRecipes {
    public static void init() {
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_smith_table"), Blocks.SMITHING_TABLE)
                        .setShape("II", "##", "##")
                        .addMaterial('#', ItemTags.PLANKS)
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .setCategory(RecipeCategory.DECORATIONS)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_cauldron"), Blocks.CAULDRON)
                        .setShape("I I", "I I", "III")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .setCategory(RecipeCategory.BREWING)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_hopper"), Blocks.HOPPER)
                        .setShape("I I", "ICI", " I ")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .addMaterial('C', CommonItemTags.CHEST)
                        .setCategory(RecipeCategory.REDSTONE)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_piston"), Blocks.PISTON)
                        .setShape("WWW", "CIC", "CDC")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .addMaterial('D', Items.REDSTONE)
                        .addMaterial('C', Items.COBBLESTONE)
                        .addMaterial('W', ItemTags.PLANKS)
                        .setCategory(RecipeCategory.REDSTONE)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_rail"), Blocks.RAIL)
                        .setOutputCount(16)
                        .setShape("I I", "ISI", "I I")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .addMaterial('S', Items.STICK)
                        .setCategory(RecipeCategory.TRANSPORTATION)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_stonecutter"), Blocks.STONECUTTER)
                        .setShape(" I ", "SSS")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .addMaterial('S', Items.STONE)
                        .setCategory(RecipeCategory.DECORATIONS)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_compass"), Items.COMPASS)
                        .setShape(" I ", "IDI", " I ")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .addMaterial('D', Items.REDSTONE)
                        .setCategory(RecipeCategory.TOOLS)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_bucket"), Items.BUCKET)
                        .setShape("I I", " I ")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .setCategory(RecipeCategory.MISC)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_minecart"), Items.MINECART)
                        .setShape("I I", "III")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .setCategory(RecipeCategory.TRANSPORTATION)
                        .build();
        BCLRecipeBuilder.crafting(BCLib.makeID("tag_shield"), Items.SHIELD)
                        .setShape("WIW", "WWW", " W ")
                        .addMaterial('I', CommonItemTags.IRON_INGOTS)
                        .addMaterial('W', ItemTags.PLANKS)
                        .setCategory(RecipeCategory.COMBAT)
                        .build();

        BCLRecipeBuilder.crafting(BCLib.makeID("tag_shulker_box"), Blocks.SHULKER_BOX)
                        .setShape("S", "C", "S")
                        .addMaterial('S', Items.SHULKER_SHELL)
                        .addMaterial('C', CommonItemTags.CHEST)
                        .setCategory(RecipeCategory.DECORATIONS)
                        .build();
    }
}
