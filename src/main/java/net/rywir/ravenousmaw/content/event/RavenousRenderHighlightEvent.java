package net.rywir.ravenousmaw.content.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;

import java.util.List;

public class RavenousRenderHighlightEvent {
    private static final float RED = 0.0F;
    private static final float GREEN = 0.0F;
    private static final float BLUE = 0.0F;
    private static final float ALPHA = 0.2F;

    @SubscribeEvent
    public static void onRenderHighlightEvent(RenderHighlightEvent.Block event) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();

        if (!(heldItem.getItem() instanceof MawItem)) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(heldItem);

        boolean hasTectonicBite = mutationHandler.has(Mutations.TECTONIC_BITE);

        if (!hasTectonicBite) {
            return;
        }

        int range = mutationHandler.getConfigVal(Mutations.Parameters.TECTONIC_AREA);

        // Sneaking breaks a single block (see RavenousBreakEvent), so there is no area to preview.
        if (range <= 1 || player.isShiftKeyDown()) {
            return;
        }

        BlockPos targetPos = event.getTarget().getBlockPos();
        List<BlockPos> blocksToDestroy = MawItem.getBlocksToBeDestroyed(range, targetPos, player);

        Vec3 cameraPos = event.getCamera().getPosition();
        VertexConsumer lineRenderer = event.getMultiBufferSource().getBuffer(RenderType.lines());

        for (BlockPos pos : blocksToDestroy) {
            if (pos.equals(targetPos)) {
                continue;
            }

            renderBlockOutline(
                event.getPoseStack(),
                lineRenderer,
                player.level(),
                pos,
                cameraPos,
                player
            );
        }
    }

    private static void renderBlockOutline(
        PoseStack poseStack,
        VertexConsumer consumer,
        Level level,
        BlockPos blockPos,
        Vec3 cameraPos,
        Player player
    ) {
        BlockState blockState = level.getBlockState(blockPos);
        VoxelShape shape = blockState.getShape(level, blockPos, CollisionContext.of(player));

        double offsetX = blockPos.getX() - cameraPos.x;
        double offsetY = blockPos.getY() - cameraPos.y;
        double offsetZ = blockPos.getZ() - cameraPos.z;

        renderVoxelShape(poseStack, consumer, shape, offsetX, offsetY, offsetZ);
    }

    private static void renderVoxelShape(
        PoseStack poseStack,
        VertexConsumer consumer,
        VoxelShape shape,
        double offsetX,
        double offsetY,
        double offsetZ
    ) {
        PoseStack.Pose pose = poseStack.last();

        shape.forAllEdges((startX, startY, startZ, endX, endY, endZ) -> {
            float deltaX = (float) (endX - startX);
            float deltaY = (float) (endY - startY);
            float deltaZ = (float) (endZ - startZ);

            float length = Mth.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            float normalX = deltaX / length;
            float normalY = deltaY / length;
            float normalZ = deltaZ / length;

            consumer
                .addVertex(pose, (float) (startX + offsetX), (float) (startY + offsetY), (float) (startZ + offsetZ))
                .setColor(RED, GREEN, BLUE, ALPHA)
                .setNormal(pose, normalX, normalY, normalZ);

            consumer
                .addVertex(pose, (float) (endX + offsetX), (float) (endY + offsetY), (float) (endZ + offsetZ))
                .setColor(RED, GREEN, BLUE, ALPHA)
                .setNormal(pose, normalX, normalY, normalZ);
        });
    }
}