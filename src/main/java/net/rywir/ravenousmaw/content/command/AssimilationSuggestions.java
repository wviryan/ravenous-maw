package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.rywir.ravenousmaw.registry.Assimilations;

import java.util.Arrays;

public class AssimilationSuggestions {
    /** Suggests assimilation keys that match the text typed so far (filtered by the vanilla helper). */
    public static final SuggestionProvider<CommandSourceStack> PROVIDER = (context, builder) ->
        SharedSuggestionProvider.suggest(Arrays.stream(Assimilations.values()).map(Assimilations::getKey), builder);
}
