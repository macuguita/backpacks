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

import com.macuguita.backpacks.client.model.GBModelReloadListener;
import com.macuguita.backpacks.client.render.state.BackpackRenderState;
import com.macuguita.backpacks.common.reg.GBComponents;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BackpackFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends FeatureRenderer<S, M> {

	public BackpackFeatureRenderer(FeatureRendererContext<S, M> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {

		@Nullable BackpackRenderState backpackRenderState = state.getData(BackpackRenderState.KEY);

		if (backpackRenderState == null)
			return;

		ItemStack chestStack = backpackRenderState.chest;
		if (chestStack.getItem() == Items.ELYTRA)
			return;

		ItemStack backpack = backpackRenderState.backpack;
		if (backpack.isEmpty()) return;

		if (!backpack.contains(GBComponents.VISIBLE.get()) || !backpack.contains(GBComponents.BACKPACK_MODEL_ID.get()))
			return;

		if (Boolean.FALSE.equals(backpack.get(GBComponents.VISIBLE.get())))
			return;

		BlockStateModel model = GBModelReloadListener.INSTANCE.getModel(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));

		if (model == null)
			return;

		matrices.push();

		var playerModel = this.getContextModel();
		// Align with body
		playerModel.body.applyTransform(matrices);

		// Fix model placement
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
		matrices.scale(1.1F, -1.1F, -1.1F);
		matrices.translate(0, -0.06, 0.125);

		matrices.translate(-0.5F, -0.5F, -0.5F);

		// FIXME 1.21.9
		// Fabric had this in their example leaving this to remind me later of when it is fixed
		// https://github.com/FabricMC/fabric/blob/0.134.1%2B1.21.10/fabric-model-loading-api-v1/src/testmodClient/java/net/fabricmc/fabric/test/model/loading/BakedModelFeatureRenderer.java
		// FabricBlockModelRenderer.render(matrices.peek(), RenderLayerHelper.entityDelegate(vertexConsumers), model, 1, 1, 1, light, OverlayTexture.DEFAULT_UV, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());

		queue.getBatchingQueue(0).submitBlockStateModel(
				matrices,
				TexturedRenderLayers.getEntityCutout(),
				model,
				1, 1, 1,
				light,
				OverlayTexture.DEFAULT_UV,
				0
		);

		matrices.pop();
	}
}
