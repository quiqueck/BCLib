package org.betterx.bclib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.QuartPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.phys.Vec3;

public class PrintInfo {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> bnContext) {
        return bnContext
                .then(Commands.literal("print")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .then(Commands.literal("dimensions")
                                            .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                                            .executes(PrintInfo::printDimensions)
                              )
                              .then(Commands.literal("climate")
                                            .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                                            .executes(PrintInfo::printMapValues)
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

    static int printMapValues(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceStack source = ctx.getSource();
        final ServerLevel serverLevel = source.getLevel();
        final Vec3 pos = source.getPosition();

        final RandomState randomState = serverLevel.getChunkSource().randomState();
        final Climate.Sampler sampler = randomState.sampler();
        final Climate.TargetPoint sample = sampler.sample(QuartPos.fromBlock((int) pos.x), QuartPos.fromBlock((int) pos.y), QuartPos.fromBlock((int) pos.z));

        MutableComponent result = Component
                .literal("Samples for " + pos.toString() + " in " + serverLevel
                        .dimension()
                        .location()
                        .toString() + ":\n")
                .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE));
        StringBuilder output = new StringBuilder();
        output.append("temperature: ").append(sample.temperature()).append("\n");
        output.append("humidity: ").append(sample.humidity()).append("\n");
        output.append("continentalness: ").append(sample.continentalness()).append("\n");
        output.append("erosion: ").append(sample.erosion()).append("\n");
        output.append("depth: ").append(sample.depth()).append("\n");
        output.append("weirdness: ").append(sample.weirdness()).append("\n");

        result.append(Component.literal(output.toString())
                               .setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
        ctx.getSource().sendSuccess(() -> result, false);
        return Command.SINGLE_SUCCESS;
    }
}
