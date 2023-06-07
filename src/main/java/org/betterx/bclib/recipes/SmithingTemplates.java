package org.betterx.bclib.recipes;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.util.List;

public class SmithingTemplates {
    public static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    public static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    public static final ResourceLocation EMPTY_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_SLOT_HOE = new ResourceLocation("item/empty_slot_hoe");
    public static final ResourceLocation EMPTY_SLOT_AXE = new ResourceLocation("item/empty_slot_axe");
    public static final ResourceLocation EMPTY_SLOT_SWORD = new ResourceLocation("item/empty_slot_sword");
    public static final ResourceLocation EMPTY_SLOT_SHOVEL = new ResourceLocation("item/empty_slot_shovel");
    public static final ResourceLocation EMPTY_SLOT_PICKAXE = new ResourceLocation("item/empty_slot_pickaxe");
    public static final ResourceLocation EMPTY_SLOT_INGOT = new ResourceLocation("item/empty_slot_ingot");
    public static final ResourceLocation EMPTY_SLOT_REDSTONE_DUST = new ResourceLocation("item/empty_slot_redstone_dust");
    public static final ResourceLocation EMPTY_SLOT_DIAMOND = new ResourceLocation("item/empty_slot_diamond");

    public static final List<ResourceLocation> TOOLS = List.of(
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_PICKAXE,
            EMPTY_SLOT_AXE,
            EMPTY_SLOT_HOE,
            EMPTY_SLOT_SHOVEL
    );

    public static final List<ResourceLocation> ARMOR = List.of(
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_BOOTS
    );
    public static final List<ResourceLocation> ARMOR_AND_TOOLS = combine(ARMOR, TOOLS);

    public static List<ResourceLocation> combine(List<ResourceLocation>... sources) {
        final ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        for (var s : sources) {
            builder.addAll(s);
        }
        return builder.build();
    }

    public static Builder create(ResourceLocation id) {
        return new Builder(id);
    }

    public static class Builder {
        private final ResourceLocation ID;
        List<ResourceLocation> baseSlotEmptyIcons;
        List<ResourceLocation> additionalSlotEmptyIcons;

        protected Builder(ResourceLocation id) {
            ID = id;
        }

        public Builder setBaseSlotEmptyIcons(List<ResourceLocation> baseSlotEmptyIcons) {
            this.baseSlotEmptyIcons = baseSlotEmptyIcons;
            return this;
        }

        public Builder setAdditionalSlotEmptyIcons(List<ResourceLocation> additionalSlotEmptyIcons) {
            this.additionalSlotEmptyIcons = additionalSlotEmptyIcons;
            return this;
        }

        public SmithingTemplateItem build() {
            if (baseSlotEmptyIcons == null || baseSlotEmptyIcons.isEmpty()) {
                throw new IllegalStateException("Base slot empty icons must contain at least one icon");
            }
            if (additionalSlotEmptyIcons == null || additionalSlotEmptyIcons.isEmpty()) {
                throw new IllegalStateException("Additional slot empty icons must contain at least one icon");
            }


            return new SmithingTemplateItem(
                    Component.translatable(Util.makeDescriptionId(
                            "item",
                            new ResourceLocation(ID.getNamespace(), "smithing_template." + ID.getPath() + ".applies_to")
                    )).withStyle(DESCRIPTION_FORMAT),
                    Component.translatable(Util.makeDescriptionId(
                            "item",
                            new ResourceLocation(
                                    ID.getNamespace(),
                                    "smithing_template." + ID.getPath() + ".ingredients"
                            )
                    )).withStyle(DESCRIPTION_FORMAT),
                    Component.translatable(Util.makeDescriptionId(
                            "upgrade",
                            ID
                    )).withStyle(TITLE_FORMAT),
                    Component.translatable(Util.makeDescriptionId(
                            "item",
                            new ResourceLocation(
                                    ID.getNamespace(),
                                    "smithing_template." + ID.getPath() + ".base_slot_description"
                            )
                    )),
                    Component.translatable(Util.makeDescriptionId(
                            "item",
                            new ResourceLocation(
                                    ID.getNamespace(),
                                    "smithing_template." + ID.getPath() + ".additions_slot_description"
                            )
                    )),
                    baseSlotEmptyIcons,
                    additionalSlotEmptyIcons
            );
        }
    }
}
