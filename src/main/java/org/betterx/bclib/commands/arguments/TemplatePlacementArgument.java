package org.betterx.bclib.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;

public class TemplatePlacementArgument
        extends StringRepresentableArgument<PlacementDirections> {
    private TemplatePlacementArgument() {
        super(PlacementDirections.CODEC, PlacementDirections::values);
    }

    public static TemplatePlacementArgument templatePlacement() {
        return new TemplatePlacementArgument();
    }

    public static PlacementDirections getPlacement(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, PlacementDirections.class);
    }
}
