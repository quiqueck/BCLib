package org.betterx.bclib.recipes;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public class SmithingTemplates {
    public static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    public static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    public static final ResourceLocation EMPTY_SLOT_INGOT = new ResourceLocation("item/empty_slot_ingot");
    
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
