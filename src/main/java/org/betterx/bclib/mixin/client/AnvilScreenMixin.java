package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AnvilScreen.class)
@Implements(@Interface(iface = ContainerEventHandler.class, prefix = "bcl$"))
public class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {

    @Shadow
    private EditBox name;

    @Shadow
    @Final
    private static ResourceLocation ANVIL_LOCATION;
    private final List<AbstractWidget> be_buttons = Lists.newArrayList();

    public AnvilScreenMixin(AnvilMenu handler, Inventory playerInventory, Component title, ResourceLocation texture) {
        super(handler, playerInventory, title, texture);
    }

    @Override
    public void renderErrorIcon(GuiGraphics guiGraphics, int i, int j) {
        if (this.bcl_hasRecipeError()) {
            guiGraphics.blit(ANVIL_LOCATION, i + 65, j + 46, this.imageWidth, 0, 28, 21);
        }
    }

    private boolean bcl_hasRecipeError() {
        //TODO: 1.19.4 check error conditions
        return false;
    }

    @Inject(method = "subInit", at = @At("TAIL"))
    protected void be_subInit(CallbackInfo info) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        be_buttons.clear();
        be_buttons.add(Button.builder(Component.literal("<"), b -> be_previousRecipe())
                             .bounds(x + 8, y + 45, 15, 20)
                             .build());
        be_buttons.add(Button.builder(Component.literal(">"), b -> be_nextRecipe())
                             .bounds(x + 154, y + 45, 15, 20)
                             .build());

        be_buttons.forEach(button -> addWidget(button));
    }

    @Inject(method = "renderFg", at = @At("TAIL"))
    protected void be_renderForeground(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo info
    ) {
        be_buttons.forEach(button -> {
            button.render(guiGraphics, mouseX, mouseY, delta);
        });
    }

    @Inject(method = "slotChanged", at = @At("HEAD"), cancellable = true)
    public void be_onSlotUpdate(AbstractContainerMenu handler, int slotId, ItemStack stack, CallbackInfo info) {
        AnvilScreenHandlerExtended anvilHandler = (AnvilScreenHandlerExtended) handler;
        if (anvilHandler.bcl_getCurrentRecipe() != null) {
            if (anvilHandler.bcl_getRecipes().size() > 1) {
                be_buttons.forEach(button -> button.visible = true);
            } else {
                be_buttons.forEach(button -> button.visible = false);
            }
            name.setValue("");
            info.cancel();
        } else {
            be_buttons.forEach(button -> button.visible = false);
        }
    }

    private void be_nextRecipe() {
        ((AnvilScreenHandlerExtended) menu).be_nextRecipe();
    }

    private void be_previousRecipe() {
        ((AnvilScreenHandlerExtended) menu).be_previousRecipe();
    }


    @Intrinsic(displace = true)
    //@Override
    public boolean bcl$mouseClicked(double mouseX, double mouseY, int button) {
        if (minecraft != null) {
            for (AbstractWidget elem : be_buttons) {
                if (elem.visible && elem.mouseClicked(mouseX, mouseY, button)) {
                    if (minecraft.gameMode != null) {
                        int i = be_buttons.indexOf(elem);
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
