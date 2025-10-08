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
