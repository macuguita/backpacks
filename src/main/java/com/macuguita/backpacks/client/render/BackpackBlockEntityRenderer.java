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
import com.macuguita.backpacks.client.render.state.BackpackBlockEntityRenderState;
import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BackpackBlockEntityRenderer implements BlockEntityRenderer<BackpackBlockEntity, BackpackBlockEntityRenderState> {

	public BackpackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void extractRenderState(BackpackBlockEntity blockEntity, BackpackBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.modelId = blockEntity.getBlockModelId();
		renderState.direction = blockEntity.getBlockState().getValue(LecternBlock.FACING);
	}

	@Override
	public BackpackBlockEntityRenderState createRenderState() {
		return new BackpackBlockEntityRenderState();
	}

	@Override
	public void submit(BackpackBlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		Optional<BlockStateModel> maybeModel = GBModelReloadListener.INSTANCE.getModel(renderState.modelId);
		Direction direction = renderState.direction;

		if (maybeModel.isEmpty())
			return;

		poseStack.pushPose();

		poseStack.translate(0.5, 0.5, 0.5);

		// Rotate according to block direction
		float rotation = switch (direction) {
			case NORTH -> 0f;
			case SOUTH -> 180f;
			case WEST -> 90f;
			case EAST -> -90f;
			default -> 0f;
		};

		poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

		poseStack.translate(-0.5, -0.5, -0.5);

		nodeCollector.order(0).submitBlockModel(
				poseStack,
				Sheets.cutoutBlockSheet(),
				maybeModel.get(),
				1, 1, 1,
				renderState.lightCoords,
				OverlayTexture.NO_OVERLAY,
				0
		);
		poseStack.popPose();
	}
}
