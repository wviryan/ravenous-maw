package net.rywir.ravenousmaw.content.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;

import java.util.ArrayList;
import java.util.List;

public class RavenousRenderHighlightEvent {
    // Black at 20% opacity (ARGB), as before the port.
    private static final int COLOR = 0x33000000;

    /**
     * Collects the shapes of the extra blocks Tectonic Bite would break and hands them to a renderer. In 26.1
     * the outline is drawn later from extracted state, so nothing but plain shapes may be captured here.
     */
    @SubscribeEvent
    public static void onExtractBlockOutlineRenderStateEvent(ExtractBlockOutlineRenderStateEvent event) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();

        if (!(heldItem.getItem() instanceof MawItem)) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(heldItem);

        if (!mutationHandler.has(Mutations.TECTONIC_BITE)) {
            return;
        }

        int range = mutationHandler.getConfigVal(Mutations.Parameters.TECTONIC_AREA);

        // Sneaking breaks a single block (see RavenousBreakEvent), so there is no area to preview.
        if (range <= 1 || player.isShiftKeyDown()) {
            return;
        }

        BlockPos targetPos = event.getBlockPos();
        Level level = player.level();
        List<Outline> outlines = new ArrayList<>();

        for (BlockPos pos : MawItem.getBlocksToBeDestroyed(range, targetPos, player)) {
            if (pos.equals(targetPos)) {
                continue;
            }

            BlockState blockState = level.getBlockState(pos);
            outlines.add(new Outline(pos, blockState.getShape(level, pos, CollisionContext.of(player))));
        }

        event.addCustomRenderer(new Renderer(outlines));
    }

    private record Outline(BlockPos pos, VoxelShape shape) {}

    private record Renderer(List<Outline> outlines) implements net.neoforged.neoforge.client.CustomBlockOutlineRenderer {
        @Override
        public boolean render(
            BlockOutlineRenderState renderState,
            MultiBufferSource.BufferSource buffer,
            PoseStack poseStack,
            boolean translucentPass,
            LevelRenderState levelRenderState
        ) {
            // Same pass as the vanilla outline of the targeted block.
            if (renderState.isTranslucent() != translucentPass) {
                return false;
            }

            Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
            float lineWidth = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
            VertexConsumer lines = buffer.getBuffer(RenderTypes.lines());

            for (Outline outline : outlines) {
                ShapeRenderer.renderShape(
                    poseStack,
                    lines,
                    outline.shape(),
                    outline.pos().getX() - cameraPos.x,
                    outline.pos().getY() - cameraPos.y,
                    outline.pos().getZ() - cameraPos.z,
                    COLOR,
                    lineWidth
                );
            }

            // Keep the vanilla outline of the targeted block.
            return false;
        }
    }
}
