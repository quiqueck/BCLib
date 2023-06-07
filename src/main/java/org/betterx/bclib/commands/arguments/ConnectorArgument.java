package org.betterx.bclib.commands.arguments;

import org.betterx.bclib.commands.PlaceCommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class ConnectorArgument extends ResourceLocationArgument {
    private static final Collection<String> EXAMPLES = Arrays.asList("-:building_entrance", "-:bottom", "-:street");

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
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
    private List<String> getStrings(String namespace, List<String> input) {
        return input.stream().map(s -> namespace + ":" + s).toList();
    }

    public static ConnectorArgument id() {
        return new ConnectorArgument();
    }
}
