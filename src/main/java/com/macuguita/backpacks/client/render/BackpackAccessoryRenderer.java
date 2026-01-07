package com.macuguita.backpacks.client.render;

import java.util.Optional;

import com.macuguita.backpacks.client.model.GBModelReloadListener;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
//import io.wispforest.accessories.api.client.AccessoriesRenderStateKeys;
//import io.wispforest.accessories.api.client.AccessoryRenderState;
//import io.wispforest.accessories.api.client.renderers.AccessoryRenderer;
//import io.wispforest.accessories.api.client.renderers.SimpleAccessoryRenderer;
//import io.wispforest.accessories.api.client.rendering.Side;
//import io.wispforest.accessories.pond.AccessoriesRenderStateAPI;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BackpackAccessoryRenderer /*implements SimpleAccessoryRenderer*/ {
//
//	@Override
//	public <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryState, S entityState, EntityModel<S> modelId, PoseStack matrices, SubmitNodeCollector collector) {
//		if (!(entityState instanceof HumanoidRenderState humanoidRenderState)) return;
//		if (!(modelId instanceof HumanoidModel<?> humanoid)) return;
//
//		ItemStack chestStack = humanoidRenderState.chestEquipment;
//		if (chestStack.getItem() == Items.ELYTRA)
//			return;
//
//		ItemStack backpack = accessoryState.getStateData(AccessoriesRenderStateKeys.ITEM_STACK);
//		if (backpack.isEmpty())
//			return;
//
//		if (!backpack.has(GBComponents.VISIBLE.get()) || !backpack.has(GBComponents.BACKPACK_MODEL_ID.get()))
//			return;
//
//		if (Boolean.FALSE.equals(backpack.get(GBComponents.VISIBLE.get())))
//			return;
//
//		Optional<BlockStateModel> maybeBackpackModel = GBModelReloadListener.INSTANCE.getModel(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));
//
//		if (maybeBackpackModel.isEmpty())
//			return;
//
//		matrices.pushPose();
//
//		// Align with body
//		align(accessoryState, entityState, modelId, matrices);
//
//		// FIXME 1.21.9
//		// Fabric had this in their example leaving this to remind me later of when it is fixed
//		// https://github.com/FabricMC/fabric/blob/0.134.1%2B1.21.10/fabric-model-loading-api-v1/src/testmodClient/java/net/fabricmc/fabric/test/model/loading/BakedModelFeatureRenderer.java
//		// FabricBlockModelRenderer.render(matrices.peek(), RenderLayerHelper.entityDelegate(vertexConsumers), modelId, 1, 1, 1, light, OverlayTexture.DEFAULT_UV, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());
//
//		collector.order(0).submitBlockModel(
//				matrices,
//				Sheets.cutoutBlockSheet(),
//				maybeBackpackModel.get(),
//				1, 1, 1,
//				// Need to cast because of injected interfaces
//				((AccessoriesRenderStateAPI) humanoidRenderState).getStateData(AccessoriesRenderStateKeys.LIGHT),
//				OverlayTexture.NO_OVERLAY,
//				0
//		);
//
//		matrices.popPose();
//	}
//
//	@Override
//	public <S extends LivingEntityRenderState> void align(AccessoryRenderState accessoryState, S entityState, EntityModel<S> modelId, PoseStack matrices) {
//		if (!(modelId instanceof HumanoidModel<?> humanoid)) return;
//		AccessoryRenderer.transformToFace(matrices, humanoid.body, Side.BACK);
//
//		// Fix modelId placement
//		matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
//		matrices.scale(2.15F, 2.15F, 2.15F);
//		matrices.translate(0.0F, 0.3F, 0.0F);
//
//		matrices.translate(-0.5F, -0.5F, -0.5F);
//	}
}
