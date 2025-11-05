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
import java.util.UUID;

import com.macuguita.backpacks.client.gui.BackpackScreen;
import com.macuguita.backpacks.client.gui.EquipmentScreen;
import com.macuguita.backpacks.client.model.GBModelLoadingPlugin;
import com.macuguita.backpacks.client.model.GBModelReloadListener;
import com.macuguita.backpacks.client.payload.BackpackListSyncPayload;
import com.macuguita.backpacks.client.render.BackpackBlockEntityRenderer;
import com.macuguita.backpacks.client.render.BackpackFeatureRenderer;
import com.macuguita.backpacks.client.render.BlockStateGuiRenderer;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.item.BackpackItem;
import com.macuguita.backpacks.common.reg.GBBlockEntities;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;

public class GuitaBackpacksClient implements ClientModInitializer {

	public static final List<BackpacksResourceReloadListener.Backpack> BACKPACKS = new ArrayList<>();

	@Override
	public void onInitializeClient() {

		GBKeybinds.init();

		// Sources: https://github.com/FabricMC/fabric/tree/0.134.1%2B1.21.10/fabric-model-loading-api-v1/src/testmodClient/java/net/fabricmc/fabric/test/model/loading
		ModelLoadingPlugin.register(new GBModelLoadingPlugin());

		ClientPlayNetworking.registerGlobalReceiver(BackpackListSyncPayload.ID, new BackpackListSyncPayload.Receiver());

		// Might have to do something with this, look at the link above
		ResourceLoader resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.registerReloader(GBModelReloadListener.ID, GBModelReloadListener.INSTANCE);
		resourceLoader.addReloaderOrdering(ResourceReloaderKeys.Client.MODELS, GBModelReloadListener.ID);

		BlockEntityRenderers.register(GBBlockEntities.BACKPACK, BackpackBlockEntityRenderer::new);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> playerRenderer) {
				registrationHelper.register(new BackpackFeatureRenderer<>(playerRenderer));
			}
		});

		SpecialGuiElementRegistry.register(ctx -> new BlockStateGuiRenderer(ctx.vertexConsumers()));
		MenuScreens.register(GuitaBackpacks.BACKPACK_SCREEN_HANDLER, BackpackScreen::new);
		if (!EquipmentUtils.isTrinketsLoaded())
			MenuScreens.register(GuitaBackpacks.EQUIPMENT_SCREEN_HANDLER, EquipmentScreen::new);

		ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
			if (!(itemStack.getItem() instanceof BackpackItem)) return;

			if (itemStack.has(GBComponents.VISIBLE.get())) {
				Boolean visible = itemStack.get(GBComponents.VISIBLE.get());
				if (Boolean.FALSE.equals(visible)) {
					list.add(Component.translatable("item.gbackpacks.backpack.tooltip.hidden")
							.withStyle(ChatFormatting.DARK_GRAY));
				}
			}

			if (itemStack.has(GBComponents.BACKPACK_MODEL_ID.get())) {
				var modelId = itemStack.get(GBComponents.BACKPACK_MODEL_ID.get());
				if (modelId != null) {
					GuitaBackpacksClient.BACKPACKS.stream()
							.filter(backpack -> backpack.id().equals(modelId))
							.findFirst()
							.ifPresent(backpack -> list.add(
									Component.translatable("item.gbackpacks.backpack.tooltip.cosmetic")
											.append(Component.translatable(backpack.translationKey()))
											.withStyle(ChatFormatting.DARK_GRAY)
							));
				}
			}

			if (itemStack.has(GBComponents.BACKPACK_UUID.get())) {
				if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT)) {
					list.add(Component.translatable("item.gbackpacks.backpack.tooltip.uuid.hidden")
							.withStyle(ChatFormatting.DARK_GRAY));
				} else {
					UUID uuid = itemStack.get(GBComponents.BACKPACK_UUID.get());
					if (uuid != null) {
						list.add(Component.translatable("item.gbackpacks.backpack.tooltip.uuid", uuid)
								.withStyle(ChatFormatting.GOLD));
					}
				}
			}
		});
	}
}
