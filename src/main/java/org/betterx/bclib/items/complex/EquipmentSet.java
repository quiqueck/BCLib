package org.betterx.bclib.items.complex;

import org.betterx.bclib.registry.ItemRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class EquipmentSet {
    public final Tier material;
    public final String prefix;
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
    public static final String LEGGINS_SLOT = "leggings";
    public static final String BOOTS_SLOT = "boots";

    private final Map<String, EquipmentDescription<?>> descriptions = new HashMap<>();

    public EquipmentSet(Tier material, String modID, String prefix, ItemLike stick) {
        this.material = material;
        this.prefix = prefix;
        this.modID = modID;
        this.stick = stick;
    }

    protected <I extends Item> void add(String slot, EquipmentDescription<I> desc) {
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
        return new ResourceLocation(modID, prefix + "_" + desc.getKey());
    }

    public <I extends Item> I getSlot(String slot) {
        return (I) descriptions.get(slot).getItem();
    }
}
