package net.rywir.ravenousmaw.content.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;

public class RavenousAttackEntityEvent {
    /**
     * Blocks melee attacks once the Maw is at its protected durability threshold, like every other Maw action.
     * Runs on both sides so the client also plays the warning (see {@link MawItem#canSurvive}).
     */
    @SubscribeEvent
    public static void onAttackEntityEvent(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) return;

        if (stack.getItem() instanceof MawItem maw && !maw.canSurvive(stack, player.level(), player)) {
            event.setCanceled(true);
        }
    }
}
