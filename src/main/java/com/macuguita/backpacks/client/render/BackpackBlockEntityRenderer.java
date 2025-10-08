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

import com.macuguita.backpacks.common.block.BackpackBlock;
import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings("ClassCanBeRecord")
@Environment(EnvType.CLIENT)
public class BackpackBlockEntityRenderer implements BlockEntityRenderer<BackpackBlockEntity> {

	private final BlockEntityRendererFactory.Context context;

	public BackpackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this.context = context;
	}

	@Override
	public void render(BackpackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		BakedModel model = context.getItemRenderer().getModels().getModelManager().getModel(entity.getBlockModelId());
		Direction direction = entity.getCachedState().get(BackpackBlock.FACING);

		if (model == null) return;

		matrices.push();

		// Move to block center
		matrices.translate(0.5, 0.5, 0.5);

		// Rotate according to block direction
		float rotation = switch (direction) {
			case NORTH -> 0f;
			case SOUTH -> 180f;
			case WEST  -> 90f;
			case EAST  -> -90f;
			default    -> 0f;
		};
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

		BakedModelRenderer.drawBakedModel(model, matrices, vertexConsumers, light, overlay);

		matrices.pop();
	}

}
