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

import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.utils.EquipmentUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BackpackFeatureRenderer<T extends PlayerEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M> {

	public BackpackFeatureRenderer(FeatureRendererContext<T, M> context) {
		super(context);
	}

	// README: Item syncing is UNREALIABLE in CREATIVE mode do NOT try to fix it
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

		ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
		if (chestStack.getItem() == Items.ELYTRA)
			return;

		ItemStack backpack = EquipmentUtils.getEquippedBackpack(player);
		if (backpack.isEmpty()) return;

		if (!backpack.contains(GBComponents.VISIBLE.get()) || !backpack.contains(GBComponents.BACKPACK_MODEL_ID.get()))
			return;

		if (Boolean.FALSE.equals(backpack.get(GBComponents.VISIBLE.get())))
			return;

		BakedModel model = getModel(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));
		matrices.push();

		// Transforms the pose to player's body
		this.getContextModel().body.rotate(matrices);

		// Apply transforms to fix rotation and inverted model
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
		matrices.scale(1.1F, -1.1F, -1.1F);
		int offset = !chestStack.isEmpty() ? 3 : 2;
		matrices.translate(0, -0.06, offset * 0.0625);

		BakedModelRenderer.drawBakedModel(model, matrices, vertexConsumers, light, 0xF000F0);

		matrices.pop();
	}

	private BakedModel getModel(Identifier id) {
		return MinecraftClient.getInstance().getItemRenderer().getModels().getModelManager().getModel(id);
	}
}
