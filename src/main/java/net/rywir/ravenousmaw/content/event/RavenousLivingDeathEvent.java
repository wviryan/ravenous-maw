package net.rywir.ravenousmaw.content.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.util.Constants;

public class RavenousLivingDeathEvent {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityType<?> type = event.getEntity().getType();

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(stack);
        Mutations voracity = Mutations.INSATIABLE_VORACITY;

        boolean hasVoracity = mutationHandler.has(voracity);

        if (hasVoracity) {
            mutationHandler.increaseKill();
        }

        if (!Stages.getOBJECTIVES().contains(type)) {
            return;
        }

        StageHandler handler = new StageHandler(stack);
        Stages stage = handler.getStage();

        if (!(stage.getObjective() == type)) {
            return;
        }

        ItemStack newMaw = handler.stageUp(stage.getId(), stack, player.level().registryAccess());

        if (!(player.getMainHandItem().is(RavenousItemTagsProvider.MAW))) {
            return;
        }

        player.setItemSlot(EquipmentSlot.MAINHAND, newMaw);

        if (player instanceof ServerPlayer serverPlayer) {
            announceStageUp(serverPlayer, Stages.byId(stage.getId() + 1));
        }
    }

    /** Totem sound for everyone nearby, a soul particle burst, and a title with the new stage for the player. */
    private static void announceStageUp(ServerPlayer player, Stages newStage) {
        ServerLevel level = player.serverLevel();

        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(),
                Constants.STAGE_UP_PARTICLE_COUNT, 0.5, 0.8, 0.5, 0.05);

        // The subtitle packet must be sent before the title packet, which is what shows both.
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.translatable("ravenousmaw.stage_up_subtitle", newStage.getDisplayComponent())));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("ravenousmaw.stage_up_title")));
    }
}
