package org.betterx.ui.layout.components;


import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class Item extends CustomRenderComponent {
    private ItemStack itemStack;

    public Item(Value width, Value height) {
        super(width, height);
    }

    public Item setItem(ItemStack item) {
        this.itemStack = item;
        return this;
    }

    @Override
    protected void customRender(PoseStack stack, int x, int y, float deltaTicks, Rectangle bounds, Rectangle clipRect) {
        final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderAndDecorateItem(Minecraft.getInstance().player, itemStack, bounds.left, bounds.top, 0);
        itemRenderer.renderGuiItemDecorations(
                Minecraft.getInstance().font,
                itemStack,
                bounds.left,
                bounds.top,
                "" + itemStack.getCount()
        );
    }

    @Override
    public int getContentWidth() {
        return 16;
    }

    @Override
    public int getContentHeight() {
        return 16;
    }
}
