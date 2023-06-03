package org.betterx.bclib.items.complex;

import org.betterx.bclib.registry.ItemRegistry;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public abstract class EquipmentSet {
    public static class AttackDamage {
        public static SetValues WOOD_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.AXE_SLOT, 6)
                .add(EquipmentSet.HOE_SLOT, 0);

        public static SetValues STONE_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.AXE_SLOT, 7)
                .add(EquipmentSet.HOE_SLOT, -1);

        public static SetValues GOLDEN_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.AXE_SLOT, 6)
                .add(EquipmentSet.HOE_SLOT, 0);
        public static SetValues IRON_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.AXE_SLOT, 6)
                .add(EquipmentSet.HOE_SLOT, -2);

        public static SetValues DIAMOND_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.AXE_SLOT, 5)
                .add(EquipmentSet.HOE_SLOT, -3);

        public static SetValues NETHERITE_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.AXE_SLOT, 5)
                .add(EquipmentSet.HOE_SLOT, -4);
    }

    public static class AttackSpeed {
        public static SetValues WOOD_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.AXE_SLOT, -3.2f)
                .add(EquipmentSet.HOE_SLOT, -3.0f);

        public static SetValues STONE_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.AXE_SLOT, -3.2f)
                .add(EquipmentSet.HOE_SLOT, -2.0f);

        public static SetValues GOLDEN_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.AXE_SLOT, -3.0f)
                .add(EquipmentSet.HOE_SLOT, -3.0f);
        public static SetValues IRON_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.AXE_SLOT, -3.1f)
                .add(EquipmentSet.HOE_SLOT, -1.0f);

        public static SetValues DIAMOND_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.AXE_SLOT, -3.0f)
                .add(EquipmentSet.HOE_SLOT, 0.0f);

        public static SetValues NETHERITE_LEVEL = DIAMOND_LEVEL;
    }

    public interface ItemDescriptorCreator<I extends Item> {
        EquipmentDescription<I> build(EquipmentSlot slot, Item base, Function<Tier, I> creator);
    }

    public interface DescriptorCreator<I extends Item> {
        EquipmentDescription<I> build(EquipmentSlot slot, Function<Tier, I> creator);
    }

    public interface ItemCreator<I extends Item> {
        I build(Tier t, float attackDamage, float attackSpeed);
    }

    public static class SetValues {
        private final Map<EquipmentSlot, Float> values;

        private SetValues() {
            values = new HashMap<>();
        }

        public static SetValues create() {
            return new SetValues();
        }

        public static SetValues copy(SetValues source, float offset) {
            SetValues v = create();
            for (var e : source.values.entrySet())
                v.add(e.getKey(), e.getValue() + offset);
            return v;
        }

        public SetValues add(EquipmentSlot slot, float value) {
            values.put(slot, value);
            return this;
        }

        public SetValues offset(EquipmentSlot slot, float offset) {
            values.put(slot, get(slot) + offset);
            return this;
        }

        public float get(EquipmentSlot slot) {
            return values.getOrDefault(slot, 0.0f);
        }
    }

    public final Tier material;
    public final String baseName;
    public final String modID;
    public final ItemLike stick;

    public static final EquipmentSlot PICKAXE_SLOT = new EquipmentSlot("pickaxe", RecipeCategory.TOOLS);
    public static final EquipmentSlot AXE_SLOT = new EquipmentSlot("axe", RecipeCategory.TOOLS);
    public static final EquipmentSlot SHOVEL_SLOT = new EquipmentSlot("shovel", RecipeCategory.TOOLS);
    public static final EquipmentSlot SWORD_SLOT = new EquipmentSlot("sword", RecipeCategory.COMBAT);
    public static final EquipmentSlot HOE_SLOT = new EquipmentSlot("hoe", RecipeCategory.TOOLS);
    public static final EquipmentSlot SHEARS_SLOT = new EquipmentSlot("shears", RecipeCategory.TOOLS);
    public static final EquipmentSlot HELMET_SLOT = new EquipmentSlot("helmet", RecipeCategory.COMBAT);
    public static final EquipmentSlot CHESTPLATE_SLOT = new EquipmentSlot("chestplate", RecipeCategory.COMBAT);
    public static final EquipmentSlot LEGGINGS_SLOT = new EquipmentSlot("leggings", RecipeCategory.COMBAT);
    public static final EquipmentSlot BOOTS_SLOT = new EquipmentSlot("boots", RecipeCategory.COMBAT);

    public final SetValues attackDamage;
    public final SetValues attackSpeed;

    private final Map<EquipmentSlot, EquipmentDescription<?>> descriptions = new HashMap<>();
    protected final EquipmentSet sourceSet;

    public EquipmentSet(
            Tier material,
            String modID,
            String baseName,
            ItemLike stick,
            SetValues attackDamage,
            SetValues attackSpeed
    ) {
        this(material, modID, baseName, stick, null, attackDamage, attackSpeed);
    }

    public EquipmentSet(
            Tier material,
            String modID,
            String baseName,
            ItemLike stick,
            EquipmentSet sourceSet,
            SetValues attackDamage,
            SetValues attackSpeed
    ) {
        this.material = material;
        this.baseName = baseName;
        this.modID = modID;
        this.stick = stick;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.sourceSet = sourceSet;
    }

    protected <I extends Item> void add(EquipmentSlot slot, EquipmentDescription<I> desc) {
        descriptions.put(slot, desc);
    }

    protected <I extends Item> void add(
            EquipmentSlot slot,
            EquipmentSet baseSet,
            ItemDescriptorCreator<I> descriptor,
            ItemCreator<I> item
    ) {
        EquipmentDescription<I> desc = descriptor.build(
                slot,
                baseSet.getSlot(slot),
                (tier) -> item.build(tier, this.attackDamage.get(slot), this.attackSpeed.get(slot))
        );
        descriptions.put(slot, desc);
    }

    protected <I extends Item> void add(EquipmentSlot slot, DescriptorCreator<I> descriptor, ItemCreator<I> item) {
        EquipmentDescription<I> desc = descriptor.build(
                slot,
                (tier) -> item.build(tier, this.attackDamage.get(slot), this.attackSpeed.get(slot))
        );
        descriptions.put(slot, desc);
    }


    public EquipmentSet init(ItemRegistry itemsRegistry) {
        for (var desc : descriptions.entrySet()) {
            desc.getValue()
                .init(buildID(desc), itemsRegistry, material, stick, sourceSet);
        }
        return this;
    }

    @NotNull
    protected ResourceLocation buildID(Map.Entry<EquipmentSlot, EquipmentDescription<?>> desc) {
        return new ResourceLocation(modID, baseName + "_" + desc.getKey().name());
    }

    public <I extends Item> I getSlot(EquipmentSlot slot) {
        return (I) descriptions.get(slot).getItem();
    }
}
