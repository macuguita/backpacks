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

package com.macuguita.backpacks.client.render.state;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.resources.ResourceLocation;

public record BlockStateGuiElementRenderState(
		Matrix3x2f pose,
		ResourceLocation modelId,
		int x,
		int y,
		ScreenRectangle bounds
) implements PictureInPictureRenderState {

	private static final int SIZE = 27;

	public BlockStateGuiElementRenderState(Matrix3x2f pose, ResourceLocation modelId, int x, int y) {
		this(pose, modelId, x, y, new ScreenRectangle(x, y, SIZE, SIZE).transformMaxBounds(pose));
	}

	@Override
	public int x0() {
		return x;
	}

	@Override
	public int y0() {
		return y;
	}

	@Override
	public int x1() {
		return x + SIZE;
	}

	@Override
	public int y1() {
		return y + SIZE;
	}

	@Override
	public float scale() {
		return 20.0f;
	}

	@Nullable
	@Override
	public ScreenRectangle scissorArea() {
		return null;
	}

	@Override
	public @NotNull Matrix3x2f pose() {
		return pose;
	}
}
