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

package com.macuguita.backpacks.common;

import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.client.gui.BackpackScreenHandler;
import com.macuguita.backpacks.client.gui.EquipmentScreenHandler;
import com.macuguita.backpacks.client.gui.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.client.payload.BackpackListSyncPayload;
import com.macuguita.backpacks.common.payload.BackpackCosmeticSyncPayload;
import com.macuguita.backpacks.common.payload.OpenBackpackPayload;
import com.macuguita.backpacks.common.payload.OpenEquipmentPayload;
import com.macuguita.backpacks.common.reg.GBBlockEntities;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.reg.GBItemGroups;
import com.macuguita.backpacks.common.reg.GBObjects;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;
import com.macuguita.backpacks.common.utils.BackpackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

public class GuitaBackpacks implements ModInitializer {
	public static final String MOD_ID = "gbackpacks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier DEFAULT_BACKPACK_MODEL_ID = GuitaBackpacks.id("backpacks/backpack");

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	public static final ExtendedScreenHandlerType<BackpackScreenHandler, BackpackInventoryPayload> BACKPACK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(BackpackScreenHandler::new, BackpackInventoryPayload.CODEC);
	public static final ScreenHandlerType<EquipmentScreenHandler> EQUIPMENT_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, id("equipment"), new ScreenHandlerType<>(EquipmentScreenHandler::new, FeatureSet.empty()));

	@Override
	public void onInitialize() {
		GBConfig.load();
		initRegistries();
		initPayloads();
		initEvents();
		ResourceLoader.get(ResourceType.SERVER_DATA)
				.registerReloader(BackpacksResourceReloadListener.ID, new BackpacksResourceReloadListener());
	}

	private void initEvents() {
		ServerEntityEvents.ENTITY_LOAD.register(new BackpackUtils.DeduplicateBackpacks());

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			BackpackListSyncPayload.send(handler.player, BackpacksResourceReloadListener.BACKPACKS);
		});

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
			if (success) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
					BackpackListSyncPayload.send(player, BackpacksResourceReloadListener.BACKPACKS);
			}
		});
	}

	private void initRegistries() {
		Registry.register(Registries.SCREEN_HANDLER, id("backpack"), BACKPACK_SCREEN_HANDLER);
		GBComponents.init();
		GBObjects.init();
		GBBlockEntities.init();
		GBItemGroups.init();
	}

	private void initPayloads() {
		// Client
		PayloadTypeRegistry.playS2C().register(BackpackInventoryPayload.ID, BackpackInventoryPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(BackpackListSyncPayload.ID, BackpackListSyncPayload.CODEC);
		// Server
		PayloadTypeRegistry.playC2S().register(OpenBackpackPayload.ID, OpenBackpackPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OpenEquipmentPayload.ID, OpenEquipmentPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(BackpackCosmeticSyncPayload.ID, BackpackCosmeticSyncPayload.CODEC);
		// Receivers
		ServerPlayNetworking.registerGlobalReceiver(OpenBackpackPayload.ID, new OpenBackpackPayload.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(OpenEquipmentPayload.ID, new OpenEquipmentPayload.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(BackpackCosmeticSyncPayload.ID, new BackpackCosmeticSyncPayload.Receiver());
	}
}
