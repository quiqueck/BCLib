package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.RenderHelper;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Image extends CustomRenderComponent {
    protected Rectangle uvRect;
    public final ResourceLocation location;
    protected float alpha;
    protected Size resourceSize;

    public Image(Value width, Value height, ResourceLocation location) {
        this(width, height, location, new Size(16, 16));
    }

    public Image(Value width, Value height, ResourceLocation location, Size resourceSize) {
        super(width, height);
        this.location = location;
        this.uvRect = new Rectangle(0, 0, resourceSize.width(), resourceSize.height());
        this.resourceSize = resourceSize;
        this.alpha = 1f;
    }


    public Image setAlpha(float a) {
        alpha = a;
        return this;
    }

    public float getAlpha() {
        return alpha;
    }


    public Image setUvRect(int left, int top, int width, int height) {
        uvRect = new Rectangle(left, top, width, height);
        return this;
    }

    public Rectangle getUvRect() {
        return uvRect;
    }

    public Image setResourceSize(int width, int height) {
        resourceSize = new Size(width, height);
        return this;
    }

    public Size getResourceSize() {
        return resourceSize;
    }

    @Override
    public int getContentWidth() {
        return resourceSize.width();
    }

    @Override
    public int getContentHeight() {
        return resourceSize.height();
    }


    @Override
    protected void customRender(
            PoseStack stack,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        RenderHelper.renderImage(stack, bounds.width, bounds.height, location, uvRect, resourceSize, alpha);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
    }
}
