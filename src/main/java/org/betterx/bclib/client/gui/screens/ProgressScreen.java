package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Value;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreen;
import org.betterx.bclib.BCLib;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProgressListener;

import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.Nullable;

class ProgressLogoRender extends CustomRenderComponent<ProgressLogoRender> {
    public static final int SIZE = 64;
    public static final int LOGO_SIZE = 512;
    public static final int PIXELATED_SIZE = 512;
    float percentage = 0;
    double time = 0;

    protected ProgressLogoRender() {
        super(Value.fixed(SIZE), Value.fixed(SIZE));
    }

    @Override
    public int getContentWidth() {
        return SIZE;
    }

    @Override
    public int getContentHeight() {
        return SIZE;
    }

    @Override
    protected void customRender(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            float deltaTicks,
            Rectangle transform,
            Rectangle clipRect
    ) {
        //time += 0.03;
        time += deltaTicks * 0.1;

        final int yBarLocal = (int) (transform.height * percentage);

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
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    BCLibLayoutScreen.BCLIB_LOGO_LOCATION,
                    xOffset, yOffset,
                    0.0f, 0.0f,
                    width, yBarImage,
                    LOGO_SIZE, uvTopLogo,
                    LOGO_SIZE, LOGO_SIZE,
                    0xFFFFFFFF
            );
        }

        if (yBarImage < height) {
            final int uvTopPixelated = (int) (relativeY * PIXELATED_SIZE);
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ProgressScreen.BCLIB_LOGO_PIXELATED_LOCATION,
                    xOffset, yOffset + yBarImage,
                    0.0f, (float) uvTopPixelated,
                    width, height - yBarImage,
                    PIXELATED_SIZE, PIXELATED_SIZE - uvTopPixelated,
                    PIXELATED_SIZE, PIXELATED_SIZE,
                    0xFFFFFFFF
            );
        }

        if (percentage > 0 && percentage < 1.0) {
            guiGraphics.fill(
                    0, yBarLocal,
                    transform.width, yBarLocal + 1,
                    0x3FFFFFFF
            );
        }
    }

    private boolean focused;

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean bl) {
        focused = bl;
    }
}

public class ProgressScreen extends LayoutScreen implements ProgressListener, AtomicProgressListener {

    static final Identifier BCLIB_LOGO_PIXELATED_LOCATION = Identifier.fromNamespaceAndPath(
            BCLib.MOD_ID,
            "iconpixelated.png"
    );

    public ProgressScreen(@Nullable Screen parent, Component title, Component description) {
        super(parent, title);
        this.description = description;
    }


    Component description;
    private Component stageComponent;
    private MultiLineText stage;
    private HorizontalStack stageRow;
    private Text progress;
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
        if (stageRow != null) stageRow.reCalculateLayout();
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

    @Override
    protected LayoutComponent<?, ?> createScreen(LayoutComponent<?, ?> content) {
        return content;
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack grid = new VerticalStack(fill(), fill()).setDebugName("grid");
        grid.addFiller();
        grid.add(createTitle());
        grid.addSpacer(4);

        HorizontalStack contentRow = grid.addRow(fit(), fit())
                                         .centerHorizontal()
                                         .setDebugName("contentRow");

        progressImage = new ProgressLogoRender();
        progressImage.percentage = currentProgress / 100.0f;
        contentRow.add(progressImage);
        contentRow.addSpacer(8);

        VerticalStack textCol = contentRow.addColumn(fit(), fit()).setDebugName("textCol").centerVertical();
        textCol.addText(fit(), fit(), description);
        textCol.addSpacer(4);
        progress = textCol.addText(fit(), fit(), getProgressComponent()).setColor(ColorHelper.GRAY);


        grid.addSpacer(20);
        stageRow = grid.addRow(fill(), fit());
        stage = stageRow.addMultilineText(
                fill(), fit(),
                stageComponent != null ? stageComponent : Component.literal("")
        ).centerHorizontal();
        grid.addFiller();

        return grid;
    }
}
