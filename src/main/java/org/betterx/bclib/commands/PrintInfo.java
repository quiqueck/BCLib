package org.betterx.bclib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;

public class PrintInfo {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> bnContext) {
        return bnContext
                .then(Commands.literal("print")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .then(Commands.literal("dimensions")
                                            .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                                            .executes(PrintInfo::printDimensions)
                              )
                );
    }

    static int printDimensions(CommandContext<CommandSourceStack> ctx) {
        MutableComponent result = Component.literal("World Dimensions: ")
                                           .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE));

        for (var serverLevel : ctx.getSource().getLevel().getServer().getAllLevels()) {
            var generator = serverLevel.getChunkSource().getGenerator();
            String output = "\n - " + serverLevel.dimension().location().toString() + ": " +
                    "\n     " + generator.toString().trim() + " " +
                    generator
                            .getBiomeSource()
                            .toString()
                            .replace("\n", "\n     ");
            var cl = ChatFormatting.LIGHT_PURPLE;
            if (serverLevel.dimension().location().equals(Level.OVERWORLD.location()))
                cl = ChatFormatting.WHITE;
            else if (serverLevel.dimension().location().equals(Level.NETHER.location()))
                cl = ChatFormatting.RED;
            if (serverLevel.dimension().location().equals(Level.END.location()))
                cl = ChatFormatting.YELLOW;
            Component dimComponent = Component.literal(output)
                                              .setStyle(Style.EMPTY.withBold(false).withColor(cl));
            result.append(dimComponent);
        }
        ctx.getSource().sendSuccess(() -> result, false);
        return Command.SINGLE_SUCCESS;
    }

}
