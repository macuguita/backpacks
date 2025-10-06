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

import com.macuguita.backpacks.block.BackpackBlock;
import com.macuguita.backpacks.block.entity.BackpackBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

public class BackpackBlockEntityRenderState extends BlockEntityRenderState {

	public static final RenderStateDataKey<BackpackBlockEntityRenderState> KEY = RenderStateDataKey.create(() -> "backpack_block");

	public Identifier modelId = null;
	public Direction direction = Direction.NORTH;

	public static <E extends BlockEntity, S extends BlockEntityRenderState> void updateRenderState(E blockEntity, S state) {
		BackpackBlockEntityRenderState backpackRenderState = new BackpackBlockEntityRenderState();
		if (!(blockEntity instanceof BackpackBlockEntity backpackBlock))
			return;
		backpackRenderState.direction = blockEntity.getCachedState().get(BackpackBlock.FACING);
		backpackRenderState.modelId = backpackBlock.getBlockModelId();
		state.setData(KEY, backpackRenderState);
	}
}
