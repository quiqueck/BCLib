package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
    private static Identifier ANVIL_LOCATION;
    @Unique
    private final List<AbstractWidget> bcl_buttons = Lists.newArrayList();
    @Unique
    private boolean bcl_nameDisabled = false;

    public AnvilScreenMixin(AnvilMenu handler, Inventory playerInventory, Component title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    @Override
    public void extractErrorIcon(GuiGraphicsExtractor guiGraphics, int i, int j) {
        if (this.bcl_hasRecipeError()) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ANVIL_LOCATION,
                    i + 65,
                    j + 46,
                    0,
                    this.imageWidth,
                    0,
                    28,
                    21,
                    256,
                    256
            );
        }
    }

    @Unique
    private boolean bcl_hasRecipeError() {
        return false;
    }

    @Inject(method = "subInit", at = @At("TAIL"))
    protected void be_subInit(CallbackInfo info) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        bcl_buttons.clear();
        bcl_buttons.add(Button.builder(Component.literal("<"), b -> be_previousRecipe())
                              .bounds(x + 8, y + 45, 15, 20)
                              .build());
        bcl_buttons.add(Button.builder(Component.literal(">"), b -> be_nextRecipe())
                              .bounds(x + 154, y + 45, 15, 20)
                              .build());

        //the bounds above are absolute screen coordinates, so the buttons need to be rendered by
        //Screen#extractRenderState. Rendering them from extractLabels would offset them by
        //(leftPos, topPos), as that runs inside the translated pose of the container screen.
        bcl_buttons.forEach(this::addRenderableWidget);
        bcl_syncRecipeState();
    }

    @Inject(method = "extractBackground", at = @At("HEAD"))
    protected void be_beforeRender(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            float a,
            CallbackInfo info
    ) {
        bcl_syncRecipeState();
    }

    /**
     * The recipe count is transferred in a {@code DataSlot}, which is sent after the slot contents.
     * So we can not rely on {@link #be_onSlotUpdate} alone and refresh the state before every frame.
     */
    @Unique
    private void bcl_syncRecipeState() {
        final int recipeCount = ((AnvilScreenHandlerExtended) menu).bcl_getRecipeCount();
        bcl_buttons.forEach(button -> button.visible = recipeCount > 1);
        if (recipeCount > 0) {
            //renaming is disabled while an anvil recipe is active
            if (!name.getValue().isEmpty()) name.setValue("");
            name.setEditable(false);
            bcl_nameDisabled = true;
        } else if (bcl_nameDisabled) {
            //restore what vanilla's slotChanged would have done
            bcl_nameDisabled = false;
            final ItemStack input = menu.getSlot(0).getItem();
            name.setValue(input.isEmpty() ? "" : input.getHoverName().getString());
            name.setEditable(!input.isEmpty());
        }
    }

    @Inject(method = "slotChanged", at = @At("HEAD"), cancellable = true)
    public void be_onSlotUpdate(AbstractContainerMenu handler, int slotId, ItemStack stack, CallbackInfo info) {
        if (((AnvilScreenHandlerExtended) handler).bcl_getRecipeCount() > 0) {
            bcl_syncRecipeState();
            info.cancel();
        } else {
            bcl_buttons.forEach(button -> button.visible = false);
        }
    }

    @Unique
    private void be_nextRecipe() {
        ((AnvilScreenHandlerExtended) menu).be_nextRecipe();
    }

    @Unique
    private void be_previousRecipe() {
        ((AnvilScreenHandlerExtended) menu).be_previousRecipe();
    }


    @Intrinsic(displace = true)
    //@Override
    public boolean bcl$mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (minecraft != null) {
            for (AbstractWidget elem : bcl_buttons) {
                if (elem.visible && elem.mouseClicked(mouseButtonEvent, bl)) {
                    if (minecraft.gameMode != null) {
                        int i = bcl_buttons.indexOf(elem);
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }
}
