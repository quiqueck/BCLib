package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.UnknownReceipBookCategory;
import org.betterx.bclib.util.ItemUtil;
import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.gson.JsonObject;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class AnvilRecipe implements Recipe<Container>, UnknownReceipBookCategory {
    public final static String GROUP = "smithing";
    public final static RecipeType<AnvilRecipe> TYPE = BCLRecipeManager.registerType(BCLib.MOD_ID, GROUP);
    public final static Serializer SERIALIZER = BCLRecipeManager.registerSerializer(
            BCLib.MOD_ID,
            GROUP,
            new Serializer()
    );
    public final static ResourceLocation ID = BCLib.makeID(GROUP);


    public static void register() {
        //we call this to make sure that TYPE is initialized
    }

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int damage;
    private final int toolLevel;
    private final int anvilLevel;
    private final int inputCount;

    public AnvilRecipe(
            ResourceLocation identifier,
            Ingredient input,
            ItemStack output,
            int inputCount,
            int toolLevel,
            int anvilLevel,
            int damage
    ) {
        this.id = identifier;
        this.input = input;
        this.output = output;
        this.toolLevel = toolLevel;
        this.anvilLevel = anvilLevel;
        this.inputCount = inputCount;
        this.damage = damage;
    }

    static Builder create(ResourceLocation id, ItemLike output) {
        return new Builder(id, output);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess acc) {
        return this.output;
    }

    @Override
    public boolean matches(@NotNull Container craftingInventory, @NotNull Level world) {
        return this.matches(craftingInventory);
    }

    @Override
    public ItemStack assemble(@NotNull Container craftingInventory, RegistryAccess acc) {
        return this.output.copy();
    }

    public static Iterable<Holder<Item>> getAllHammers() {
        Registry<Item> registry = WorldBootstrap.getLastRegistryAccessOrElseBuiltin()
                                                .registryOrThrow(CommonItemTags.HAMMERS.registry());
        return registry.getTagOrEmpty(CommonItemTags.HAMMERS);
    }

    public static int getHammerSlot(Container c) {
        ItemStack h = c.getItem(0);
        if (!h.isEmpty() && h.is(CommonItemTags.HAMMERS)) return 0;

        //this is the default slot
        return 1;
    }

    public static int getIngredientSlot(Container c) {
        return Math.abs(getHammerSlot(c) - 1);
    }

    public ItemStack getHammer(Container c) {
        ItemStack h = c.getItem(1);
        if (!h.isEmpty() && h.is(CommonItemTags.HAMMERS)) return h;
        h = c.getItem(0);
        if (!h.isEmpty() && h.is(CommonItemTags.HAMMERS)) return h;
        return null;
    }

    public ItemStack getIngredient(Container c) {
        ItemStack i = c.getItem(0);
        if (i.is(CommonItemTags.HAMMERS)) i = c.getItem(1);
        return i;
    }

    public ItemStack craft(Container craftingInventory, Player player) {
        if (!player.isCreative()) {
            if (!checkHammerDurability(craftingInventory, player)) return ItemStack.EMPTY;
            ItemStack hammer = getHammer(craftingInventory);
            if (hammer != null) {
                hammer.hurtAndBreak(this.damage, player, entity -> entity.broadcastBreakEvent((InteractionHand) null));
                return ItemStack.EMPTY;
            }
        }
        return this.assemble(craftingInventory, Minecraft.getInstance().level.registryAccess());
    }

    public boolean checkHammerDurability(Container craftingInventory, Player player) {
        if (player.isCreative()) return true;
        ItemStack hammer = getHammer(craftingInventory);
        if (hammer != null) {
            int damage = hammer.getDamageValue() + this.damage;
            return damage < hammer.getMaxDamage();
        }
        return true;
    }

    public boolean matches(Container craftingInventory) {
        ItemStack hammer = getHammer(craftingInventory);
        if (hammer == null) {
            return false;
        }
        ItemStack material = getIngredient(craftingInventory);
        int materialCount = material.getCount();
        int level = ((TieredItem) hammer.getItem()).getTier().getLevel();
        return this.input.test(getIngredient(craftingInventory)) && materialCount >= this.inputCount && level >= this.toolLevel;
    }

    public int getDamage() {
        return this.damage;
    }

    public int getInputCount() {
        return this.inputCount;
    }

    public Ingredient getMainIngredient() {
        return this.input;
    }

    public int getAnvilLevel() {
        return this.anvilLevel;
    }

    public boolean canUse(Item tool) {
        if (tool instanceof TieredItem ti) {
            return ti.getTier().getLevel() >= toolLevel;
        }
        return false;
    }

    public static boolean isHammer(Item tool) {
        if (tool == null) return false;
        return tool.getDefaultInstance().is(CommonItemTags.HAMMERS);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> defaultedList = NonNullList.create();
        defaultedList.add(Ingredient.of(BuiltInRegistries.ITEM.stream()
                                                              .filter(AnvilRecipe::isHammer)
                                                              .filter(this::canUse)
                                                              .map(ItemStack::new))
        );
        defaultedList.add(input);
        return defaultedList;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnvilRecipe that = (AnvilRecipe) o;
        return damage == that.damage && toolLevel == that.toolLevel && id.equals(that.id) && input.equals(that.input) && output.equals(
                that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, input, output, damage, toolLevel);
    }

    @Override
    public String toString() {
        return "AnvilRecipe [" + id + "]";
    }

    public static class Builder extends AbstractSingleInputRecipeBuilder<Builder, AnvilRecipe> {
        private int inputCount;
        private int toolLevel;
        private int anvilLevel;
        private int damage;

        protected Builder(ResourceLocation id, ItemLike output) {
            super(id, output);

            this.inputCount = 1;
            this.toolLevel = 1;
            this.anvilLevel = 1;
            this.damage = 1;
        }

        @Override
        protected Builder setOutputTag(CompoundTag tag) {
            return super.setOutputTag(tag);
        }

        @Override
        protected Builder setOutputCount(int count) {
            return super.setOutputCount(count);
        }

        /**
         * @param inputItems
         * @return
         * @deprecated Use {@link #setPrimaryInput(ItemLike...)} instead
         */
        @Deprecated(forRemoval = true)
        public Builder setInput(ItemLike... inputItems) {
            return super.setPrimaryInput(inputItems);
        }

        /**
         * @param inputTag
         * @return
         * @deprecated Use {@link #setPrimaryInput(TagKey)} instead
         */
        @Deprecated(forRemoval = true)
        public Builder setInput(TagKey<Item> inputTag) {
            return super.setPrimaryInput(inputTag);
        }

        @Deprecated(forRemoval = true)
        public Builder setInput(Ingredient ingredient) {
            this.primaryInput = ingredient;
            return this;
        }

        public Builder setInputCount(int count) {
            this.inputCount = count;
            return this;
        }


        public Builder setToolLevel(int level) {
            this.toolLevel = level;
            return this;
        }

        public Builder setAnvilLevel(int level) {
            this.anvilLevel = level;
            return this;
        }

        public Builder setDamage(int damage) {
            this.damage = damage;
            return this;
        }

        @Override
        protected RecipeSerializer<AnvilRecipe> getSerializer() {
            return SERIALIZER;
        }

        @Override
        protected boolean checkRecipe() {
            if (inputCount <= 0) {
                BCLib.LOGGER.warning(
                        "Number of input items for Recipe must be positive. Recipe {} will be ignored!",
                        id
                );
                return false;
            }
            return super.checkRecipe();
        }

        @Override
        protected void serializeRecipeData(JsonObject root) {
            super.serializeRecipeData(root);

            if (inputCount > 1) {
                root.addProperty("inputCount", inputCount);
            }
            if (toolLevel != 1) {
                root.addProperty("toolLevel", toolLevel);
            }
            if (anvilLevel != 1) {
                root.addProperty("anvilLevel", anvilLevel);
            }
            if (damage != 1) {
                root.addProperty("damage", damage);
            }
        }
    }

    public static class Serializer implements RecipeSerializer<AnvilRecipe> {
        @Override
        public AnvilRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(json.get("input"));
            JsonObject result = GsonHelper.getAsJsonObject(json, "result");
            ItemStack output = ItemUtil.fromJsonRecipeWithNBT(result);
            if (output == null) {
                throw new IllegalStateException("Output item does not exists!");
            }

            int inputCount = GsonHelper.getAsInt(json, "inputCount", 1);
            int toolLevel = GsonHelper.getAsInt(json, "toolLevel", 1);
            int anvilLevel = GsonHelper.getAsInt(json, "anvilLevel", 1);
            int damage = GsonHelper.getAsInt(json, "damage", 1);

            return new AnvilRecipe(id, input, output, inputCount, toolLevel, anvilLevel, damage);
        }

        @Override
        public AnvilRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf packetBuffer) {
            Ingredient input = Ingredient.fromNetwork(packetBuffer);
            ItemStack output = packetBuffer.readItem();
            int inputCount = packetBuffer.readVarInt();
            int toolLevel = packetBuffer.readVarInt();
            int anvilLevel = packetBuffer.readVarInt();
            int damage = packetBuffer.readVarInt();

            return new AnvilRecipe(id, input, output, inputCount, toolLevel, anvilLevel, damage);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetBuffer, AnvilRecipe recipe) {
            recipe.input.toNetwork(packetBuffer);
            packetBuffer.writeItem(recipe.output);
            packetBuffer.writeVarInt(recipe.inputCount);
            packetBuffer.writeVarInt(recipe.toolLevel);
            packetBuffer.writeVarInt(recipe.anvilLevel);
            packetBuffer.writeVarInt(recipe.damage);
        }
    }
}
