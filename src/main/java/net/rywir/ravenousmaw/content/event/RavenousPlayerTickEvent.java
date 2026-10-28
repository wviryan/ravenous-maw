package net.rywir.ravenousmaw.content.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.rywir.ravenousmaw.system.RavenousEnergyStorage;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.OxytocinergicEsca;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.system.ability.SymbioticAid;
import net.rywir.ravenousmaw.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RavenousPlayerTickEvent {
    private static final int ENERGY_COST_PER_OPERATION = 5;
    private static final int AQUANAUT_REFRESH_TICKS = 20;
    private static final int GIFT_CHECK_TICKS = 20;
    private static final int AQUANAUT_EFFECT_TICKS = 60;

    @SubscribeEvent
    public static void onPlayerTickEvent(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;

        SymbioticAid.cleanse(player);
        applyAquanaut(player);
        if (player instanceof ServerPlayer serverPlayer && player.tickCount % GIFT_CHECK_TICKS == 0) {
            OxytocinergicEsca.tryGift(serverPlayer);
        }
        if (player instanceof ServerPlayer serverPlayer && player.tickCount % Constants.ELDRITCH_ANTENNA_CHECK_TICKS == 0) {
            AssimilationHandler.checkEldritchAntenna(serverPlayer);
        }

        ItemStack stack = player.getMainHandItem();

        boolean isMaw = stack.is(RavenousItemTagsProvider.MAW);

        if (!isMaw) return;

        MutationHandler mutationHandler = new MutationHandler(stack);
        boolean hasElectricMending = mutationHandler.has(Mutations.ELECTRIC_MENDING);
        if (!hasElectricMending) return;

        int isMagneticFieldActive = mutationHandler.getConfigVal(Mutations.Parameters.MAGNETIC_FIELD);
        if (isMagneticFieldActive == 0) return;

        int range = mutationHandler.getConfigVal(Mutations.Parameters.MAGNETIC_RANGE);

        AABB area = player.getBoundingBox().inflate(range);
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, area);

        var energy = RavenousEnergyStorage.of(stack);

        if (energy == null) return;

        StageHandler stageHandler = new StageHandler(stack);
        Stages stage = stageHandler.getStage();

        double scale = switch (stage) {
            case LATENT -> 0.05D;
            case ADVANCED -> 0.10D;
            case NOBLE -> 0.15D;
            case EXCELSIOR -> 0.20D;
        };

        for (ItemEntity item : items) {
            if (energy.getAmountAsInt() < ENERGY_COST_PER_OPERATION) return;

            if (!canReachPlayer(player, item)) continue;

            Vec3 motion = new Vec3(
                player.getX() - item.getX(),
                player.getY() + 0.5D - item.getY(),
                player.getZ() - item.getZ()
            ).normalize().scale(scale);

            item.setDeltaMovement(motion);
            item.needsSync = true;

            RavenousEnergyStorage.extract(energy, ENERGY_COST_PER_OPERATION);
        }

        List<ExperienceOrb> orbs = player.level().getEntitiesOfClass(ExperienceOrb.class, area);
        for (ExperienceOrb orb : orbs) {
            if (energy.getAmountAsInt() < ENERGY_COST_PER_OPERATION) return;

            Vec3 motion = new Vec3(
                player.getX() - orb.getX(),
                player.getY() + 0.5D - orb.getY(),
                player.getZ() - orb.getZ()
            ).normalize().scale(0.25D);

            orb.setDeltaMovement(motion);

            RavenousEnergyStorage.extract(energy, ENERGY_COST_PER_OPERATION);
        }
    }

    /**
     * Aquanaut Coalescence: while in water, keeps hidden Water Breathing and Dolphin's Grace on the player.
     * The effects are short and renewed every second, so they fade soon after leaving the water or dropping the Maw.
     */
    private static void applyAquanaut(Player player) {
        if (player.tickCount % AQUANAUT_REFRESH_TICKS != 0) return;
        if (!player.isInWater()) return;
        if (!AssimilationHandler.isActive(player, Assimilations.AQUANAUT_COALESCENCE)) return;

        // Ambient, no particles, no HUD icon: the trait is part of the Maw, not a potion.
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, AQUANAUT_EFFECT_TICKS, 0, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, AQUANAUT_EFFECT_TICKS, 0, true, false, false));
    }

    private static boolean canReachPlayer(@NotNull Player player, @NotNull ItemEntity target) {
        Vec3 start = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 end = player.getEyePosition();

        ClipContext ctx = new ClipContext(
            start, end,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            target
        );

        BlockHitResult hit = target.level().clip(ctx);

        return hit.getType() == HitResult.Type.MISS;
    }
}
