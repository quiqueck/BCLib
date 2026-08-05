package org.betterx.bclib.api.v2.advancement;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

class Display {
    // ItemStackTemplate, not ItemStack: DisplayInfo only ever needed the Item+count+component-patch
    // triple an ItemStackTemplate already is, but building a full ItemStack first (as this used to)
    // eagerly reads the item's bound default DataComponentMap via Holder.Reference.components() -
    // not populated yet during datagen bootstrap (advancement generation runs before the
    // ReloadableServerResources reload that binds components), throwing "Components not bound yet".
    // ItemStackTemplate's constructors never touch components(), so building it directly here (see
    // DisplayBuilder.icon below) sidesteps the problem entirely instead of working around it.
    ItemStackTemplate icon;
    Component title;
    net.minecraft.network.chat.Component description;
    @Nullable Identifier background;
    AdvancementType frame;
    boolean showToast;
    boolean announceChat;
    boolean hidden;

    Display() {
    }

    Display reset() {
        this.icon = null;
        this.title = null;
        this.description = null;
        frame = AdvancementType.TASK;
        background = null;
        showToast = true;
        announceChat = true;
        hidden = false;
        return this;
    }

    DisplayInfo build() {
        return new DisplayInfo(
                icon, title, description,
                background == null ? Optional.empty() : Optional.of(new ClientAsset.ResourceTexture(background)),
                frame, showToast, announceChat, hidden
        );
    }
}

