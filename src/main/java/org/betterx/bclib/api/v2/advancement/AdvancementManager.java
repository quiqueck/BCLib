package org.betterx.bclib.api.v2.advancement;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;

public class AdvancementManager {
    private static final Map<ResourceLocation, Advancement.Builder> ADVANCEMENTS = new HashMap<>();

    public static void register(ResourceLocation id, Advancement.Builder builder) {
        ADVANCEMENTS.put(id, builder);
    }

    @ApiStatus.Internal
    public static void addAdvancements(Map<ResourceLocation, Advancement.Builder> map) {
        for (var entry : ADVANCEMENTS.entrySet()) {
            if (!map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
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

        private final Advancement.Builder builder = Advancement.Builder.advancement();
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

        public static Builder create(ResourceLocation id) {
            return new Builder(id, AdvancementType.REGULAR);
        }

        public static Builder create(ResourceLocation id, AdvancementType type) {
            return new Builder(id, type);
        }

        public static Builder create(Item item) {
            return create(item, AdvancementType.REGULAR);
        }

        public static Builder create(ItemStack item) {
            return create(item, AdvancementType.REGULAR);
        }

        public static Builder create(ItemLike item, AdvancementType type) {
            return create(new ItemStack(item), type);
        }

        public static Builder create(ItemStack item, AdvancementType type) {
            return create(item, type, (displayBuilder) -> {
            });
        }

        public static Builder create(Item item, AdvancementType type, Consumer<DisplayBuilder> displayAdapter) {
            return create(new ItemStack(item), type, displayAdapter);
        }

        public static Builder create(ItemStack item, AdvancementType type, Consumer<DisplayBuilder> displayAdapter) {
            var id = Registry.ITEM.getKey(item.getItem());
            boolean canBuild = true;
            if (id == null || item.is(Items.AIR)) {
                canBuild = false;
                id = Registry.ITEM.getDefaultKey();
            }

            String baseName = "advancements." + id.getNamespace() + "." + id.getPath() + ".";
            Builder b = new Builder(id, type);
            var displayBuilder = b.startDisplay(
                    item,
                    Component.translatable(baseName + "title"),
                    Component.translatable(baseName + "description")
            );
            if (displayAdapter != null) displayAdapter.accept(displayBuilder);
            b = displayBuilder.endDisplay();
            b.canBuild = canBuild;
            return b;
        }

        public static <C extends Container, T extends Recipe<C>> Builder createRecipe(T recipe, AdvancementType type) {
            Item item = recipe.getResultItem().getItem();
            return create(item, type, displayBuilder -> displayBuilder.hideToast().hideFromChat())
                    .awardRecipe(item)
                    .addRecipeUnlockCriterion(
                            "has_the_recipe",
                            recipe
                    );
        }

        public Builder parent(Advancement advancement) {
            builder.parent(advancement);
            return this;
        }

        public Builder parent(ResourceLocation resourceLocation) {
            builder.parent(resourceLocation);
            return this;
        }

        public DisplayBuilder startDisplay(Item icon) {
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
                var id = Registry.ITEM.getKey(icon.getItem());
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
            var rewardBuilder = startReward();
            for (ItemLike item : items) {
                var id = Registry.ITEM.getKey(item.asItem());
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

        public Builder addCriterion(String string, CriterionTriggerInstance criterionTriggerInstance) {
            builder.addCriterion(string, new Criterion(criterionTriggerInstance));
            return this;
        }

        public Builder addCriterion(String string, Criterion criterion) {
            builder.addCriterion(string, criterion);
            return this;
        }

        public <C extends Container, T extends Recipe<C>> Builder addRecipeUnlockCriterion(String name, T recipe) {
            return addCriterion(
                    name,
                    RecipeUnlockedTrigger.unlocked(recipe.getId())
            )
                    .startReward()
                    .addRecipe(recipe.getId())
                    .endReward()
                    .requirements(RequirementsStrategy.OR);
        }

        public Builder addInventoryChangedCriterion(String name, ItemLike... items) {
            return addCriterion(
                    name,
                    InventoryChangeTrigger.TriggerInstance.hasItems(items)
            );
        }

        public Builder requirements(RequirementsStrategy requirementsStrategy) {
            builder.requirements(requirementsStrategy);
            return this;
        }

        public Builder requirements(String[][] strings) {
            builder.requirements(strings);
            return this;
        }

        public ResourceLocation buildAndRegister() {
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
