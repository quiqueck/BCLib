package org.betterx.bclib.commands.arguments;

import org.betterx.bclib.commands.PlaceCommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * Connector argument backed by a vanilla {@link ResourceLocationArgument} with server-side suggestions.
 * <p>
 * A custom {@link com.mojang.brigadier.arguments.ArgumentType} would end up in the command tree that is
 * sent to clients, and proxies like Velocity can not decode unknown argument types (#124).
 */
public final class ConnectorArgument {
    private ConnectorArgument() {
    }

    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        ResourceLocation pool = null;
        try {
            pool = context.getArgument(PlaceCommand.POOL, ResourceKey.class).location();
        } catch (Throwable t) {
            pool = new ResourceLocation("-", "");
        }
        return SharedSuggestionProvider.suggest(getStrings(
                pool.getNamespace(),
                List.of("bottom", "building_entrance", "street")
        ), builder);
    }

    @NotNull
    private static List<String> getStrings(String namespace, List<String> input) {
        return input.stream().map(s -> namespace + ":" + s).toList();
    }

    public static ResourceLocationArgument id() {
        return ResourceLocationArgument.id();
    }
}
