package net.rywir.ravenousmaw.system.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Configs;

public class ResonantRending implements IMutationAbility {
    @Override
    public float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        boolean hasRending = handler.has(Mutations.RESONANT_RENDING);

        if (!hasRending) {
            return 0.0F;
        }

        float maxHealth = target.getMaxHealth();

        return maxHealth * ((float) Configs.RESONANT_RENDING_PERCENTAGE.getAsDouble());
    }
}
