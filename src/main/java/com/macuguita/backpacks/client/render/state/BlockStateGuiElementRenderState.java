package com.macuguita.backpacks.client.render.state;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.util.Identifier;

public record BlockStateGuiElementRenderState(
		Matrix3x2f pose,
		Identifier modelId,
		int x1,
		int y1,
		ScreenRect bounds
) implements SpecialGuiElementRenderState {

	public BlockStateGuiElementRenderState(Matrix3x2f pose, Identifier modelId, int x, int y) {
		this(pose, modelId, x, y, new ScreenRect(x, y, 27, 27).transformEachVertex(pose));
	}

	@Override
	public int x2() {
		return x1 + 27;
	}

	@Override
	public int y2() {
		return y1 + 27;
	}

	@Override
	public float scale() {
		return 20.0f;
	}

	@Override
	public @Nullable ScreenRect scissorArea() {
		return null;
	}
}
