package org.betterx.bclib.client.gui.gridlayout;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Deprecated(forRemoval = true)
@Environment(EnvType.CLIENT)
class SignalingCheckBox extends Checkbox {
    private final Consumer<Boolean> onChange;

    public SignalingCheckBox(
            int left,
            int top,
            int width,
            int height,
            Component component,
            boolean checked,
            Consumer<Boolean> onChange
    ) {
        super(left, top, width, height, component, checked);
        this.onChange = onChange;
        if (onChange != null)
            onChange.accept(checked);
    }

    @Override
    public void onPress() {
        super.onPress();
        if (onChange != null)
            onChange.accept(this.selected());
    }
}

@Deprecated(forRemoval = true)
@Environment(EnvType.CLIENT)
public class GridCheckboxCell extends GridCell implements GridWidgetWithEnabledState {
    private boolean checked;
    private Checkbox lastCheckbox;
    private boolean enabled;
    private final float alpha;

    GridCheckboxCell(
            Component text,
            boolean checked,
            float alpha,
            double width,
            GridLayout.GridValueType widthType,
            double height
    ) {
        this(text, checked, alpha, width, widthType, height, null);
    }

    GridCheckboxCell(
            Component text,
            boolean checked,
            float alpha,
            double width,
            GridLayout.GridValueType widthType,
            double height,
            Consumer<Boolean> onChange
    ) {
        super(width, height, widthType, null, null);
        lastCheckbox = null;
        enabled = true;
        this.alpha = alpha;
        this.componentPlacer = (transform) -> {
            Checkbox cb = new SignalingCheckBox(transform.left, transform.top, transform.width, transform.height,
                    text,
                    checked,
                    (state) -> {
                        this.checked = state;
                        if (onChange != null) onChange.accept(state);
                    }
            );
            cb.setAlpha(alpha);
            lastCheckbox = cb;
            setEnabled(enabled);
            return cb;
        };

    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (lastCheckbox != null) {
            lastCheckbox.active = enabled;
            lastCheckbox.setAlpha(enabled ? alpha : (alpha * 0.5f));
        }
        this.enabled = enabled;
    }
}
