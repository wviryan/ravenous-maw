package net.rywir.ravenousmaw.system.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class MentalSupremacy implements IMutationAbility {
    private static final int STUN_DURATION = 80;
    private static final int AMPLIFIER = 5;

    @Override
    public boolean interactLivingEntity(ItemStack stack, Player player, Entity entity) {
        MutationHandler handler = new MutationHandler(stack);

        if (!(entity instanceof TamableAnimal tamable)) return false;
        if (tamable.isTame()) return false;

        if (!handler.has(Mutations.MENTAL_SUPREMACY)) return false;
        if (handler.getConfigVal(Mutations.Parameters.DREADFUL_GLANCE) != 1) return false;

        // The client must report success too. Otherwise Minecraft.startUseItem sees PASS for the main hand
        // and sends a second interact packet for the offhand; the now-tamed pet's own mobInteract
        // handles it and toggles it back to standing.
        if (player.level().isClientSide()) return true;

        tamable.tame(player);
        tamable.setOwnerUUID(player.getUUID());
        // Same cleanup as vanilla taming: an angry wolf must not keep chasing its target.
        tamable.getNavigation().stop();
        tamable.setTarget(null);
        tamable.setOrderedToSit(true);

        player.level().broadcastEntityEvent(tamable, (byte)7);

        return true;
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (player.level().isClientSide()) return;

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_DURATION, AMPLIFIER));
        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.mental_supremacy"), true);
    }
}
