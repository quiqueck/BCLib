package org.betterx.bclib.api.v2.advancement;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

class Display {
    ItemStack icon;
    Component title;
    net.minecraft.network.chat.Component description;
    @Nullable ResourceLocation background;
    FrameType frame;
    boolean showToast;
    boolean announceChat;
    boolean hidden;

    Display() {
    }

    Display reset() {
        this.icon = null;
        this.title = null;
        this.description = null;
        frame = FrameType.TASK;
        background = null;
        showToast = true;
        announceChat = true;
        hidden = false;
        return this;
    }

    DisplayInfo build() {
        return new DisplayInfo(
                icon, title, description,
                background, frame, showToast, announceChat, hidden
        );
    }
}

