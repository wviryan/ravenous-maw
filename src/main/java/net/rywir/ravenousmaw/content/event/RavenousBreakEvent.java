package net.rywir.ravenousmaw.content.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RavenousBreakEvent {
    private static final Map<UUID, Set<BlockPos>> HARVESTED_BLOCKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_WARNING_TICK = new ConcurrentHashMap<>();
    private static final Component LOW_DURABILITY_WARNING = Component.translatable("ravenousmaw.tectonic_low_durability");

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(stack);

        if (!mutationHandler.has(Mutations.TECTONIC_BITE)) {
            return;
        }

        int range = mutationHandler.getConfigVal(Mutations.Parameters.TECTONIC_AREA);

        // Sneaking forces a single-block break.
        if (range <= 1 || serverPlayer.isShiftKeyDown()) {
            return;
        }

        BlockPos initialBlockPos = event.getPos();
        UUID playerUUID = serverPlayer.getUUID();

        Set<BlockPos> playerHarvestedBlocks = HARVESTED_BLOCKS.computeIfAbsent(
            playerUUID,
            k -> ConcurrentHashMap.newKeySet()
        );

        if (!playerHarvestedBlocks.add(initialBlockPos)) {
            return;
        }

        try {
            List<BlockPos> extraBlocks = new ArrayList<>();

            for (BlockPos pos : MawItem.getBlocksToBeDestroyed(range, initialBlockPos, serverPlayer)) {
                if (!pos.equals(initialBlockPos) &&
                    stack.isCorrectToolForDrops(event.getLevel().getBlockState(pos))) {
                    extraBlocks.add(pos);
                }
            }

            // Each block costs 1 durability (the initial block included). Area breaking is all or nothing:
            // if the Maw cannot pay for the whole area and stay above its protected threshold, only the
            // initial block breaks.
            if (!MawItem.canAfford(stack, extraBlocks.size() + 1)) {
                warnLowDurability(serverPlayer);
                return;
            }

            for (BlockPos pos : extraBlocks) {
                if (playerHarvestedBlocks.add(pos)) {
                    try {
                        serverPlayer.gameMode.destroyBlock(pos);
                    } finally {
                        playerHarvestedBlocks.remove(pos);
                    }
                }
            }
        }

        finally {
            playerHarvestedBlocks.remove(initialBlockPos);

            if (playerHarvestedBlocks.isEmpty()) {
                HARVESTED_BLOCKS.remove(playerUUID);
            }
        }
    }

    /**
     * Tells the player why the area break fell back to one block. Rate limited per player so holding the
     * mine button does not repeat it for every block.
     */
    private static void warnLowDurability(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long last = LAST_WARNING_TICK.get(player.getUUID());

        if (last != null && now - last < Constants.TECTONIC_BITE_WARNING_COOLDOWN_TICKS) {
            return;
        }

        LAST_WARNING_TICK.put(player.getUUID(), now);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.25F, 0.5F);
        player.displayClientMessage(LOW_DURABILITY_WARNING, true);
    }
}
