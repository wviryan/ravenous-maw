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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RavenousLivingDeathEvent {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = event.getSource().getEntity() instanceof Player player ? player : null;

        if (killer != null) {
            countVoracityKill(killer);
        }

        // Aquanaut Coalescence: the deed is slaying an Elder Guardian, with or without a Maw.
        if (dead.getType() == EntityType.ELDER_GUARDIAN && killer instanceof ServerPlayer player) {
            AssimilationHandler.grant(player, Assimilations.AQUANAUT_COALESCENCE);
        }

        if (!Stages.getOBJECTIVES().contains(dead.getType())) return;
        if (!(dead.level() instanceof ServerLevel level)) return;

        // Everyone who hurt the boss while carrying a fitting Maw shares the feast, plus the killer.
        Set<UUID> credited = new HashSet<>(dead.getData(RavenousAttachments.BOSS_CONTRIBUTORS).getPlayers());

        if (killer != null) {
            credited.add(killer.getUUID());
        }

        for (UUID id : credited) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);

            // A contributor who logged off or left the dimension misses the feast.
            if (player != null && player.level() == level) {
                stageUpMaw(player, dead.getType(), level);
            }
        }
    }

    private static void countVoracityKill(Player killer) {
        ItemStack stack = killer.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) return;

        MutationHandler mutationHandler = new MutationHandler(stack);

        if (mutationHandler.has(Mutations.INSATIABLE_VORACITY)) {
            mutationHandler.increaseKill();
        }
    }

    /** Ranks up the player's Maw that has {@code boss} as its objective, if they carry one. */
    private static void stageUpMaw(ServerPlayer player, EntityType<?> boss, ServerLevel level) {
        int slot = StageHandler.findMawSlotFor(player.getInventory(), boss);

        if (slot < 0) return;

        ItemStack stack = player.getInventory().getItem(slot);
        StageHandler handler = new StageHandler(stack);
        Stages stage = handler.getStage();

        player.getInventory().setItem(slot, handler.stageUp(stage.getId(), stack, level.registryAccess()));
        announceStageUp(player, Stages.byId(stage.getId() + 1));
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
