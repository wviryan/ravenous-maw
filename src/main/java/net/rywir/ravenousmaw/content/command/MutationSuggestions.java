package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.rywir.ravenousmaw.registry.Mutations;

import java.util.Arrays;

public class MutationSuggestions {
    /**
     * Suggests mutation keys that match the text typed so far.
     * {@code SuggestionsBuilder.suggest} does not filter, so the vanilla helper is used to do it.
     */
    public static final SuggestionProvider<CommandSourceStack> PROVIDER = (context, builder) ->
        SharedSuggestionProvider.suggest(Arrays.stream(Mutations.values()).map(Mutations::key), builder);
}
