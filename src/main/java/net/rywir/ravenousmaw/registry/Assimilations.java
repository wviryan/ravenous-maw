package net.rywir.ravenousmaw.registry;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Traits the Maw absorbs from the world, earned by a deed instead of crafted with a mutagen.
 * Unlike {@link Mutations} they are stored on the player (see AssimilationData), not on the item,
 * so a replacement Maw benefits at once. The stage only places the assimilation in the progression.
 */
public enum Assimilations {
    GILDED_PHEROMONES       (Stages.LATENT),
    NICTITATING_MEMBRANE    (Stages.ADVANCED),
    AQUANAUT_COALESCENCE    (Stages.ADVANCED),
    OXYTOCINERGIC_ESCA      (Stages.NOBLE),
    ELDRITCH_ANTENNA        (Stages.EXCELSIOR);

    private final String    key;
    private final Component title;
    private final Stages    stage;

    private static final Map<String, Assimilations> BY_KEY = new HashMap<>();

    Assimilations(Stages stage) {
        this.key = this.name().toLowerCase(Locale.ROOT);
        this.title = Component.translatable("assimilation.ravenousmaw." + this.key);
        this.stage = stage;
    }

    public @NotNull String getKey() {
        return key;
    }

    public @NotNull String title() {
        return title.getString();
    }

    public @NotNull Stages getStage() {
        return stage;
    }

    public static @Nullable Assimilations byKey(String key) {
        return BY_KEY.get(key);
    }

    static {
        for (Assimilations assimilation : values()) {
            BY_KEY.put(assimilation.key, assimilation);
        }
    }
}
