package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.gui.gridlayout.*;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProgressListener;

import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.Nullable;

class ProgressLogoRender extends GridCustomRenderCell {
    public static final int SIZE = 64;
    public static final int LOGO_SIZE = 512;
    public static final int PIXELATED_SIZE = 512;
    float percentage = 0;
    double time = 0;

    protected ProgressLogoRender() {
        super(SIZE, GridLayout.GridValueType.CONSTANT, SIZE);
    }

    @Override
    public void onRender(PoseStack poseStack, GridTransform transform, Object context) {
        time += 0.03;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);

        final int yBarLocal = (int) (transform.height * percentage);
        final int yBar = transform.top + yBarLocal;

        final float fScale = (float) (0.3 * ((Math.sin(time) + 1.0) * 0.5) + 0.7);
        int height = (int) (transform.height * fScale);
        int width = (int) (transform.width * fScale);
        width -= ((transform.width - width) % 2);
        height -= ((transform.height - height) % 2);

        final int yOffset = (transform.height - height) / 2;
        final int xOffset = (transform.width - width) / 2;


        final int yBarImage = Math.max(0, Math.min(height, yBarLocal - yOffset));
        final float relativeY = ((float) yBarImage / height);

        if (yBarImage > 0) {
            final int uvTopLogo = (int) (relativeY * LOGO_SIZE);
            RenderSystem.setShaderTexture(0, BCLibScreen.BCLIB_LOGO_LOCATION);
            GuiComponent.blit(poseStack,
                    xOffset + transform.left,
                    yOffset + transform.top,
                    width,
                    yBarImage,
                    0, 0, LOGO_SIZE, uvTopLogo,
                    LOGO_SIZE, LOGO_SIZE
            );
        }

        if (yBarImage < height) {
            final int uvTopPixelated = (int) (relativeY * PIXELATED_SIZE);
            RenderSystem.setShaderTexture(0, ProgressScreen.BCLIB_LOGO_PIXELATED_LOCATION);
            GuiComponent.blit(poseStack,
                    xOffset + transform.left,
                    yOffset + transform.top + yBarImage,
                    width,
                    height - yBarImage,
                    0, uvTopPixelated, PIXELATED_SIZE, PIXELATED_SIZE - uvTopPixelated,
                    PIXELATED_SIZE, PIXELATED_SIZE
            );
        }

        if (percentage > 0 && percentage < 1.0) {
            GuiComponent.fill(
                    poseStack,
                    transform.left,
                    yBar,
                    transform.left + transform.width,
                    yBar + 1,
                    0x3FFFFFFF
            );
        }
    }
}

public class ProgressScreen extends GridScreen implements ProgressListener, AtomicProgressListener {

    static final ResourceLocation BCLIB_LOGO_PIXELATED_LOCATION = new ResourceLocation(
            BCLib.MOD_ID,
            "iconpixelated.png"
    );

    public ProgressScreen(@Nullable Screen parent, Component title, Component description) {
        super(parent, title, 20, true);
        this.description = description;
    }


    Component description;
    private Component stageComponent;
    private GridMessageCell stage;
    private GridStringCell progress;
    private ProgressLogoRender progressImage;
    private int currentProgress = 0;
    private AtomicInteger atomicCounter;

    @Override
    public void incAtomic(int maxProgress) {
        if (atomicCounter != null) {
            progressStagePercentage((100 * atomicCounter.incrementAndGet()) / maxProgress);
        }
    }

    @Override
    public void resetAtomic() {
        progressStagePercentage(0);
        atomicCounter = new AtomicInteger(0);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public Component getProgressComponent() {
        return getProgressComponent(currentProgress);
    }

    private Component getProgressComponent(int pg) {
        return Component.translatable("title.bclib.progress").append(": " + pg + "%");
    }

    @Override
    protected void initLayout() {
        grid.addSpacerRow();

        GridRow row = grid.addRow(GridLayout.VerticalAlignment.CENTER);
        row.addFiller();
        progressImage = new ProgressLogoRender();
        progressImage.percentage = currentProgress / 100.0f;
        row.addCustomRender(progressImage);
        row.addSpacer();

        int textWidth = Math.max(getWidth(description), getWidth(getProgressComponent(100)));
        GridColumn textCol = row.addColumn(0, GridLayout.GridValueType.INHERIT);
        textCol.addRow().addString(description, this);
        textCol.addSpacerRow();
        progress = textCol.addRow()
                          .addString(getProgressComponent(), GridLayout.COLOR_GRAY, GridLayout.Alignment.LEFT, this);

        row.addFiller();

        grid.addSpacerRow(20);
        row = grid.addRow();
        stage = row.addMessage(
                stageComponent != null ? stageComponent : Component.literal(""),
                font,
                GridLayout.Alignment.CENTER
        );
    }

    @Override
    public void progressStartNoAbort(Component text) {
        this.progressStage(text);
    }

    @Override
    public void progressStart(Component text) {
        this.progressStage(text);
        this.progressStagePercentage(0);
    }

    @Override
    public void progressStage(Component text) {
        stageComponent = text;
        if (stage != null) stage.setText(text);
    }

    @Override
    public void progressStagePercentage(int progress) {
        if (progress != currentProgress) {
            currentProgress = progress;
            if (progressImage != null) progressImage.percentage = currentProgress / 100.0f;
            if (this.progress != null) this.progress.setText(getProgressComponent());
        }
    }

    @Override
    public void stop() {

    }
}
