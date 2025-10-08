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

import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.macuguita.backpacks.client.model.GBModelLoadingPlugin;
import com.macuguita.backpacks.client.render.state.BackpackBlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.LecternBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings("ClassCanBeRecord")
@Environment(EnvType.CLIENT)
public class BackpackBlockEntityRenderer implements BlockEntityRenderer<BackpackBlockEntity, BackpackBlockEntityRenderState> {

	private final BlockEntityRendererFactory.Context context;

	public BackpackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this.context = context;
	}

	@Override
	public void updateRenderState(BackpackBlockEntity blockEntity, BackpackBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
		BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.modelId = blockEntity.getBlockModelId();
		state.direction = blockEntity.getCachedState().get(LecternBlock.FACING);
	}

	@Override
	public BackpackBlockEntityRenderState createRenderState() {
		return new BackpackBlockEntityRenderState();
	}

	@Override
	public void render(BackpackBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
		BlockStateModel model = GBModelLoadingPlugin.getBlockstateModel(state.modelId);
		Direction direction = state.direction;

		if (model == null)
			return;

		matrices.push();

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

		matrices.translate(-0.5, -0.5, -0.5);

		queue.getBatchingQueue(0).submitBlockStateModel(
				matrices,
				TexturedRenderLayers.getEntityCutout(),
				model,
				1, 1, 1,
				state.lightmapCoordinates,
				OverlayTexture.DEFAULT_UV,
				0
		);
		matrices.pop();
	}
}
