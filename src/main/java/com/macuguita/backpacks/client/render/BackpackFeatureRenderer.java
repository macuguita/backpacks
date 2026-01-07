/*
 * Copyright (c) 2025 macuguita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.macuguita.backpacks.client.render;

import java.util.Optional;

import com.macuguita.backpacks.client.model.GBModelReloadListener;
import com.macuguita.backpacks.client.render.state.BackpackRenderState;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BackpackFeatureRenderer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

	public BackpackFeatureRenderer(RenderLayerParent<S, M> context) {
		super(context);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S renderState, float yRot, float xRot) {
		if (EquipmentUtils.isAccessoriesLoaded())
			return;

		@Nullable BackpackRenderState backpackRenderState = renderState.getData(BackpackRenderState.KEY);

		if (backpackRenderState == null)
			return;

		ItemStack chestStack = backpackRenderState.chest;
		if (chestStack.getItem() == Items.ELYTRA)
			return;

		ItemStack backpack = backpackRenderState.backpack;
		if (backpack.isEmpty()) return;

		if (!backpack.has(GBComponents.VISIBLE.get()) || !backpack.has(GBComponents.BACKPACK_MODEL_ID.get()))
			return;

		if (Boolean.FALSE.equals(backpack.get(GBComponents.VISIBLE.get())))
			return;

		Optional<BlockStateModel> maybeModel = GBModelReloadListener.INSTANCE.getModel(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));

		if (maybeModel.isEmpty())
			return;

		poseStack.pushPose();

		var playerModel = this.getParentModel();
		// Align with body
		playerModel.body.translateAndRotate(poseStack);

		// Fix modelId placement
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.scale(1.1F, -1.1F, -1.1F);
		poseStack.translate(0, -0.06, 0.125);

		poseStack.translate(-0.5F, -0.5F, -0.5F);

		// FIXME 1.21.9
		// Fabric had this in their example leaving this to remind me later of when it is fixed
		// https://github.com/FabricMC/fabric/blob/0.134.1%2B1.21.10/fabric-model-loading-api-v1/src/testmodClient/java/net/fabricmc/fabric/test/model/loading/BakedModelFeatureRenderer.java
		// FabricBlockModelRenderer.render(matrices.peek(), RenderLayerHelper.entityDelegate(vertexConsumers), modelId, 1, 1, 1, light, OverlayTexture.DEFAULT_UV, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());

		nodeCollector.order(0).submitBlockModel(
				poseStack,
				Sheets.cutoutBlockSheet(),
				maybeModel.get(),
				1, 1, 1,
				packedLight,
				OverlayTexture.NO_OVERLAY,
				0
		);

		poseStack.popPose();
	}
}
