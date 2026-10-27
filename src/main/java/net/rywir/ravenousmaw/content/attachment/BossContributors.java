package net.rywir.ravenousmaw.content.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Attached to a stage objective boss: the players who damaged it while carrying a Maw ready to feast on it.
 * Saved with the boss, so a fight that spans a server restart keeps its credit.
 */
public class BossContributors {
    public static final Codec<BossContributors> CODEC = UUIDUtil.CODEC.listOf().xmap(
        list -> new BossContributors(new HashSet<>(list)),
        contributors -> List.copyOf(contributors.players)
    );

    private final Set<UUID> players;

    public BossContributors() {
        this(new HashSet<>());
    }

    private BossContributors(Set<UUID> players) {
        this.players = players;
    }

    public void add(UUID player) {
        players.add(player);
    }

    public Set<UUID> getPlayers() {
        return Set.copyOf(players);
    }
}
