package net.rywir.ravenousmaw.content.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.rywir.ravenousmaw.system.OxytocinergicEsca;
import net.rywir.ravenousmaw.util.Constants;

/**
 * Oxytocinergic Esca discounts the trades of a villager. The menu opens after vanilla has set its own
 * prices and before the offers are sent to the client, so the discount shows in the trading screen.
 */
public class RavenousContainerOpenEvent {
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getContainer() instanceof MerchantMenu)) return;

        // MerchantMenu hides its trader, so look for the villager that is trading with this player
        player.level()
            .getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(Constants.OXYTOCINERGIC_ESCA_TRADER_SEARCH_RADIUS), villager -> villager.getTradingPlayer() == player)
            .forEach(villager -> OxytocinergicEsca.applyTradeDiscount(player, villager));
    }
}
