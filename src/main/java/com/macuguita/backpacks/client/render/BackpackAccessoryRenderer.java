package com.macuguita.backpacks.client.render;

import com.macuguita.backpacks.common.reg.GBComponents;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.Side;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class BackpackAccessoryRenderer implements SimpleAccessoryRenderer {
	@Override
	public <M extends LivingEntity> void render(ItemStack backpack, SlotReference reference, MatrixStack matrices, EntityModel<M> model, VertexConsumerProvider multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!(model instanceof BipedEntityModel<?> humanoid)) return;

		if (!backpack.contains(GBComponents.VISIBLE.get()) || !backpack.contains(GBComponents.BACKPACK_MODEL_ID.get()))
			return;

		if (Boolean.FALSE.equals(backpack.get(GBComponents.VISIBLE.get())))
			return;

		BakedModel backpackModel = BakedModelRenderer.getModel(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));

		matrices.push();

		// Align with body
		align(backpack, reference, humanoid, matrices);

		// FIXME 1.21.9
		// Fabric had this in their example leaving this to remind me later of when it is fixed
		// https://github.com/FabricMC/fabric/blob/0.134.1%2B1.21.10/fabric-model-loading-api-v1/src/testmodClient/java/net/fabricmc/fabric/test/model/loading/BakedModelFeatureRenderer.java
		// FabricBlockModelRenderer.render(matrices.peek(), RenderLayerHelper.entityDelegate(vertexConsumers), model, 1, 1, 1, light, OverlayTexture.DEFAULT_UV, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());

		BakedModelRenderer.drawBakedModel(backpackModel, matrices, multiBufferSource, light, 0xF000F0);

		matrices.pop();
	}

	@Override
	public <M extends LivingEntity> void align(ItemStack itemStack, SlotReference slotReference, EntityModel<M> entityModel, MatrixStack matrixStack) {
		if (!(entityModel instanceof BipedEntityModel<?> humanoid)) return;
		AccessoryRenderer.transformToFace(matrixStack, humanoid.body, Side.BACK);

		// Fix model placement
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
		matrixStack.scale(2.2F, 2.2F, 2.2F);
		matrixStack.translate(0.0F, 0.3F, 0.0F);

	}
}
