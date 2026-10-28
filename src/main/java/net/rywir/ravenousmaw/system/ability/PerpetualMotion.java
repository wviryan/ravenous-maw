package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class PerpetualMotion implements IMutationAbility {
    private static final float DIVIDER = 2;
    private static final int SPEED_DURATION = 3 * 20;

    public void stealLife(ItemStack maw, float damage, Player player) {
        MutationHandler handler = new MutationHandler(maw);

        boolean hasPerpetualMotion = handler.has(Mutations.PERPETUAL_MOTION);

        if (!hasPerpetualMotion) return;

        player.heal(damage / DIVIDER);
        player.getFoodData().eat(Items.PUMPKIN_PIE.components().get(DataComponents.FOOD));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, SPEED_DURATION));
    }
}
