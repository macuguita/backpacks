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

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BakedModelRenderer {

	private static final Random RANDOM = Random.create();

	public static void drawBakedModel(BakedModel model, MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, int overlay) {
		matrices.push();
		matrices.translate(-0.5F, -0.5F, -0.5F);

		// Use a translucent block sheet for GUI-like rendering
		VertexConsumer consumer = vertexConsumer.getBuffer(TexturedRenderLayers.getEntityTranslucentCull());

		// Render quads for each face
		for (Direction dir : Direction.values()) {
			pushQuads(matrices, consumer, model.getQuads(null, dir, RANDOM), light, overlay);
		}

		// Render quads that are not face-specific
		pushQuads(matrices, consumer, model.getQuads(null, null, RANDOM), light, overlay);

		matrices.pop();
	}

	private static void pushQuads(MatrixStack matrix, VertexConsumer consumer, List<BakedQuad> quads, int light, int overlay) {
		MatrixStack.Entry last = matrix.peek();
		for (BakedQuad quad : quads) {
			consumer.quad(last, quad, 1f, 1f, 1f, 1f, light, overlay);
		}
	}

	public static BakedModel getModel(Identifier id) {
		return MinecraftClient.getInstance().getItemRenderer().getModels().getModelManager().getModel(id);
	}
}
