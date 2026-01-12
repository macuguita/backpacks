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
import com.macuguita.backpacks.common.attachments.EquipmentAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;
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

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.minecraft.world.item.ItemStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;

public class GuitaBackpacks implements ModInitializer {
	public static final String MOD_ID = "gbackpacks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier DEFAULT_BACKPACK_MODEL_ID = GuitaBackpacks.id("backpacks/backpack");

	public static Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}

	public static final ExtendedMenuType<BackpackScreenHandler, BackpackInventoryPayload> BACKPACK_SCREEN_HANDLER = new ExtendedMenuType<>(BackpackScreenHandler::new, BackpackInventoryPayload.CODEC);
	public static final MenuType<EquipmentScreenHandler> EQUIPMENT_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, id("equipment"), new MenuType<>(EquipmentScreenHandler::new, FeatureFlagSet.of()));

	@Override
	public void onInitialize() {
		GBConfig.load();
		initRegistries();
		initPayloads();
		initEvents();
		GBAttachmentTypes.init();
		ResourceLoader.get(PackType.SERVER_DATA)
				.registerReloadListener(BackpacksResourceReloadListener.ID, new BackpacksResourceReloadListener());
	}

	private void initEvents() {
		ServerEntityEvents.ENTITY_LOAD.register(new BackpackUtils.DeduplicateBackpacks());

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				BackpackListSyncPayload.send(handler.player, BackpacksResourceReloadListener.BACKPACKS));

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
			if (success) {
				for (ServerPlayer player : server.getPlayerList().getPlayers())
					BackpackListSyncPayload.send(player, BackpacksResourceReloadListener.BACKPACKS);
			}
		});
	}

	private void initRegistries() {
		Registry.register(BuiltInRegistries.MENU, id("backpack"), BACKPACK_SCREEN_HANDLER);
		GBComponents.init();
		GBObjects.init();
		GBBlockEntities.init();
		GBItemGroups.init();
	}

	private void initPayloads() {
		// Client
		NetworkManager.registerS2C(BackpackInventoryPayload.ID, BackpackInventoryPayload.CODEC);
		NetworkManager.registerS2C(BackpackListSyncPayload.ID, BackpackListSyncPayload.CODEC, pkt -> {
			GuitaBackpacksClient.BACKPACKS.clear();
			GuitaBackpacksClient.BACKPACKS.addAll(pkt.list());
		});
		// Server
		NetworkManager.registerC2S(OpenBackpackPayload.ID, OpenBackpackPayload.CODEC, (pkt, player) -> {
			int backpackSlot = EquipmentUtils.getBackpackSlotIndex(player);
			if (backpackSlot != -1) {
				BackpackItem.openOrCreateBackpackIfNotExists(player, backpackSlot);
			}
		});
		NetworkManager.registerC2S(OpenEquipmentPayload.ID, OpenEquipmentPayload.CODEC, (pkt, player) -> {
			if (EquipmentUtils.isAccessoriesLoaded()) return;

			var factory = new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return Component.empty();
				}

				@Override
				public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
					// Get a working copy for the menu
					SimpleContainer workingCopy = player.getAttachedOrCreate(
							GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE,
							() -> EquipmentAttachedData.DEFAULT
					).getInventory();

					return new EquipmentScreenHandler(syncId, playerInventory, workingCopy);
				}
			};

			player.openMenu(factory);
		});
		NetworkManager.registerC2S(BackpackCosmeticSyncPayload.ID, BackpackCosmeticSyncPayload.CODEC, (pkt, player) -> {
			if (pkt.slotIndex() == -1) {
				return;
			}

			ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, pkt.slotIndex());
			if (backpack.isEmpty() || !backpack.has(GBComponents.BACKPACK_MODEL_ID.get())) {
				return;
			}

			backpack.set(GBComponents.BACKPACK_MODEL_ID.get(), pkt.newId());

			if (!EquipmentUtils.isAccessoriesLoaded() && pkt.slotIndex() >= 20000) {
				EquipmentAttachedData currentData = player.getAttachedOrCreate(
						GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE,
						() -> EquipmentAttachedData.DEFAULT
				);

				EquipmentAttachedData updatedData = currentData.setBackpack(backpack);
				player.setAttached(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, updatedData);
			}
		});
	}
}
