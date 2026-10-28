package net.rywir.ravenousmaw.system;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
import java.util.List;
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
     * Lets the nearest villager toss the player a hero gift (the same loot as vanilla Hero of the Village),
     * once per cooldown. More stacks mean a shorter cooldown. Players who already have the real effect are
     * skipped, vanilla villagers gift them on their own.
     */
    public static void tryGift(ServerPlayer player) {
        if (!AssimilationHandler.isActive(player, Assimilations.OXYTOCINERGIC_ESCA)) return;

        int stacks = player.getData(RavenousAttachments.OXYTOCIN_STACKS);
        if (stacks <= 0 || player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) return;

        long now = player.level().getGameTime();
        long nextGift = player.getData(RavenousAttachments.OXYTOCIN_NEXT_GIFT);
        if (now < nextGift) return;

        // The timer starts at 0 after a relog: wait a full cooldown instead of gifting right away
        int cooldown = Math.max(
            Constants.OXYTOCINERGIC_ESCA_GIFT_BASE_COOLDOWN_TICKS - (stacks - 1) * Constants.OXYTOCINERGIC_ESCA_GIFT_COOLDOWN_STEP_TICKS,
            Constants.OXYTOCINERGIC_ESCA_GIFT_COOLDOWN_STEP_TICKS);
        player.setData(RavenousAttachments.OXYTOCIN_NEXT_GIFT, now + cooldown);
        if (nextGift == 0L) return;

        AABB area = player.getBoundingBox().inflate(Constants.OXYTOCINERGIC_ESCA_GIFT_RADIUS);
        List<Villager> villagers = player.level().getEntitiesOfClass(Villager.class, area, Villager::isAlive);
        villagers.stream()
            .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)))
            .ifPresent(villager -> getGiftItems(villager).forEach(gift -> BehaviorUtils.throwItem(villager, gift, player.position())));
    }

    /**
     * Gift loot of a villager, as in vanilla's GiveGiftToHero: a poppy from babies, the profession's gift
     * table otherwise, and wheat seeds for professions without one. Vanilla keeps its table map private, but
     * the tables follow the pattern gameplay/hero_of_the_village/<profession>_gift.
     */
    private static List<ItemStack> getGiftItems(Villager villager) {
        if (villager.isBaby()) return List.of(new ItemStack(Items.POPPY));

        ServerLevel level = (ServerLevel) villager.level();
        Identifier profession = villager.getVillagerData().profession().unwrapKey().orElseThrow().identifier();
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.withDefaultNamespace("gameplay/hero_of_the_village/" + profession.getPath() + "_gift"));
        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableKey);
        if (table == LootTable.EMPTY) return List.of(new ItemStack(Items.WHEAT_SEEDS));

        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, villager.position())
            .withParameter(LootContextParams.THIS_ENTITY, villager)
            .create(LootContextParamSets.GIFT);
        return table.getRandomItems(params);
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
