package org.betterx.bclib.client.gui.gridlayout;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class GridRow extends GridContainer {
    public final GridLayout.VerticalAlignment alignment;

    GridRow(double width) {
        this(width, GridLayout.VerticalAlignment.TOP);
    }

    GridRow(double width, GridLayout.GridValueType widthType) {
        this(width, widthType, GridLayout.VerticalAlignment.CENTER);
    }

    GridRow(double width, GridLayout.VerticalAlignment alignment) {
        super(width);
        this.alignment = alignment;
    }

    GridRow(double width, GridLayout.GridValueType widthType, GridLayout.VerticalAlignment alignment) {
        super(width, widthType);
        this.alignment = alignment;
    }

    public GridColumn addColumn(double width, GridLayout.GridValueType widthType) {
        GridColumn cell = new GridColumn(width, widthType);
        this.cells.add(cell);
        return cell;
    }

    public GridCell addComponent(double height, Function<GridTransform, Object> componentPlacer) {
        return addComponent(1.0, GridLayout.GridValueType.PERCENTAGE, height, componentPlacer);
    }

    public GridCell addComponent(
            double width,
            GridLayout.GridValueType widthType,
            double height,
            Function<GridTransform, Object> componentPlacer
    ) {
        GridCell cell = new GridCell(width, height, widthType, componentPlacer, null);
        this.cells.add(cell);
        return cell;
    }


    public GridCell addButton(Component text, double height, OnPress onPress) {
        return addButton(text, 1.0, GridLayout.GridValueType.PERCENTAGE, height, onPress);
    }

    public GridCell addButton(Component text, float alpha, double height, OnPress onPress) {
        return addButton(text, alpha, 1.0, GridLayout.GridValueType.PERCENTAGE, height, onPress);
    }

    public GridCell addButton(Component text, double height, Font font, OnPress onPress) {
        return addButton(text, 1.0f, height, font, onPress);
    }

    public GridCell addButton(Component text, float alpha, double height, Font font, OnPress onPress) {
        final int width = font.width(text.getVisualOrderText()) + 24;
        return addButton(text, alpha, width, GridLayout.GridValueType.CONSTANT, height, onPress);
    }

    public GridCell addButton(
            Component text,
            double width,
            GridLayout.GridValueType widthType,
            double height,
            OnPress onPress
    ) {
        return addButton(text, 1.0f, width, widthType, height, onPress);
    }

    public GridCell addButton(
            Component text,
            float alpha,
            double width,
            GridLayout.GridValueType widthType,
            double height,
            OnPress onPress
    ) {
        GridCell cell = new GridCell(width, height, widthType, (transform) -> {
            Button customButton = new Button(
                    transform.left,
                    transform.top,
                    transform.width,
                    transform.height,
                    text,
                    onPress
            );
            customButton.setAlpha(alpha);
            return customButton;
        }, null);
        this.cells.add(cell);
        return cell;
    }

    public GridCheckboxCell addCheckbox(Component text, boolean checked, Font font, Consumer<Boolean> onChange) {
        final int width = font.width(text.getVisualOrderText()) + 24 + 2 * 12;
        return addCheckbox(text, checked, width, widthType, onChange);
    }

    public GridCheckboxCell addCheckbox(
            Component text, boolean checked, double width,
            GridLayout.GridValueType widthType, Consumer<Boolean> onChange
    ) {

        GridCheckboxCell cell = new GridCheckboxCell(text, checked, 1.0f, width, widthType, 20, onChange);
        this.cells.add(cell);
        return cell;
    }

    public GridCheckboxCell addCheckbox(Component text, boolean checked, int height) {
        return addCheckbox(text, checked, 1.0f, height);
    }

    public GridCheckboxCell addCheckbox(Component text, boolean checked, float alpha, int height) {
        return addCheckbox(text, checked, alpha, 1.0, GridLayout.GridValueType.PERCENTAGE, height);
    }

    public GridCheckboxCell addCheckbox(Component text, boolean checked, int height, Font font) {
        return addCheckbox(text, checked, 1.0f, height, font);
    }

    public GridCheckboxCell addCheckbox(Component text, boolean checked, float alpha, int height, Font font) {
        final int width = font.width(text.getVisualOrderText()) + 24 + 2 * 12;
        return addCheckbox(text, checked, alpha, width, GridLayout.GridValueType.CONSTANT, height);
    }

    public GridCheckboxCell addCheckbox(
            Component text,
            boolean checked,
            double width,
            GridLayout.GridValueType widthType,
            int height
    ) {
        return addCheckbox(text, checked, 1.0f, width, widthType, height);
    }

    public GridCheckboxCell addCheckbox(
            Component text,
            boolean checked,
            float alpha,
            double width,
            GridLayout.GridValueType widthType,
            int height
    ) {
        GridCheckboxCell cell = new GridCheckboxCell(text, checked, alpha, width, widthType, height);
        this.cells.add(cell);
        return cell;
    }

    public GridCustomRenderCell addCustomRender(GridCustomRenderCell cell) {
        this.cells.add(cell);
        return cell;
    }

    public GridCell addImage(ResourceLocation location, int width, int height) {
        return addImage(location, 1.0f, width, height);
    }

    public GridCell addImage(ResourceLocation location, float alpha, int width, int height) {
        return addImage(
                location,
                alpha,
                width,
                GridLayout.GridValueType.CONSTANT,
                height,
                0,
                0,
                width,
                height,
                width,
                height
        );
    }

    public GridCell addImage(
            ResourceLocation location,
            double width,
            GridLayout.GridValueType widthType,
            int height,
            int resourceWidth,
            int resourceHeight
    ) {
        return addImage(location, 1.0f, width, widthType, height, resourceWidth, resourceHeight);
    }

    public GridCell addImage(
            ResourceLocation location,
            float alpha,
            double width,
            GridLayout.GridValueType widthType,
            int height,
            int resourceWidth,
            int resourceHeight
    ) {
        return addImage(
                location,
                alpha,
                width,
                widthType,
                height,
                0,
                0,
                resourceWidth,
                resourceWidth,
                resourceWidth,
                resourceHeight
        );
    }

    public GridCell addImage(
            ResourceLocation location,
            double width,
            GridLayout.GridValueType widthType,
            int height,
            int uvLeft,
            int uvTop,
            int uvWidth,
            int uvHeight,
            int resourceWidth,
            int resourceHeight
    ) {
        return addImage(
                location,
                1.0f,
                width,
                widthType,
                height,
                uvLeft,
                uvTop,
                uvWidth,
                uvHeight,
                resourceWidth,
                resourceHeight
        );
    }

    public GridCell addImage(
            ResourceLocation location,
            float alpha,
            double width,
            GridLayout.GridValueType widthType,
            int height,
            int uvLeft,
            int uvTop,
            int uvWidth,
            int uvHeight,
            int resourceWidth,
            int resourceHeight
    ) {
        GridCell cell = new GridImageCell(
                location,
                width,
                widthType,
                height,
                alpha,
                uvLeft,
                uvTop,
                uvWidth,
                uvHeight,
                resourceWidth,
                resourceHeight
        );
        this.cells.add(cell);
        return cell;
    }


    public GridColumn addFiller() {
        return addFiller(1);
    }

    public GridColumn addFiller(float portion) {
        GridColumn cell = new GridColumn(portion, GridLayout.GridValueType.FILL);
        this.cells.add(cell);
        return cell;
    }

    public void addSpacer() {
        addSpacer(12);
    }

    public void addSpacer(int width) {
        GridCell cell = new GridCell(width, 0, GridLayout.GridValueType.CONSTANT, null, null);
        this.cells.add(cell);
    }


    public GridMessageCell addMessage(Component text, Font font, GridLayout.Alignment contentAlignment) {
        return addMessage(text, font, GridLayout.COLOR_WHITE, contentAlignment);
    }

    public GridMessageCell addMessage(Component text, Font font, int color, GridLayout.Alignment contentAlignment) {
        return addMessage(text, 1.0, GridLayout.GridValueType.PERCENTAGE, font, color, contentAlignment);
    }

    public GridMessageCell addMessage(
            Component text,
            double width,
            GridLayout.GridValueType widthType,
            Font font,
            GridLayout.Alignment contentAlignment
    ) {
        return addMessage(text, width, widthType, font, GridLayout.COLOR_WHITE, contentAlignment);
    }

    public GridMessageCell addMessage(
            Component text,
            double width,
            GridLayout.GridValueType widthType,
            Font font,
            int color,
            GridLayout.Alignment contentAlignment
    ) {
        GridMessageCell cell = new GridMessageCell(width, widthType, contentAlignment, font, text, color);
        this.cells.add(cell);
        return cell;
    }

    public GridStringCell addString(Component text, GridScreen parent) {
        return this.addString(text, GridLayout.COLOR_WHITE, parent);
    }


    public GridStringCell addString(Component text, int color, GridScreen parent) {
        final int width = parent.getWidth(text);
        return this.addString(
                text,
                width,
                GridLayout.GridValueType.CONSTANT,
                color,
                GridLayout.Alignment.CENTER,
                parent
        );
    }

    public GridStringCell addString(Component text, GridLayout.Alignment contentAlignment, GridScreen parent) {
        return this.addString(text, GridLayout.COLOR_WHITE, contentAlignment, parent);
    }

    public GridStringCell addString(
            Component text,
            int color,
            GridLayout.Alignment contentAlignment,
            GridScreen parent
    ) {
        return this.addString(text, 1.0, GridLayout.GridValueType.PERCENTAGE, color, contentAlignment, parent);
    }

    public GridStringCell addString(
            Component text,
            double width,
            GridLayout.GridValueType widthType,
            GridLayout.Alignment contentAlignment,
            GridScreen parent
    ) {
        return addString(text, width, widthType, GridLayout.COLOR_WHITE, contentAlignment, parent);
    }

    public GridStringCell addString(
            Component text,
            double width,
            GridLayout.GridValueType widthType,
            int color,
            GridLayout.Alignment contentAlignment,
            GridScreen parent
    ) {
        GridStringCell cell = new GridStringCell(
                width,
                widthType,
                parent.getFont().lineHeight,
                contentAlignment,
                parent,
                text,
                color
        );
        this.cells.add(cell);
        return cell;
    }

    @Override
    protected GridElement buildElementAt(int inLeft, int top, int width, final List<GridElement> collector) {
        int height = 0;
        int left = inLeft;
        if (widthType == GridLayout.GridValueType.INHERIT) {
            final int originalWidth = width;
            width = cells.stream()
                         .filter(row -> row.widthType == GridLayout.GridValueType.CONSTANT || row.widthType == GridLayout.GridValueType.INHERIT)
                         .map(row -> row.buildElement(0, 0, 1, 0, 0, null).width)
                         .reduce(0, (p, c) -> p + c);
        }

        final int inheritedWidth = width;
        final int fixedWidth = cells.stream()
                                    .filter(col -> col.widthType != GridLayout.GridValueType.FILL)
                                    .map(col -> col.calculateWidth(inheritedWidth))
                                    .reduce(0, (p, c) -> p + c);
        final float autoWidthSum = cells.stream()
                                        .filter(col -> col.widthType == GridLayout.GridValueType.FILL)
                                        .map(col -> col.width)
                                        .reduce(0.0f, (p, c) -> p + c);
        final int autoWidth = width - fixedWidth;

        if (alignment == GridLayout.VerticalAlignment.TOP) {
            for (GridCellDefinition col : cells) {
                GridElement element = col.buildElement(width, autoWidth, autoWidthSum, left, top, collector);
                left += element.width;
                height = Math.max(height, element.height);
            }
        } else {
            //first iteration will collect heights, second one will transform top position for alignment
            Map<GridCellDefinition, GridElement> cache = new HashMap<>();
            for (GridCellDefinition col : cells) {
                GridElement element = col.buildElement(width, autoWidth, autoWidthSum, left, top, null);
                left += element.width;
                height = Math.max(height, element.height);
                cache.put(col, element);
            }

            left = inLeft;
            for (GridCellDefinition col : cells) {
                GridElement element = cache.get(col);
                final int topOffset = (alignment == GridLayout.VerticalAlignment.BOTTOM)
                        ? (height - element.height)
                        : (height - element.height) >> 1;
                element = col.buildElement(width, autoWidth, autoWidthSum, left, top + topOffset, collector);
                left += element.width;
            }
        }


        return new GridElement(inLeft, top, width, height);
    }
}
