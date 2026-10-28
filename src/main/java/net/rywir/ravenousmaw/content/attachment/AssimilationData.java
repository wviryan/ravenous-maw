package net.rywir.ravenousmaw.content.attachment;

import com.mojang.serialization.Codec;
import net.rywir.ravenousmaw.registry.Assimilations;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Attached to a player: the assimilations they have earned. Saved with the player and copied on death
 * (see RavenousAttachments), so it follows the player whatever Maw they carry.
 */
public class AssimilationData {
    // Saved as keys, so reordering the enum never corrupts old saves and an unknown key is simply dropped
    public static final Codec<AssimilationData> CODEC = Codec.STRING.listOf().xmap(
        AssimilationData::fromKeys,
        AssimilationData::toKeys
    );

    private final Set<Assimilations> earned;

    public AssimilationData() {
        this(EnumSet.noneOf(Assimilations.class));
    }

    private AssimilationData(Set<Assimilations> earned) {
        this.earned = earned;
    }

    public static AssimilationData fromKeys(List<String> keys) {
        Set<Assimilations> earned = EnumSet.noneOf(Assimilations.class);
        for (String key : keys) {
            Assimilations assimilation = Assimilations.byKey(key);
            if (assimilation != null) earned.add(assimilation);
        }
        return new AssimilationData(earned);
    }

    public List<String> toKeys() {
        return earned.stream().map(Assimilations::getKey).toList();
    }

    public boolean has(Assimilations assimilation) {
        return earned.contains(assimilation);
    }

    /** @return true if it was newly earned, false if the player already had it */
    public boolean grant(Assimilations assimilation) {
        return earned.add(assimilation);
    }

    public int count() {
        return earned.size();
    }

    public List<Assimilations> getEarned() {
        return List.copyOf(earned);
    }
}
