package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;
import org.betterx.bclib.interfaces.UnknownReceipBookCategory;
import org.betterx.bclib.util.ItemUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AlloyingRecipe implements Recipe<Container>, UnknownReceipBookCategory {
    public final static String GROUP = "alloying";
    public final static RecipeType<AlloyingRecipe> TYPE = BCLRecipeManager.registerType(BCLib.MOD_ID, GROUP);
    public final static Serializer SERIALIZER = BCLRecipeManager.registerSerializer(
            BCLib.MOD_ID,
            GROUP,
            new Serializer()
    );

    protected final RecipeType<?> type;
    protected final ResourceLocation id;
    protected final Ingredient primaryInput;
    protected final Ingredient secondaryInput;
    protected final ItemStack output;
    protected final String group;
    protected final float experience;
    protected final int smeltTime;

    public AlloyingRecipe(
            ResourceLocation id,
            String group,
            Ingredient primaryInput,
            Ingredient secondaryInput,
            ItemStack output,
            float experience,
            int smeltTime
    ) {
        this.group = group;
        this.id = id;
        this.primaryInput = primaryInput;
        this.secondaryInput = secondaryInput;
        this.output = output;
        this.experience = experience;
        this.smeltTime = smeltTime;
        this.type = TYPE;
    }

    public float getExperience() {
        return this.experience;
    }

    public int getSmeltTime() {
        return this.smeltTime;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> defaultedList = NonNullList.create();
        defaultedList.add(primaryInput);
        defaultedList.add(secondaryInput);

        return defaultedList;
    }

    @Override
    public boolean matches(Container inv, Level world) {
        return this.primaryInput.test(inv.getItem(0)) && this.secondaryInput.test(inv.getItem(1)) || this.primaryInput.test(
                inv.getItem(1)) && this.secondaryInput.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess acc) {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getGroup() {
        return this.group;
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getToastSymbol() {
        return AlloyingRecipeWorkstation.getWorkstationIcon();
    }

    public static class Builder extends AbstractDoubleInputRecipeBuilder<Builder, AlloyingRecipe> {
        private Builder(ResourceLocation id, ItemLike output) {
            super(id, output);
            this.experience = 0.0F;
            this.smeltTime = 350;
        }

        static Builder create(ResourceLocation id, ItemLike output) {
            return new Builder(id, output);
        }

        private float experience;
        private int smeltTime;


        @Override
        public Builder setOutputCount(int count) {
            return super.setOutputCount(count);
        }

        @Override
        public Builder setOutputTag(CompoundTag tag) {
            return super.setOutputTag(tag);
        }

        public Builder setInput(ItemLike primaryInput, ItemLike secondaryInput) {
            this.setPrimaryInput(primaryInput);
            this.setSecondaryInput(secondaryInput);
            return this;
        }

        public Builder setInput(TagKey<Item> primaryInput, TagKey<Item> secondaryInput) {
            this.setPrimaryInput(primaryInput);
            this.setSecondaryInput(secondaryInput);
            return this;
        }

        public Builder setExperience(float amount) {
            this.experience = amount;
            return this;
        }

        public Builder setSmeltTime(int time) {
            this.smeltTime = time;
            return this;
        }

        @Override
        public Builder setGroup(String group) {
            return super.setGroup(group);
        }

        @Override
        protected boolean checkRecipe() {
            if (smeltTime < 0) {
                BCLib.LOGGER.warning("Semelt-time for recipe {} most be positive!", id);
                return false;
            }
            return super.checkRecipe();
        }

        @Override
        protected RecipeSerializer<AlloyingRecipe> getSerializer() {
            return SERIALIZER;
        }

        @Override
        protected void serializeRecipeData(JsonObject root) {
            super.serializeRecipeData(root);

            if (experience != 0) {
                root.addProperty("experience", experience);
            }
            if (experience != 350) {
                root.addProperty("smelttime", smeltTime);
            }
        }
    }

    public static class Serializer implements RecipeSerializer<AlloyingRecipe> {
        @Override
        public AlloyingRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            Ingredient primaryInput = Ingredient.fromJson(ingredients.get(0));
            Ingredient secondaryInput = Ingredient.fromJson(ingredients.get(1));

            String group = GsonHelper.getAsString(json, "group", "");

            JsonObject result = GsonHelper.getAsJsonObject(json, "result");
            ItemStack output = ItemUtil.fromJsonRecipeWithNBT(result);
            if (output == null) {
                throw new IllegalStateException("Output item does not exists!");
            }
            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int smeltTime = GsonHelper.getAsInt(json, "smelttime", 350);

            return new AlloyingRecipe(id, group, primaryInput, secondaryInput, output, experience, smeltTime);
        }

        @Override
        public AlloyingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf packetBuffer) {
            String group = packetBuffer.readUtf(32767);
            Ingredient primary = Ingredient.fromNetwork(packetBuffer);
            Ingredient secondary = Ingredient.fromNetwork(packetBuffer);
            ItemStack output = packetBuffer.readItem();
            float experience = packetBuffer.readFloat();
            int smeltTime = packetBuffer.readVarInt();

            return new AlloyingRecipe(id, group, primary, secondary, output, experience, smeltTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetBuffer, AlloyingRecipe recipe) {
            packetBuffer.writeUtf(recipe.group);
            recipe.primaryInput.toNetwork(packetBuffer);
            recipe.secondaryInput.toNetwork(packetBuffer);
            packetBuffer.writeItem(recipe.output);
            packetBuffer.writeFloat(recipe.experience);
            packetBuffer.writeVarInt(recipe.smeltTime);
        }
    }

    public static void register() {
        //we call this to make sure that TYPE is initialized
    }
}
