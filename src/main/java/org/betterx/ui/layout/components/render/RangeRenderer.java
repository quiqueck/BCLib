package org.betterx.ui.layout.components.render;

import org.betterx.ui.layout.components.AbstractVanillaComponentRenderer;
import org.betterx.ui.layout.components.Range;
import org.betterx.ui.vanilla.Slider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RangeRenderer<N extends Number> extends AbstractVanillaComponentRenderer<Slider<N>, Range<N>> {

}
