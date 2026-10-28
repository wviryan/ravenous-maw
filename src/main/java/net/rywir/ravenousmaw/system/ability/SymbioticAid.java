package net.rywir.ravenousmaw.system.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousMobEffects;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SymbioticAid implements IMutationAbility {
    // Immunity is applied by cleanse(), called every tick from RavenousPlayerTickEvent.
    private static final int DURATION = 8 * 20;
    private static final Component ABSORBED_MESSAGE = Component.translatable("ravenousmaw.symbiotic_aid_absorbed");

    /** Game time of the last "effect absorbed" notice per player. */
    private static final Map<UUID, Long> LAST_ABSORB_NOTICE_TICK = new ConcurrentHashMap<>();

    /**
     * Symbiotic Immunity: removes every harmful effect from a player holding a Maw with Symbiotic Aid
     * (either hand, since drinking a potion occupies one) while the Immunity toggle is ON.
     * It runs every tick so effects received before the Maw was picked up are cleansed too.
     * The Maw's own Symbiotic Infection stays, and so does Nausea while it is active, because the
     * instability "puke" applies both together.
     * Absorbing an effect plays a chime and shows an action-bar message, rate limited per player.
     */
    public static void cleanse(Player player) {
        if (player.getActiveEffects().isEmpty()) return;

        MutationHandler handler = null;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);

            if (!held.is(RavenousItemTagsProvider.MAW)) continue;

            MutationHandler candidate = new MutationHandler(held);

            if (candidate.has(Mutations.SYMBIOTIC_AID)) {
                handler = candidate;
                break;
            }
        }

        if (handler == null) return;

        if (handler.getConfigVal(Mutations.Parameters.SYMBIOTIC_IMMUNITY) == 0) return;

        boolean isInfected = player.hasEffect(RavenousMobEffects.SYMBIOTIC_INFECTION);

        boolean absorbed = false;

        // Copied first, because removing an effect modifies the live collection.
        for (MobEffectInstance instance : List.copyOf(player.getActiveEffects())) {
            MobEffect effect = instance.getEffect().value();

            if (effect.getCategory() != MobEffectCategory.HARMFUL) continue;
            if (effect == RavenousMobEffects.SYMBIOTIC_INFECTION.get()) continue;
            if (isInfected && effect == MobEffects.NAUSEA.value()) continue;

            player.removeEffect(instance.getEffect());
            absorbed = true;
        }

        if (absorbed && player instanceof ServerPlayer serverPlayer) {
            notifyAbsorbed(serverPlayer);
        }
    }

    /** Tells the player the Maw removed an effect. Rate limited so a lingering source does not spam it. */
    private static void notifyAbsorbed(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long last = LAST_ABSORB_NOTICE_TICK.get(player.getUUID());

        if (last != null && now - last < Constants.SYMBIOTIC_AID_ABSORB_COOLDOWN_TICKS) return;

        LAST_ABSORB_NOTICE_TICK.put(player.getUUID(), now);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.sendOverlayMessage(ABSORBED_MESSAGE);
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (player.level().isClientSide()) return;

        boolean hasSymbioticAid = mutationHandler.has(Mutations.SYMBIOTIC_AID);

        if (!hasSymbioticAid) return;

        player.addEffect(new MobEffectInstance(RavenousMobEffects.SYMBIOTIC_INFECTION, DURATION));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, DURATION));
        player.sendOverlayMessage(Component.translatable("instability_message.ravenousmaw.symbiotic_aid"));
    }
}
