package org.betterx.bclib.client.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

/**
 * @deprecated Please use {@link org.betterx.ui.layout.components.VerticalStack} instead
 */
@Deprecated(forRemoval = true)
@Environment(EnvType.CLIENT)
public class GridColumn extends GridContainer {
    GridColumn(double width) {
        super(width);
    }

    GridColumn(double width, GridLayout.GridValueType widthType) {
        super(width, widthType);
    }

    public GridRow addRow() {
        return addRow(GridLayout.VerticalAlignment.TOP);
    }

    public GridRow addRow(GridLayout.VerticalAlignment align) {
        GridRow row = new GridRow(
                1.0,
                widthType == GridLayout.GridValueType.INHERIT
                        ? GridLayout.GridValueType.INHERIT
                        : GridLayout.GridValueType.PERCENTAGE,
                align
        );
        this.cells.add(row);
        return row;
    }


    public void addSpacerRow() {
        this.addSpacerRow(4);
    }

    public void addSpacerRow(int height) {
        GridCell cell = new GridCell(1.0, height, GridLayout.GridValueType.PERCENTAGE, null, null);
        this.cells.add(cell);
    }

    @Override
    public int calculateWidth(final int parentWidth) {
        if (widthType == GridLayout.GridValueType.INHERIT) {
            return cells.stream()
                        .filter(row -> row.widthType == GridLayout.GridValueType.INHERIT)
                        .map(row -> row.buildElement(0, 0, 1, 0, 0, null).width)
                        .reduce(0, (p, c) -> Math.max(p, c));

        } else {
            return super.calculateWidth(parentWidth);
        }
    }


    @Override
    protected GridElement buildElementAt(int left, int inTop, int width, final List<GridElement> collector) {
        int height = 0;
        int top = inTop;

        if (widthType == GridLayout.GridValueType.INHERIT) {
            width = calculateWidth(width);
        }

        for (GridCellDefinition row : cells) {
            GridElement element = row.buildElement(width, 0, 1, left, top, collector);
            top += element.height;
            height += element.height;
        }

        return new GridElement(left, inTop, width, height);
    }
}
