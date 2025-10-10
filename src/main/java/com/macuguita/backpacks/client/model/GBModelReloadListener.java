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

package com.macuguita.backpacks.client.model;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

public class GBModelReloadListener implements SynchronousResourceReloader {
	public static final GBModelReloadListener INSTANCE = new GBModelReloadListener();
	public static final Identifier ID = Identifier.of("backpacks", "model_reload_listener");

	private final Map<Identifier, BlockStateModel> loadedModels = new HashMap<>();

	@Override
	public void reload(ResourceManager manager) {
		loadedModels.clear();

		var client = MinecraftClient.getInstance();

		for (var entry : GBModelLoadingPlugin.getBlockStateModels().entrySet()) {
			Identifier id = entry.getKey();
			var key = entry.getValue();
			BlockStateModel model = client.getBakedModelManager().getModel(key);
			if (model != null) {
				loadedModels.put(id, model);
			}
		}
	}

	public BlockStateModel getModel(Identifier id) {
		return loadedModels.get(id);
	}
}
