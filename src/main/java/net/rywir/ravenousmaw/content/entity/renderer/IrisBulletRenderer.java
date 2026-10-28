package net.rywir.ravenousmaw.content.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.entity.IrisBulletEntity;
import net.rywir.ravenousmaw.content.entity.model.IrisBulletModel;
import org.jetbrains.annotations.NotNull;

public class IrisBulletRenderer extends EntityRenderer<IrisBulletEntity> {
    private IrisBulletModel model;

    public IrisBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new IrisBulletModel(context.bakeLayer(IrisBulletModel.LAYER_LOCATION));
    }

    @Override
    public void render(IrisBulletEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.translate(0, -1.0f, 0);

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(this.getTextureLocation(entity)), false, false);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull IrisBulletEntity entity) {
        return Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/entity/iris_bullet.png");
    }
}