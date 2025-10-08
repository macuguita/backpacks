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
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;

@Environment(EnvType.CLIENT)
public class GBModelLoadingPlugin implements ModelLoadingPlugin {

	private static final Map<Identifier, ExtraModelKey<BlockStateModel>> blockStateModels = new HashMap<>();

	@Override
	public void initialize(Context context) {
		ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		var resources = resourceManager.findResources("models/backpacks", path -> path.getPath().endsWith(".json"));

		resources.forEach((key, resource) -> {
			String relPath = key.getPath()
					.substring("models/".length(), key.getPath().length() - ".json".length());
			Identifier id = Identifier.of(key.getNamespace(), relPath);

			ExtraModelKey<BlockStateModel> modelKey = ExtraModelKey.create(id::toString);

			context.addModel(modelKey, SimpleUnbakedExtraModel.blockStateModel(id));
			blockStateModels.put(id, modelKey);
		});
	}

	public static ExtraModelKey<BlockStateModel> getModelKey(Identifier id) {
		return blockStateModels.get(id);
	}

	public static BlockStateModel getBlockstateModel(Identifier id) {
		return MinecraftClient.getInstance().getBakedModelManager().getModel(getModelKey(id));
	}
}
