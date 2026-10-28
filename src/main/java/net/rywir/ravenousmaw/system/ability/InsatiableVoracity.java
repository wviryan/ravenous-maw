package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Configs;

public class InsatiableVoracity implements IMutationAbility {
    // Look into RavenousLivingDeathEvent to see configuration
    @Override
    public float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        boolean hasInsatiableVoracity = handler.has(Mutations.INSATIABLE_VORACITY);

        if (!hasInsatiableVoracity) {
            return 0.0F;
        }

        float multiplier = switch (stage) {
            case Stages.ADVANCED -> ((float) Configs.INSATIABLE_VORACITY_ADVANCED_DAMAGE_MULTIPLIER.getAsDouble());
            case Stages.NOBLE -> ((float) Configs.INSATIABLE_VORACITY_NOBLE_DAMAGE_MULTIPLIER.getAsDouble());
            case Stages.EXCELSIOR -> ((float) Configs.INSATIABLE_VORACITY_EXCELSIOR_DAMAGE_MULTIPLIER.getAsDouble());
            default -> 0.0F;
        };

        float maxBonus = switch (stage) {
            case Stages.ADVANCED -> ((float) Configs.INSATIABLE_VORACITY_ADVANCED_MAX_DAMAGE.getAsDouble());
            case Stages.NOBLE -> ((float) Configs.INSATIABLE_VORACITY_NOBLE_MAX_DAMAGE.getAsDouble());
            case Stages.EXCELSIOR -> ((float) Configs.INSATIABLE_VORACITY_EXCELSIOR_MAX_DAMAGE.getAsDouble());
            default -> 0.0F;
        };

        float numberOfKills = handler.getKills();
        // The bonus grows with every kill until the stage's ceiling.
        return Math.min(numberOfKills * multiplier, maxBonus);
    }

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(DataComponentTypes.VORACITY_COMPONENT_TYPE, 0);
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.remove(DataComponentTypes.VORACITY_COMPONENT_TYPE);
    }

    @Override
    public void onInstability(MutationHandler handler, ItemStack stack, Player player) {
        if (player.level().isClientSide()) return;

        boolean hasInsatiableVoracity = handler.has(Mutations.INSATIABLE_VORACITY);

        if (!hasInsatiableVoracity) {
            return;
        }

        Holder<DamageType> type = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK);

        DamageSource source = new DamageSource(type, player, player, player.position());

        player.hurt(source, 4.0F);
        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.insatiable_voracity"), true);
    }
}
