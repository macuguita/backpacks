package com.macuguita.backpacks.client.render;

import java.util.ArrayDeque;
import java.util.Deque;

import com.macuguita.backpacks.client.model.GBModelLoadingPlugin;
import com.macuguita.backpacks.client.render.state.BlockStateGuiElementRenderState;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

// sources: https://github.com/ZurrTum/Create-Fly/blob/v6.0.7-15/src/client/java/com/zurrtum/create/client/foundation/gui/render/ManualBlockRenderer.java
public class BlockStateGuiRenderer extends SpecialGuiElementRenderer<BlockStateGuiElementRenderState> {
	public static int MAX = 6;
	private int allocate = MAX;
	private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
	private final MatrixStack matrices = new MatrixStack();
	private int windowScaleFactor;

	public BlockStateGuiRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
		super(vertexConsumers);
	}

	@Override
	public Class<BlockStateGuiElementRenderState> getElementClass() {
		return BlockStateGuiElementRenderState.class;
	}

	@Override
	public void render(BlockStateGuiElementRenderState element, GuiRenderState guiState, int windowScaleFactor) {
		MinecraftClient mc = MinecraftClient.getInstance();

		// Manage framebuffer reuse (same logic as Create)
		if (this.windowScaleFactor != windowScaleFactor) {
			this.windowScaleFactor = windowScaleFactor;
			TEXTURES.forEach(GpuTexture::close);
			TEXTURES.clear();
			allocate = MAX;
		}

		int size = 27 * windowScaleFactor;
		GpuTexture texture;
		if (allocate > 0) {
			allocate--;
			texture = GpuTexture.create(size);
		} else {
			texture = TEXTURES.poll();
			assert texture != null;
		}

		RenderSystem.setProjectionMatrix(projectionMatrix.set(size, size), ProjectionType.ORTHOGRAPHIC);
		texture.prepare();

		matrices.push();
		matrices.translate(size / 2.0F, size, 0.0F);
		float scale = 20 * windowScaleFactor;
		matrices.scale(scale, scale, scale);

		mc.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);

		// Fancy camera transform for the model
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
		matrices.translate(-0.5f, -0.5f, -0.5f);
		matrices.scale(1, -1, 1);

		var model = GBModelLoadingPlugin.getBlockstateModel(element.modelId());
		if (model != null) {
			VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
			int light = 0xF000F0;
			int overlay = OverlayTexture.DEFAULT_UV;

			for (var part : model.getParts(mc.world != null ? mc.world.random : Random.create())) {
				for (var dir : Direction.values()) {
					for (var quad : part.getQuads(dir)) {
						buffer.quad(matrices.peek(), quad, 1f, 1f, 1f, 1f, light, overlay);
					}
				}
				for (var quad : part.getQuads(null)) {
					buffer.quad(matrices.peek(), quad, 1f, 1f, 1f, 1f, light, overlay);
				}
			}

			vertexConsumers.draw();
		}

		matrices.pop();
		texture.clear();

		guiState.addSimpleElementToCurrentLayer(new TexturedQuadGuiElementRenderState(
				RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
				TextureSetup.withoutGlTexture(texture.textureView()),
				element.pose(),
				element.x1(),
				element.y1(),
				element.x2(),
				element.y2(),
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
	protected void render(BlockStateGuiElementRenderState state, MatrixStack matrices) {
	}

	@Override
	protected String getName() {
		return "blockstate gui renderer";
	}
}
