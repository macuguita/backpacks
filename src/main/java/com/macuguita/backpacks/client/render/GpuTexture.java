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

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// source: https://github.com/ZurrTum/Create-Fly/blob/v6.0.7-15/src/client/java/com/zurrtum/create/client/catnip/gui/render/GpuTexture.java
public record GpuTexture(
		int width, int height, com.mojang.blaze3d.textures.GpuTexture texture, GpuTextureView textureView,
		com.mojang.blaze3d.textures.GpuTexture depthTexture, GpuTextureView depthTextureView
) {
	@Contract("_ -> new")
	public static @NotNull GpuTexture create(int size) {
		return create(size, size);
	}

	@Contract("_, _ -> new")
	public static @NotNull GpuTexture create(int width, int height) {
		GpuDevice gpuDevice = RenderSystem.getDevice();
		com.mojang.blaze3d.textures.GpuTexture texture = gpuDevice.createTexture(
				() -> "UI Item Transform texture",
				12,
				TextureFormat.RGBA8,
				width,
				height,
				1,
				1
		);
		texture.setTextureFilter(FilterMode.NEAREST, false);
		GpuTextureView textureView = gpuDevice.createTextureView(texture);
		com.mojang.blaze3d.textures.GpuTexture depthTexture = gpuDevice.createTexture(
				() -> "UI Item Transform depth texture",
				8,
				TextureFormat.DEPTH32,
				width,
				height,
				1,
				1
		);
		GpuTextureView depthTextureView = gpuDevice.createTextureView(depthTexture);
		return new GpuTexture(width, height, texture, textureView, depthTexture, depthTextureView);
	}

	public void prepare() {
		RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(texture, 0, depthTexture, 1.0F);
		RenderSystem.outputColorTextureOverride = textureView;
		RenderSystem.outputDepthTextureOverride = depthTextureView;
	}

	public void clear() {
		RenderSystem.outputColorTextureOverride = null;
		RenderSystem.outputDepthTextureOverride = null;
	}

	public void close() {
		texture.close();
		textureView.close();
		depthTexture.close();
		depthTextureView.close();
	}
}
