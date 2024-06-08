package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;
import org.betterx.bclib.interfaces.UnknownReceipBookCategory;
import org.betterx.bclib.util.ItemUtil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class AlloyingRecipe implements Recipe<AlloyingRecipeInput>, UnknownReceipBookCategory {
    public final static String GROUP = "alloying";
    public final static RecipeType<AlloyingRecipe> TYPE = BCLRecipeManager.registerType(BCLib.MOD_ID, GROUP);
    public final static Serializer SERIALIZER = BCLRecipeManager.registerSerializer(
            BCLib.MOD_ID,
            GROUP,
            new Serializer()
    );

    protected final RecipeType<?> type;
    protected final Ingredient primaryInput;
    protected final Ingredient secondaryInput;
    protected final ItemStack output;
    protected final String group;
    protected final float experience;
    protected final int smeltTime;

    private AlloyingRecipe(
            List<Ingredient> inputs,
            Optional<String> group,
            ItemStack output,
            float experience,
            int smeltTime
    ) {
        this(
                group.orElse(""),
                !inputs.isEmpty() ? inputs.get(0) : null,
                inputs.size() > 1 ? inputs.get(1) : null,
                output,
                experience,
                smeltTime
        );
    }

    private AlloyingRecipe(
            @NotNull String group,
            Ingredient primaryInput,
            Ingredient secondaryInput,
            ItemStack output,
            float experience,
            int smeltTime
    ) {
        this.group = group;
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
    public boolean matches(AlloyingRecipeInput inv, Level level) {
        return this.primaryInput.test(inv.getItem(0)) && this.secondaryInput.test(inv.getItem(1)) || this.primaryInput.test(
                inv.getItem(1)) && this.secondaryInput.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(AlloyingRecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
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
                BCLib.LOGGER.warn("Semelt-time for recipe {} most be positive!", id);
                return false;
            }
            return super.checkRecipe();
        }

        @Override
        protected RecipeSerializer<AlloyingRecipe> getSerializer() {
            return SERIALIZER;
        }

        @Override
        protected AlloyingRecipe createRecipe(ResourceLocation id) {
            checkRecipe();
            return new AlloyingRecipe(
                    group == null ? "" : group,
                    primaryInput,
                    secondaryInput,
                    output,
                    experience,
                    smeltTime
            );
        }
    }

    public static class Serializer implements RecipeSerializer<AlloyingRecipe> {
        public static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(Ingredient.CODEC_NONEMPTY)
                     .fieldOf("ingredients")
                     .forGetter(recipe -> List.of(recipe.primaryInput, recipe.secondaryInput)),
                Codec.STRING.lenientOptionalFieldOf("group")
                            .forGetter(recipe -> recipe.group == null || recipe.group.isEmpty()
                                    ? Optional.empty()
                                    : Optional.ofNullable(recipe.group)),
                ItemUtil.CODEC_ITEM_STACK_WITH_NBT.fieldOf("result").forGetter(recipe -> recipe.output),
                Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(recipe -> recipe.experience),
                Codec.INT.optionalFieldOf("smelttime", 350).forGetter(recipe -> recipe.smeltTime)
        ).apply(instance, AlloyingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> STREAM_CODEC = StreamCodec.of(AlloyingRecipe.Serializer::toNetwork, AlloyingRecipe.Serializer::fromNetwork);

        @Override
        public @NotNull MapCodec<AlloyingRecipe> codec() {
            return CODEC;
        }


        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() {
            return null;
        }

        public static @NotNull AlloyingRecipe fromNetwork(RegistryFriendlyByteBuf packetBuffer) {
            String group = packetBuffer.readUtf();
            Ingredient primary = Ingredient.CONTENTS_STREAM_CODEC.decode(packetBuffer);
            Ingredient secondary = Ingredient.CONTENTS_STREAM_CODEC.decode(packetBuffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(packetBuffer);
            float experience = packetBuffer.readFloat();
            int smeltTime = packetBuffer.readVarInt();

            return new AlloyingRecipe(group == null ? "" : group, primary, secondary, output, experience, smeltTime);
        }


        public static void toNetwork(RegistryFriendlyByteBuf packetBuffer, AlloyingRecipe recipe) {
            packetBuffer.writeUtf(recipe.group);
            Ingredient.CONTENTS_STREAM_CODEC.encode(packetBuffer, recipe.primaryInput);
            Ingredient.CONTENTS_STREAM_CODEC.encode(packetBuffer, recipe.secondaryInput);
            ItemStack.STREAM_CODEC.encode(packetBuffer, recipe.output);
            packetBuffer.writeFloat(recipe.experience);
            packetBuffer.writeVarInt(recipe.smeltTime);
        }
    }

    public static void register() {
        //we call this to make sure that TYPE is initialized
    }
}
