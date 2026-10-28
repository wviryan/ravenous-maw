package net.rywir.ravenousmaw.content.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.StageHandler;

public class RavenousLivingDamageEvent {
    @SubscribeEvent
    public static void onLivingDamageEvent(LivingDamageEvent.Post event) {
        recordBossContribution(event);

        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack stack = event.getSource().getWeaponItem();

        if (stack == null) return;

        boolean isMaw = stack.is(RavenousItemTagsProvider.MAW);

        if (!isMaw) return;

        float damage = event.getHealthDamage();

        Mutations.PERPETUAL_MOTION.ability().stealLife(stack, damage, player);
    }

    /**
     * Remembers a player who hurt a stage objective boss while carrying a Maw that can feast on it, with any
     * weapon (a bow counts). RavenousLivingDeathEvent ranks up every contributor's Maw when the boss dies.
     */
    private static void recordBossContribution(LivingDamageEvent.Post event) {
        LivingEntity boss = event.getEntity();

        if (!Stages.getOBJECTIVES().contains(boss.getType())) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (StageHandler.findMawSlotFor(player.getInventory(), boss.getType()) < 0) return;

        boss.getData(RavenousAttachments.BOSS_CONTRIBUTORS).add(player.getUUID());
    }
}