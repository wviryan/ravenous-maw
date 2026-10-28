package net.rywir.ravenousmaw.content.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.entity.IrisBulletEntity;
import net.rywir.ravenousmaw.content.entity.model.IrisBulletModel;

public class IrisBulletRenderer extends EntityRenderer<IrisBulletEntity, IrisBulletRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/entity/iris_bullet.png");

    private final IrisBulletModel model;

    public IrisBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new IrisBulletModel(context.bakeLayer(IrisBulletModel.LAYER_LOCATION));
    }

    @Override
    public IrisBulletRenderState createRenderState() {
        return new IrisBulletRenderState();
    }

    @Override
    public void extractRenderState(IrisBulletEntity entity, IrisBulletRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
    }

    @Override
    public void submit(IrisBulletRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        poseStack.translate(0, -1.0f, 0);

        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
