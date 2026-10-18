package org.betterx.bclib.commands.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Placement argument backed by a vanilla {@link StringArgumentType} with server-side suggestions.
 * <p>
 * A custom {@link com.mojang.brigadier.arguments.ArgumentType} would end up in the command tree that is
 * sent to clients, and proxies like Velocity can not decode unknown argument types (#124).
 */
public final class TemplatePlacementArgument {
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
            value -> Component.translatable("argument.enum.invalid", value)
    );

    private TemplatePlacementArgument() {
    }

    public static StringArgumentType templatePlacement() {
        return StringArgumentType.word();
    }

    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(PlacementDirections.values()).map(PlacementDirections::getSerializedName),
                builder
        );
    }

    public static PlacementDirections getPlacement(
            CommandContext<CommandSourceStack> commandContext,
            String string
    ) throws CommandSyntaxException {
        final String value = StringArgumentType.getString(commandContext, string);
        for (PlacementDirections dir : PlacementDirections.values()) {
            if (dir.getSerializedName().equals(value)) return dir;
        }
        throw ERROR_INVALID_VALUE.create(value);
    }
}
