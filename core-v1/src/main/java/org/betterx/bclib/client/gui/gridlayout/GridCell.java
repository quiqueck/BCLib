package org.betterx.bclib.client.gui.gridlayout;

import org.betterx.bclib.interfaces.TriConsumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
class GridCell extends GridCellDefinition {
    public final float height;
    Function<GridTransform, Object> componentPlacer;
    TriConsumer<PoseStack, GridTransform, Object> customRender;

    GridCell(
            double width,
            double height,
            GridLayout.GridValueType widthType,
            Function<GridTransform, Object> componentPlacer,
            TriConsumer<PoseStack, GridTransform, Object> customRender
    ) {
        super(width, widthType);
        this.height = (float) height;
        this.componentPlacer = componentPlacer;
        this.customRender = customRender;
    }

    protected GridElement buildElementAt(int left, int top, int width, final List<GridElement> collector) {
        return new GridElement(left, top, width, (int) this.height, componentPlacer, customRender);
    }
}
