package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.content.entity.IrisBulletEntity;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AbilityDispatcher;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class IrisOut implements IMutationAbility {
    private static final float MIN_VELOCITY = 0.6F;
    private static final float MAX_VELOCITY = 1.2F;

    private static final int FULL_CHARGE_TICKS = 20;
    private static final int MIN_CHARGE_TICKS = 10;

    private static final int DURABILITY_COST = 10;

    @Override
    public InteractionResultHolder<ItemStack> use(
        MutationHandler handler,
        ItemStack stack,
        Level level,
        Player player,
        InteractionHand hand
    ) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int left, MutationHandler handler, Stages stage, AbilityDispatcher dispatcher) {
        if (!(entity instanceof ServerPlayer player)) return;

        int used = stack.getUseDuration(entity) - left;
        if (used < MIN_CHARGE_TICKS) return;

        boolean fullyCharged = used >= FULL_CHARGE_TICKS;

        boolean grimTracer = handler.getConfigVal(Mutations.Parameters.GRIM_TRACER) == 1;

        boolean tracing = fullyCharged && grimTracer;

        float velocity = fullyCharged
            ? MAX_VELOCITY
            : MIN_VELOCITY + (MAX_VELOCITY - MIN_VELOCITY) * used / FULL_CHARGE_TICKS;

        if (tracing) {
            velocity = velocity / 1.5F;
        }

        IrisBulletEntity bullet =
            new IrisBulletEntity(player, level, tracing, handler, dispatcher, stage, stack);

        bullet.setPower(fullyCharged ? 1.5F : 1.0F);

        bullet.shootFromRotation(
            player,
            player.getXRot(),
            player.getYRot(),
            0.0F,
            velocity,
            0.5F
        );

        level.addFreshEntity(bullet);

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.FIREWORK_ROCKET_LAUNCH,
            SoundSource.PLAYERS,
            1.0F,
            fullyCharged ? 1.2F : 1.0F
        );

        if (stage != Stages.EXCELSIOR) {
            stack.hurtAndBreak(DURABILITY_COST, player, EquipmentSlot.MAINHAND);
        }
    }

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(DataComponentTypes.CHARGING_SOUND_TYPE, false);
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.remove(DataComponentTypes.CHARGING_SOUND_TYPE);
    }
}
