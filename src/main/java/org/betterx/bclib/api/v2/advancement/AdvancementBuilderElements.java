package org.betterx.bclib.api.v2.advancement;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

class Display {
    ItemStack icon;
    Component title;
    net.minecraft.network.chat.Component description;
    @Nullable ResourceLocation background;
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
                Optional.of(background), frame, showToast, announceChat, hidden
        );
    }
}

