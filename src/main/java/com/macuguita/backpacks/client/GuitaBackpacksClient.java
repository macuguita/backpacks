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

package com.macuguita.backpacks.client;

import java.util.ArrayList;
import java.util.List;

import com.macuguita.backpacks.GuitaBackpacks;
import com.macuguita.backpacks.client.gui.BackpackScreen;
import com.macuguita.backpacks.client.gui.EquipmentScreen;
import com.macuguita.backpacks.client.render.BackpackBlockEntityRenderer;
import com.macuguita.backpacks.client.render.BackpackFeatureRenderer;
import com.macuguita.backpacks.client.render.GuitaBackpacksModelLoadingPlugin;
import com.macuguita.backpacks.utils.EquipmentUtils;
import com.macuguita.backpacks.network.BackpacksResourceReloadListener;
import com.macuguita.backpacks.network.payload.BackpackListSyncPayload;
import com.macuguita.backpacks.reg.GBBlockEntities;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.PlayerEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;

public class GuitaBackpacksClient implements ClientModInitializer {

	public static final List<BackpacksResourceReloadListener.Backpack> BACKPACKS = new ArrayList<>();

	@Override
	public void onInitializeClient() {

		GBKeybinds.init();

		BlockEntityRendererFactories.register(GBBlockEntities.BACKPACK.get(), BackpackBlockEntityRenderer::new);

		ModelLoadingPlugin.register(new GuitaBackpacksModelLoadingPlugin());
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerEntityRenderer playerEntityRenderer) {
				registrationHelper.register(new BackpackFeatureRenderer<>(playerEntityRenderer));
			}
		});

		HandledScreens.register(GuitaBackpacks.BACKPACK_SCREEN_HANDLER, BackpackScreen::new);
		if (!EquipmentUtils.isTrinketsLoaded())
			HandledScreens.register(GuitaBackpacks.EQUIPMENT_SCREEN_HANDLER, EquipmentScreen::new);


		ClientPlayNetworking.registerGlobalReceiver(BackpackListSyncPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				BACKPACKS.clear();
				BACKPACKS.addAll(payload.list());
			});
		});
	}
}
