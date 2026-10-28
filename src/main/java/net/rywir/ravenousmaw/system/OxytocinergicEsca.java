package net.rywir.ravenousmaw.system;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.util.Constants;

/**
 * Oxytocinergic Esca: winning a raid under the strongest Bad Omen earns it, and every raid won afterwards
 * adds stacks equal to its Bad Omen level (up to a cap). While a Maw is carried, each stack discounts
 * every villager trade a little more.
 */
public class OxytocinergicEsca {
    /**
     * Called when a player receives Hero of the Village, which vanilla gives to every hero of a won raid
     * with amplifier = Bad Omen level - 1.
     */
    public static void onRaidWon(ServerPlayer player, int raidLevel) {
        if (!player.getData(RavenousAttachments.ASSIMILATIONS).has(Assimilations.OXYTOCINERGIC_ESCA)) {
            // The winning raid itself only earns the assimilation, stacks start with the next one
            if (raidLevel >= Constants.OXYTOCINERGIC_ESCA_MAX_RAID_LEVEL) {
                AssimilationHandler.grant(player, Assimilations.OXYTOCINERGIC_ESCA);
            }
            return;
        }

        int stacks = player.getData(RavenousAttachments.OXYTOCIN_STACKS);
        player.setData(RavenousAttachments.OXYTOCIN_STACKS, Math.min(stacks + raidLevel, Constants.OXYTOCINERGIC_ESCA_MAX_STACKS));
    }

    /**
     * Discounts every offer of the villager the player has just started trading with. Vanilla resets the
     * special prices when the trade ends, so nothing has to be undone.
     */
    public static void applyTradeDiscount(ServerPlayer player, Villager villager) {
        if (!AssimilationHandler.isActive(player, Assimilations.OXYTOCINERGIC_ESCA)) return;

        int stacks = player.getData(RavenousAttachments.OXYTOCIN_STACKS);
        if (stacks <= 0) return;

        for (MerchantOffer offer : villager.getOffers()) {
            // Same shape as the vanilla Hero of the Village discount: a share of the base price, at least 1
            int discount = (int) Math.floor(stacks * Constants.OXYTOCINERGIC_ESCA_DISCOUNT_PER_STACK * offer.getBaseCostA().getCount());
            offer.addToSpecialPriceDiff(-Math.max(discount, 1));
        }
    }
}
