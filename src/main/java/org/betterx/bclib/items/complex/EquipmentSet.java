package org.betterx.bclib.items.complex;

import org.betterx.bclib.registry.ItemRegistry;

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
        public static SetValues IRON_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.AXE_SLOT, 6)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.HOE_SLOT, -2);

        public static SetValues DIAMOND_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, 3)
                .add(EquipmentSet.AXE_SLOT, 5)
                .add(EquipmentSet.SHOVEL_SLOT, 1.5f)
                .add(EquipmentSet.PICKAXE_SLOT, 1)
                .add(EquipmentSet.HOE_SLOT, -3);
    }

    public static class AttackSpeed {
        public static SetValues IRON_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.AXE_SLOT, -3.1f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.HOE_SLOT, -1.0f);

        public static SetValues DIAMOND_LEVEL = EquipmentSet.SetValues
                .create()
                .add(EquipmentSet.SWORD_SLOT, -2.4f)
                .add(EquipmentSet.AXE_SLOT, -3.0f)
                .add(EquipmentSet.SHOVEL_SLOT, -3.0f)
                .add(EquipmentSet.PICKAXE_SLOT, -2.8f)
                .add(EquipmentSet.HOE_SLOT, 0.0f);
    }

    public interface ItemDescriptorCreator<I extends Item> {
        EquipmentDescription<I> build(Item base, Function<Tier, I> creator);
    }

    public interface DescriptorCreator<I extends Item> {
        EquipmentDescription<I> build(Function<Tier, I> creator);
    }

    public interface ItemCreator<I extends Item> {
        I build(Tier t, float attackDamage, float attackSpeed);
    }

    public static class SetValues {
        private final Map<String, Float> values;

        private SetValues() {
            values = new HashMap<>();
        }

        public static SetValues create() {
            return new SetValues();
        }

        public SetValues add(String slot, float value) {
            values.put(slot, value);
            return this;
        }

        public float get(String slot) {
            return values.getOrDefault(slot, 0.0f);
        }
    }

    public final Tier material;
    public final String baseName;
    public final String modID;
    public final ItemLike stick;

    public static final String PICKAXE_SLOT = "pickaxe";
    public static final String AXE_SLOT = "axe";
    public static final String SHOVEL_SLOT = "shovel";
    public static final String SWORD_SLOT = "sword";
    public static final String HOE_SLOT = "hoe";
    public static final String SHEARS_SLOT = "shears";
    public static final String HELMET_SLOT = "helmet";
    public static final String CHESTPLATE_SLOT = "chestplate";
    public static final String LEGGINGS_SLOT = "leggings";
    public static final String BOOTS_SLOT = "boots";

    public final SetValues attackDamage;
    public final SetValues attackSpeed;

    private final Map<String, EquipmentDescription<?>> descriptions = new HashMap<>();

    public EquipmentSet(
            Tier material,
            String modID,
            String baseName,
            ItemLike stick,
            SetValues attackDamage,
            SetValues attackSpeed
    ) {
        this.material = material;
        this.baseName = baseName;
        this.modID = modID;
        this.stick = stick;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    protected <I extends Item> void add(String slot, EquipmentDescription<I> desc) {
        descriptions.put(slot, desc);
    }

    protected <I extends Item> void add(
            String slot,
            EquipmentSet baseSet,
            ItemDescriptorCreator<I> descriptor,
            ItemCreator<I> item
    ) {
        EquipmentDescription<I> desc = descriptor.build(
                baseSet.getSlot(slot),
                (tier) -> item.build(tier, this.attackDamage.get(slot), this.attackSpeed.get(slot))
        );
        descriptions.put(slot, desc);
    }

    protected <I extends Item> void add(String slot, DescriptorCreator<I> descriptor, ItemCreator<I> item) {
        EquipmentDescription<I> desc = descriptor.build(
                (tier) -> item.build(tier, this.attackDamage.get(slot), this.attackSpeed.get(slot))
        );
        descriptions.put(slot, desc);
    }


    public EquipmentSet init(ItemRegistry itemsRegistry) {
        for (var desc : descriptions.entrySet()) {
            desc.getValue()
                .init(buildID(desc), itemsRegistry, material, stick);
        }
        return this;
    }

    @NotNull
    protected ResourceLocation buildID(Map.Entry<String, EquipmentDescription<?>> desc) {
        return new ResourceLocation(modID, baseName + "_" + desc.getKey());
    }

    public <I extends Item> I getSlot(String slot) {
        return (I) descriptions.get(slot).getItem();
    }
}
