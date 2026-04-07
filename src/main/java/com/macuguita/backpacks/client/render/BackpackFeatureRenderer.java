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
import com.macuguita.backpacks.pond.AvatarRenderStateDuck;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.Avatar;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

@Environment(EnvType.CLIENT)
public class BackpackFeatureRenderer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
	private static final Matrix4fc IDENTITY_MATRIX4FC = new Matrix4f();
	public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

	public BackpackFeatureRenderer(RenderLayerParent<S, M> context) {
		super(context);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S state, float yRot, float xRot) {
		if (EquipmentUtils.isAccessoriesLoaded())
			return;

		ItemStack chestStack = state.chestEquipment;
		if (chestStack.getItem() == Items.ELYTRA)
			return;

		if (!(state instanceof AvatarRenderStateDuck duck))
			return;

		ItemStack backpack = duck.gbackpacks$backpack();
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

		// Source for this: https://github.com/FabricMC/fabric-api/blob/26.1.1/fabric-model-loading-api-v1/src/testmodClient/java/net/fabricmc/fabric/test/model/loading/BakedModelRenderLayer.java
		// if it wasn't for FAPI I probably wouldn't know how to do this

		BlockStateModel model = maybeModel.get();
		BlockModelRenderState renderState = new BlockModelRenderState();
		QuadEmitter emitter = renderState.setupMesh(IDENTITY_MATRIX4FC, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
		model.emitQuads(
				emitter,
				BlockAndTintGetter.EMPTY,
				BlockPos.ZERO,
				Blocks.AIR.defaultBlockState(),
				renderState.scratchRandomSource(42L),
				_ -> false
		);
		renderState.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);

		poseStack.popPose();
	}
}
