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
import com.macuguita.backpacks.client.GuitaBackpacksClient;
import com.macuguita.backpacks.client.gui.BackpackScreenHandler;
import com.macuguita.backpacks.client.gui.EquipmentScreenHandler;
import com.macuguita.backpacks.client.gui.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.client.payload.BackpackListSyncPayload;
import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.common.item.BackpackItem;
import com.macuguita.backpacks.common.payload.BackpackCosmeticSyncPayload;
import com.macuguita.backpacks.common.payload.OpenBackpackPayload;
import com.macuguita.backpacks.common.payload.OpenEquipmentPayload;
import com.macuguita.backpacks.common.reg.GBBlockEntities;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.reg.GBItemGroups;
import com.macuguita.backpacks.common.reg.GBObjects;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;
import com.macuguita.backpacks.common.utils.BackpackUtils;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import com.macuguita.lib.network.NetworkManager;

import net.minecraft.client.MinecraftClient;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import org.jetbrains.annotations.NotNull;
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
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
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
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new BackpacksResourceReloadListener());
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
		// TODO: macu lib, allow registering payloads with no handler
		PayloadTypeRegistry.playS2C().register(BackpackInventoryPayload.ID, BackpackInventoryPayload.CODEC);
		NetworkManager.registerS2C(BackpackListSyncPayload.ID, BackpackListSyncPayload.CODEC, (payload) -> {
			MinecraftClient.getInstance().execute(() -> {
				GuitaBackpacksClient.BACKPACKS.clear();
				GuitaBackpacksClient.BACKPACKS.addAll(payload.list());
			});
		});
		// Server
		NetworkManager.registerC2S(OpenBackpackPayload.ID, OpenBackpackPayload.CODEC, (payload, player) -> {
			int backpackSlot = EquipmentUtils.getBackpackSlotIndex(player);
			if (backpackSlot != -1) {
				BackpackItem.openOrCreateBackpackIfNotExists(player, backpackSlot);
			}
		});
		NetworkManager.registerC2S(OpenEquipmentPayload.ID, OpenEquipmentPayload.CODEC, (payload, player) -> {
			if (EquipmentUtils.isAccessoriesLoaded()) return;
			var factory = new NamedScreenHandlerFactory() {

				@Override
				public Text getDisplayName() {
					return Text.empty();
				}

				@Override
				public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
					return new EquipmentScreenHandler(syncId, playerInventory, GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(player).getInventory());
				}
			};

			player.openHandledScreen(factory);
		});
		NetworkManager.registerC2S(BackpackCosmeticSyncPayload.ID, BackpackCosmeticSyncPayload.CODEC, (payload, player) -> {
			int backpackSlot = EquipmentUtils.getBackpackSlotIndex(player);
			if (backpackSlot == -1) {
				return;
			}

			ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, backpackSlot);
			if (backpack.isEmpty() || !backpack.contains(GBComponents.BACKPACK_MODEL_ID.get())) {
				return;
			}

			backpack.set(GBComponents.BACKPACK_MODEL_ID.get(), payload.newId());

			if (!EquipmentUtils.isAccessoriesLoaded() && backpackSlot >= 20000) {
				GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(player).getInventory().markDirty();
			}
		});
	}

}
