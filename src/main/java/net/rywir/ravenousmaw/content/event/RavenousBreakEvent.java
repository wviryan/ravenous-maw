package net.rywir.ravenousmaw.content.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RavenousBreakEvent {
    private static final Map<UUID, Set<BlockPos>> HARVESTED_BLOCKS = new ConcurrentHashMap<>();

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

        if (range <= 1) {
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
}