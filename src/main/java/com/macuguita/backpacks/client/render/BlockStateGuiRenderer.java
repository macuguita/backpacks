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

import java.util.ArrayDeque;
import java.util.Deque;

import com.macuguita.backpacks.client.model.GBModelReloadListener;
import com.macuguita.backpacks.client.render.state.BlockStateGuiElementRenderState;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

// sources: https://github.com/ZurrTum/Create-Fly/blob/v6.0.7-15/src/client/java/com/zurrtum/create/client/foundation/gui/render/ManualBlockRenderer.java
public class BlockStateGuiRenderer extends PictureInPictureRenderer<BlockStateGuiElementRenderState> {
	public static final int MAX = 6;
	private int allocate = MAX;
	private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
	private final PoseStack matrices = new PoseStack();
	private int windowScaleFactor;

	public BlockStateGuiRenderer(MultiBufferSource.BufferSource vertexConsumers) {
		super(vertexConsumers);
	}

	@Override
	public Class<BlockStateGuiElementRenderState> getRenderStateClass() {
		return BlockStateGuiElementRenderState.class;
	}

	@Override
	public void prepare(BlockStateGuiElementRenderState renderState, GuiRenderState guiRenderState, int guiScale) {
		Minecraft mc = Minecraft.getInstance();

		// Manage framebuffer reuse (same logic as Create)
		if (this.windowScaleFactor != guiScale) {
			this.windowScaleFactor = guiScale;
			TEXTURES.forEach(GpuTexture::close);
			TEXTURES.clear();
			allocate = MAX;
		}

		int size = 27 * guiScale;
		GpuTexture texture;
		if (allocate > 0) {
			allocate--;
			texture = GpuTexture.create(size);
		} else {
			texture = TEXTURES.poll();
			assert texture != null;
		}

		RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(size, size), ProjectionType.ORTHOGRAPHIC);
		texture.prepare();

		matrices.pushPose();
		matrices.translate(size / 2.0F, size, 0.0F);
		float scale = 20 * guiScale;
		matrices.scale(scale, scale, scale);

		mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

		// Fancy camera transform for the model
		matrices.mulPose(Axis.XP.rotationDegrees(-30));
		matrices.mulPose(Axis.YP.rotationDegrees(45));
		matrices.translate(-0.5f, -0.5f, -0.5f);
		matrices.scale(1, -1, 1);

		var model = GBModelReloadListener.INSTANCE.getModel(renderState.modelId());
		if (model.isPresent()) {
			VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
			int light = 0xF000F0;
			int overlay = OverlayTexture.NO_OVERLAY;

			for (var part : model.get().collectParts(mc.level != null ? mc.level.random : RandomSource.create())) {
				for (var dir : Direction.values()) {
					for (var quad : part.getQuads(dir)) {
						buffer.putBulkData(matrices.last(), quad, 1f, 1f, 1f, 1f, light, overlay);
					}
				}
				for (var quad : part.getQuads(null)) {
					buffer.putBulkData(matrices.last(), quad, 1f, 1f, 1f, 1f, light, overlay);
				}
			}

			bufferSource.endBatch();
		}

		matrices.popPose();
		texture.clear();

		guiRenderState.submitBlitToCurrentLayer(new BlitRenderState(
				RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
				TextureSetup.singleTexture(texture.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
				renderState.pose(),
				renderState.x0(),
				renderState.y0(),
				renderState.x1(),
				renderState.y1(),
				0.0F,
				1.0F,
				1.0F,
				0.0F,
				-1,
				null,
				null
		));

		TEXTURES.add(texture);
	}

	@Override
	protected void renderToTexture(BlockStateGuiElementRenderState renderState, PoseStack poseStack) {}

	@Override
	protected String getTextureLabel() {
		return "blockstate gui renderer";
	}
}
