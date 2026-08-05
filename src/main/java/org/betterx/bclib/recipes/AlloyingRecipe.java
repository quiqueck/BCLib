package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;
import org.betterx.bclib.interfaces.UnknownReceipBookCategory;
import org.betterx.bclib.util.ItemStackCodec;
import de.ambertation.wover.item.api.ItemStackHelper;
import de.ambertation.wover.recipe.api.BaseRecipeBuilder;
import de.ambertation.wover.recipe.api.BaseUnlockableRecipeBuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class AlloyingRecipe implements Recipe<AlloyingRecipeInput>, UnknownReceipBookCategory {
    public final static String GROUP = "alloying";

    public static final RecipeBookCategory ALLOYING_CATEGORY = BCLRecipeManager.registerCategory(BCLib.C.mk("alloying"));
    public final static RecipeType<AlloyingRecipe> TYPE = BCLRecipeManager.registerType(BCLib.MOD_ID, GROUP);
    public final static RecipeSerializer<AlloyingRecipe> SERIALIZER = BCLRecipeManager.registerSerializer(
            BCLib.MOD_ID,
            GROUP,
            new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC)
    );

    protected final RecipeType<? extends Recipe<AlloyingRecipeInput>> type;
    protected final Ingredient primaryInput;
    protected final Ingredient secondaryInput;
    protected final Item outputItem;
    protected final int outputCount;
    protected final String group;
    protected final float experience;
    protected final int smeltTime;
    private PlacementInfo placementInfo;

    private AlloyingRecipe(
            List<Ingredient> inputs,
            Optional<String> group,
            ItemStackCodec.ItemAndCount output,
            float experience,
            int smeltTime
    ) {
        this(
                group.orElse(""),
                !inputs.isEmpty() ? inputs.get(0) : null,
                inputs.size() > 1 ? inputs.get(1) : null,
                output.item(),
                output.count(),
                experience,
                smeltTime
        );
    }

    private AlloyingRecipe(
            @NotNull String group,
            Ingredient primaryInput,
            Ingredient secondaryInput,
            Item outputItem,
            int outputCount,
            float experience,
            int smeltTime
    ) {
        this.group = group;
        this.primaryInput = primaryInput;
        this.secondaryInput = secondaryInput;
        this.outputItem = outputItem;
        this.outputCount = outputCount;
        this.experience = experience;
        this.smeltTime = smeltTime;
        this.type = TYPE;
    }

    public float experience() {
        return this.experience;
    }

    public int getSmeltTime() {
        return this.smeltTime;
    }

    @VisibleForTesting
    public List<Optional<Ingredient>> getIngredients() {
        NonNullList<Optional<Ingredient>> defaultedList = NonNullList.create();
        defaultedList.add(Optional.of(primaryInput));
        defaultedList.add(Optional.of(secondaryInput));

        return defaultedList;
    }

    @Override
    public boolean matches(AlloyingRecipeInput inv, Level level) {
        return this.primaryInput.test(inv.getItem(0)) && this.secondaryInput.test(inv.getItem(1)) || this.primaryInput.test(
                inv.getItem(1)) && this.secondaryInput.test(inv.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(AlloyingRecipeInput recipeInput) {
        return ItemStackHelper.callItemStackSetupIfPossible(new ItemStack(outputItem, outputCount));
    }

    @Override
    public @NotNull String group() {
        return this.group == null ? "" : this.group;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<AlloyingRecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<AlloyingRecipeInput>> getType() {
        return this.type;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            if (this.secondaryInput != null)
                this.placementInfo = PlacementInfo.create(List.of(this.primaryInput, this.secondaryInput));
            else this.placementInfo = PlacementInfo.create(List.of(this.primaryInput));
        }

        return this.placementInfo;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return ALLOYING_CATEGORY;
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getToastSymbol() {
        return AlloyingRecipeWorkstation.getWorkstationIcon();
    }

    public interface Builder extends BaseRecipeBuilder<Builder>, BaseUnlockableRecipeBuilder<Builder> {
        Builder group(@Nullable String group);
        Builder outputCount(int count);

        Builder setInput(ItemLike primaryInput, ItemLike secondaryInput);
        Builder setInput(TagKey<Item> primaryInput, TagKey<Item> secondaryInput);
        Builder setExperience(float amount);
        Builder setSmeltTime(int time);

        static Builder create(Identifier id, ItemLike output) {
            return new BuilderImpl(id, output);
        }
    }

    public static class BuilderImpl extends BCLBaseRecipeBuilder<Builder, AlloyingRecipe> implements Builder {
        private BuilderImpl(Identifier id, ItemLike output) {
            super(id, output, true);
            this.experience = 0.0F;
            this.smeltTime = 350;
        }


        private float experience;
        private int smeltTime;


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
        protected void validate() {
            super.validate();

            if (smeltTime < 0) {
                throwIllegalStateException("Smelt-time for recipe {} most be positive!");
            }
        }

        @Override
        protected AlloyingRecipe createRecipe(
                de.ambertation.wover.recipe.api.RecipeBuilder.Context ctx
        ) {

            return new AlloyingRecipe(
                    group == null ? "" : group,
                    primaryInput.createIngredient(ctx),
                    secondaryInput.createIngredient(ctx),
                    outputItem,
                    outputCount,
                    experience,
                    smeltTime
            );
        }
    }

    public static class Serializer {
        public static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(Ingredient.CODEC)
                     .fieldOf("ingredients")
                     .forGetter(recipe -> List.of(recipe.primaryInput, recipe.secondaryInput)),
                Codec.STRING.lenientOptionalFieldOf("group")
                            .forGetter(recipe -> recipe.group == null || recipe.group.isEmpty()
                                    ? Optional.empty()
                                    : Optional.of(recipe.group)),
                ItemStackCodec.ItemAndCount.CODEC
                              .fieldOf("result")
                              .forGetter(recipe -> new ItemStackCodec.ItemAndCount(recipe.outputItem, recipe.outputCount)),
                Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(recipe -> recipe.experience),
                Codec.INT.optionalFieldOf("smelttime", 350).forGetter(recipe -> recipe.smeltTime)
        ).apply(instance, AlloyingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> STREAM_CODEC = StreamCodec.of(
                AlloyingRecipe.Serializer::toNetwork,
                AlloyingRecipe.Serializer::fromNetwork
        );

        public static @NotNull AlloyingRecipe fromNetwork(RegistryFriendlyByteBuf packetBuffer) {
            String group = packetBuffer.readUtf();
            Ingredient primary = Ingredient.CONTENTS_STREAM_CODEC.decode(packetBuffer);
            Ingredient secondary = Ingredient.CONTENTS_STREAM_CODEC.decode(packetBuffer);
            Item outputItem = ItemStackCodec.ITEM_STREAM_CODEC.decode(packetBuffer);
            int outputCount = packetBuffer.readVarInt();
            float experience = packetBuffer.readFloat();
            int smeltTime = packetBuffer.readVarInt();

            return new AlloyingRecipe(
                    group == null ? "" : group, primary, secondary, outputItem, outputCount, experience, smeltTime
            );
        }


        public static void toNetwork(RegistryFriendlyByteBuf packetBuffer, AlloyingRecipe recipe) {
            packetBuffer.writeUtf(recipe.group);
            Ingredient.CONTENTS_STREAM_CODEC.encode(packetBuffer, recipe.primaryInput);
            Ingredient.CONTENTS_STREAM_CODEC.encode(packetBuffer, recipe.secondaryInput);
            ItemStackCodec.ITEM_STREAM_CODEC.encode(packetBuffer, recipe.outputItem);
            packetBuffer.writeVarInt(recipe.outputCount);
            packetBuffer.writeFloat(recipe.experience);
            packetBuffer.writeVarInt(recipe.smeltTime);
        }
    }

    public static void register() {
        //we call this to make sure that TYPE is initialized
    }
}
