package org.betterx.bclib.api.v2.advancement;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.structures.BCLStructure;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.set.wood.WoodSlots;
import org.betterx.bclib.items.complex.EquipmentSet;
import org.betterx.bclib.items.complex.EquipmentSlot;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AdvancementManager {
    static class OrderedBuilder extends Advancement.Builder {
        OrderedBuilder() {
            super(false);
        }
    }

    private static final Map<ResourceLocation, Advancement.Builder> ADVANCEMENTS = new LinkedHashMap<>();

    public static void register(ResourceLocation id, Advancement.Builder builder) {
        ADVANCEMENTS.put(id, builder);
    }

    public static void registerAllDataGen(List<String> namespaces, Consumer<Advancement> consumer) {
        final Advancement ROOT_RECIPE = Advancement.Builder.advancement()
                                                           .addCriterion(
                                                                   "impossible",
                                                                   new ImpossibleTrigger.TriggerInstance()
                                                           )
                                                           .build(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        final Map<ResourceLocation, Advancement> BUILT = new HashMap<>();

        for (var entry : ADVANCEMENTS.entrySet()) {
            final ResourceLocation loc = entry.getKey();
            if (namespaces == null || namespaces.contains(loc.getNamespace())) {
                final Advancement.Builder builder = entry.getValue();
                if (builder.canBuild(locToAdd -> {
                    if (locToAdd.equals(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)) return ROOT_RECIPE;
                    return BUILT.get(locToAdd);
                })) {
                    final Advancement adv = builder.build(loc);
                    BUILT.put(loc, adv);
                    consumer.accept(adv);
                } else {
                    BCLib.LOGGER.error("Unable to build Advancement " + loc);
                }
            }
        }
    }


    public static class RewardsBuilder {
        private final Builder calle;
        private final AdvancementRewards.Builder builder = new AdvancementRewards.Builder();

        private RewardsBuilder(Builder calle) {
            this.calle = calle;
        }

        public RewardsBuilder addExperience(int i) {
            builder.addExperience(i);
            return this;
        }


        public RewardsBuilder addLootTable(ResourceLocation resourceLocation) {
            builder.addLootTable(resourceLocation);
            return this;
        }


        public RewardsBuilder addRecipe(ResourceLocation resourceLocation) {
            builder.addRecipe(resourceLocation);
            return this;
        }


        public RewardsBuilder runs(ResourceLocation resourceLocation) {
            builder.runs(resourceLocation);
            return this;
        }

        public Builder endReward() {
            calle.rewards(builder.build());
            return calle;
        }
    }

    public enum AdvancementType {
        REGULAR,
        RECIPE_DECORATIONS,
        RECIPE_TOOL
    }

    public static class Builder {
        private static final ThreadLocal<DisplayBuilder> DISPLAY_BUILDER = ThreadLocal.withInitial(DisplayBuilder::new);
        private static final ResourceLocation RECIPES_ROOT = RecipeBuilder.ROOT_RECIPE_ADVANCEMENT;

        private final Advancement.Builder builder = new OrderedBuilder();
        private final ResourceLocation id;
        private final AdvancementType type;
        private boolean canBuild = true;

        private Builder(ResourceLocation id, AdvancementType type) {
            ResourceLocation ID;
            if (type == AdvancementType.RECIPE_DECORATIONS) {
                ID = new ResourceLocation(id.getNamespace(), "recipes/decorations/" + id.getPath());
                builder.parent(RECIPES_ROOT);
            } else if (type == AdvancementType.RECIPE_TOOL) {
                ID = new ResourceLocation(id.getNamespace(), "recipes/tools/" + id.getPath());
                builder.parent(RECIPES_ROOT);
            } else {
                ID = id;
            }
            this.id = ID;
            this.type = type;
        }

        public static Builder createEmptyCopy(Builder builder) {
            return new Builder(builder.id, builder.type);
        }

        public static Builder create(ResourceLocation id) {
            return new Builder(id, AdvancementType.REGULAR);
        }

        public static Builder create(ResourceLocation id, AdvancementType type) {
            return new Builder(id, type);
        }

        public static Builder create(Item icon) {
            return create(icon, AdvancementType.REGULAR);
        }

        public static Builder create(ItemStack icon) {
            return create(icon, AdvancementType.REGULAR);
        }

        public static Builder create(ItemLike icon, AdvancementType type) {
            return create(new ItemStack(icon), type);
        }

        public static Builder create(ItemStack icon, AdvancementType type) {
            return create(icon, type, (displayBuilder) -> {
            });
        }

        public static Builder create(Item icon, AdvancementType type, Consumer<DisplayBuilder> displayAdapter) {
            return create(new ItemStack(icon), type, displayAdapter);
        }

        public static Builder create(
                ItemStack icon,
                AdvancementType type,
                Consumer<DisplayBuilder> displayAdapter
        ) {
            var id = BuiltInRegistries.ITEM.getKey(icon.getItem());
            boolean canBuild = true;
            if (id == null || icon.is(Items.AIR)) {
                canBuild = false;
                id = BuiltInRegistries.ITEM.getDefaultKey();
            }

            String baseName = "advancements." + id.getNamespace() + "." + id.getPath() + ".";
            Builder b = new Builder(id, type);
            var displayBuilder = b.startDisplay(
                    icon,
                    Component.translatable(baseName + "title"),
                    Component.translatable(baseName + "description")
            );
            if (displayAdapter != null) displayAdapter.accept(displayBuilder);
            b = displayBuilder.endDisplay();
            b.canBuild = canBuild;
            return b;
        }

        public static <C extends Container, T extends Recipe<C>> Builder createRecipe(
                T recipe,
                AdvancementType type
        ) {
            Item item = recipe.getResultItem(Minecraft.getInstance().level.registryAccess()).getItem();
            return create(item, type, displayBuilder -> displayBuilder.hideToast().hideFromChat())
                    //.awardRecipe(item)
                    .addRecipeUnlockCriterion("has_the_recipe", recipe)
                    .startReward()
                    .addRecipe(recipe.getId())
                    .endReward()
                    .requirements(RequirementsStrategy.OR);
        }

        public Builder parent(Advancement advancement) {
            builder.parent(advancement);
            return this;
        }

        public Builder parent(ResourceLocation resourceLocation) {
            builder.parent(resourceLocation);
            return this;
        }

        public DisplayBuilder startDisplay(ItemLike icon) {
            String baseName = "advancements." + id.getNamespace() + "." + id.getPath() + ".";
            return startDisplay(
                    icon,
                    Component.translatable(baseName + "title"),
                    Component.translatable(baseName + "description")
            );
        }

        public DisplayBuilder startDisplay(
                ItemLike icon,
                Component title,
                Component description
        ) {
            return startDisplay(new ItemStack(icon), title, description);
        }

        public DisplayBuilder startDisplay(
                ItemStack icon,
                Component title,
                Component description
        ) {
            if (icon == null) {
                canBuild = false;
            } else {
                var id = BuiltInRegistries.ITEM.getKey(icon.getItem());
                if (id == null) {
                    canBuild = false;
                }
            }
            DisplayBuilder dp = DISPLAY_BUILDER.get().reset(this);
            return dp.icon(icon).title(title).description(description);
        }

        Builder display(DisplayInfo displayInfo) {
            builder.display(displayInfo);
            return this;
        }

        public Builder awardRecipe(ItemLike... items) {
            RewardsBuilder rewardBuilder = startReward();
            for (ItemLike item : items) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.asItem());
                if (id == null) continue;
                rewardBuilder.addRecipe(id);
            }
            return rewardBuilder.endReward();
        }

        public RewardsBuilder startReward() {
            return new RewardsBuilder(this);
        }

        public Builder rewards(AdvancementRewards advancementRewards) {
            builder.rewards(advancementRewards);
            return this;
        }

        public Builder rewardXP(int xp) {
            return rewards(AdvancementRewards.Builder.experience(500).build());
        }

        public Builder addCriterion(String string, CriterionTriggerInstance criterionTriggerInstance) {
            builder.addCriterion(string, new Criterion(criterionTriggerInstance));
            return this;
        }

        public Builder addCriterion(String string, Criterion criterion) {
            builder.addCriterion(string, criterion);
            return this;
        }

        public Builder addAtStructureCriterion(String name, BCLStructure<?> structure) {
            return addAtStructureCriterion(name, structure.structureKey);
        }

        public Builder addAtStructureCriterion(String name, ResourceKey<Structure> structure) {
            return addCriterion(
                    name,
                    PlayerTrigger
                            .TriggerInstance
                            .located(
                                    LocationPredicate.inStructure(structure)
                            )
            );
        }

        public <C extends Container, T extends Recipe<C>> Builder addRecipeUnlockCriterion(String name, T recipe) {
            return addCriterion(
                    name,
                    RecipeUnlockedTrigger.unlocked(recipe.getId())
            );
        }

        public Builder addInventoryChangedCriterion(String name, ItemLike... items) {
            return addCriterion(
                    name,
                    InventoryChangeTrigger.TriggerInstance.hasItems(items)
            );
        }

        public Builder addInventoryChangedAnyCriterion(String name, ItemLike... items) {
            InventoryChangeTrigger.TriggerInstance trigger =
                    InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(
                            null,
                            Arrays.stream(items).map(i -> i.asItem()).collect(Collectors.toSet()),
                            MinMaxBounds.Ints.ANY,
                            MinMaxBounds.Ints.ANY,
                            EnchantmentPredicate.NONE,
                            EnchantmentPredicate.NONE,
                            null,
                            NbtPredicate.ANY
                    ));
            return addCriterion(name, trigger);
        }

        public Builder addInventoryChangedCriterion(String name, TagKey<Item> tag) {
            return addCriterion(
                    name,
                    InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(
                            tag,
                            null,
                            MinMaxBounds.Ints.ANY,
                            MinMaxBounds.Ints.ANY,
                            EnchantmentPredicate.NONE,
                            EnchantmentPredicate.NONE,
                            null,
                            NbtPredicate.ANY
                    ))
            );
        }

        //

        public Builder addEquipmentSetSlotCriterion(EquipmentSet set, EquipmentSlot slot) {
            return addInventoryChangedCriterion(
                    set.baseName + "_" + slot,
                    set.getSlot(slot)
            );
        }

        public Builder addArmorSetCriterion(EquipmentSet set) {
            return addEquipmentSetSlotCriterion(set, EquipmentSet.HELMET_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.CHESTPLATE_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.LEGGINGS_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.BOOTS_SLOT);
        }

        public Builder addToolSetCriterion(EquipmentSet set) {
            return addEquipmentSetSlotCriterion(set, EquipmentSet.PICKAXE_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.AXE_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.SHOVEL_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.SWORD_SLOT)
                    .addEquipmentSetSlotCriterion(set, EquipmentSet.HOE_SLOT);
        }

        public Builder addWoodCriterion(WoodenComplexMaterial mat) {
            return addInventoryChangedAnyCriterion(
                    "got_" + mat.getBaseName(),
                    mat.getBlock(WoodSlots.LOG),
                    mat.getBlock(WoodSlots.BARK),
                    mat.getBlock(WoodSlots.PLANKS)
            );
        }

        public Builder addVisitBiomesCriterion(List<ResourceKey<Biome>> list) {
            for (ResourceKey<Biome> resourceKey : list) {
                addCriterion(
                        resourceKey.location().toString(),
                        PlayerTrigger.TriggerInstance.located(LocationPredicate.inBiome(resourceKey))
                );
            }
            return this;
        }

        public Builder requirements(RequirementsStrategy requirementsStrategy) {
            builder.requirements(requirementsStrategy);
            return this;
        }

        public Builder requirements(String[][] strings) {
            builder.requirements(strings);
            return this;
        }

        public Builder printDebugJson() {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BCLib.LOGGER.info(gson.toJson(builder.serializeToJson()));
            return this;
        }

        public ResourceLocation build() {
            AdvancementManager.register(id, this.builder);
            return this.id;
        }
    }

    public static class DisplayBuilder {
        Builder base;
        final Display display = new Display();

        DisplayBuilder reset(Builder base) {
            this.base = base;
            this.display.reset();
            return this;
        }

        public DisplayBuilder background(ResourceLocation value) {
            display.background = value;
            return this;
        }

        public DisplayBuilder icon(ItemLike value) {
            display.icon = new ItemStack(value);
            return this;
        }

        public DisplayBuilder icon(ItemStack value) {
            display.icon = value;
            return this;
        }

        public DisplayBuilder title(Component value) {
            display.title = value;
            return this;
        }

        public DisplayBuilder description(Component value) {
            display.description = value;
            return this;
        }

        public DisplayBuilder showToast() {
            display.showToast = true;
            return this;
        }

        public DisplayBuilder hideToast() {
            display.showToast = false;
            return this;
        }

        public DisplayBuilder hidden() {
            display.hidden = true;
            return this;
        }

        public DisplayBuilder visible() {
            display.hidden = false;
            return this;
        }

        public DisplayBuilder announceToChat() {
            display.announceChat = true;
            return this;
        }

        public DisplayBuilder hideFromChat() {
            display.announceChat = false;
            return this;
        }

        public DisplayBuilder frame(FrameType type) {
            display.frame = type;
            return this;
        }

        public DisplayBuilder challenge() {
            return frame(FrameType.CHALLENGE);
        }

        public DisplayBuilder task() {
            return frame(FrameType.TASK);
        }

        public DisplayBuilder goal() {
            return frame(FrameType.GOAL);
        }

        public Builder endDisplay() {
            base.display(display.build());
            return base;
        }
    }
}
