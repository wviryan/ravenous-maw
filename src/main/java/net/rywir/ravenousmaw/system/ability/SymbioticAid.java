package net.rywir.ravenousmaw.system.ability;

import net.minecraft.network.chat.Component;
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

import java.util.List;

public class SymbioticAid implements IMutationAbility {
    // Immunity is applied by cleanse(), called every tick from RavenousPlayerTickEvent.
    private static final int DURATION = 8 * 20;

    /**
     * Symbiotic Immunity: removes every harmful effect from a player holding a Maw with Symbiotic Aid
     * (either hand, since drinking a potion occupies one) while the Immunity toggle is ON.
     * It runs every tick so effects received before the Maw was picked up are cleansed too.
     * The Maw's own Symbiotic Infection stays, and so does Nausea while it is active, because the
     * instability "puke" applies both together.
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

        // Copied first, because removing an effect modifies the live collection.
        for (MobEffectInstance instance : List.copyOf(player.getActiveEffects())) {
            MobEffect effect = instance.getEffect().value();

            if (effect.getCategory() != MobEffectCategory.HARMFUL) continue;
            if (effect == RavenousMobEffects.SYMBIOTIC_INFECTION.get()) continue;
            if (isInfected && effect == MobEffects.CONFUSION.value()) continue;

            player.removeEffect(instance.getEffect());
        }
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (player.level().isClientSide) return;

        boolean hasSymbioticAid = mutationHandler.has(Mutations.SYMBIOTIC_AID);

        if (!hasSymbioticAid) return;

        player.addEffect(new MobEffectInstance(RavenousMobEffects.SYMBIOTIC_INFECTION, DURATION));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, DURATION));
        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.symbiotic_aid"), true);
    }
}
