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

package com.macuguita.backpacks.network;

import com.macuguita.backpacks.GuitaBackpacks;
import com.macuguita.backpacks.client.gui.EquipmentScreenHandler;
import com.macuguita.backpacks.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.item.BackpackItem;
import com.macuguita.backpacks.network.payload.BackpackCosmeticSyncPayload;
import com.macuguita.backpacks.network.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.network.payload.BackpackListSyncPayload;
import com.macuguita.backpacks.network.payload.OpenBackpackPayload;
import com.macuguita.backpacks.network.payload.OpenEquipmentPayload;
import com.macuguita.backpacks.reg.GBComponents;
import com.macuguita.backpacks.utils.BackpackUtils;
import com.macuguita.backpacks.utils.EquipmentUtils;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

public class GBNetworking {

	public static final Identifier BACKPACK_INVENTORY_SIZE_PACKET = GuitaBackpacks.id("backpack_inventory_size");
	public static final Identifier OPEN_BACKPACK_PACKET = GuitaBackpacks.id("open_backpack");
	public static final Identifier OPEN_EQUIPMENT_PACKET = GuitaBackpacks.id("open_equipment");
	public static final Identifier BACKPACK_LIST_SYNC = GuitaBackpacks.id("backpack_list_sync");
	public static final Identifier BACKPACK_COSMETIC_SYNC = GuitaBackpacks.id("backpack_cosmetic_sync");

	public static void init() {
		PayloadTypeRegistry.playS2C().register(BackpackInventoryPayload.ID, BackpackInventoryPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(BackpackListSyncPayload.ID, BackpackListSyncPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OpenBackpackPayload.ID, OpenBackpackPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OpenEquipmentPayload.ID, OpenEquipmentPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(BackpackCosmeticSyncPayload.ID, BackpackCosmeticSyncPayload.CODEC);
		ResourceLoader.get(ResourceType.SERVER_DATA)
						.registerReloader(BackpacksResourceReloadListener.ID, new BackpacksResourceReloadListener());
	}

	public static void initEvents() {
		// Backpack deduplication
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(entity instanceof ItemEntity itemEntity)) return;

			ItemStack stack = itemEntity.getStack();
			if (!stack.contains(GBComponents.BACKPACK_UUID.get())) return;

			BackpackUtils.dedupeBackpackItemEntity(itemEntity);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			syncBackpacks(handler.player);
		});

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
			if (success) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
					syncBackpacks(player);
			}
		});
	}

	private static void syncBackpacks(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, new BackpackListSyncPayload(BackpacksResourceReloadListener.BACKPACKS));
	}

	public static void initGlobalReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(OpenBackpackPayload.ID, (payload, context) -> {
			if (payload.buf()) {
				context.server().execute(() -> {
					ServerPlayerEntity player = context.player();
					int backpackSlot = EquipmentUtils.getBackpackSlotIndex(player);
					if (backpackSlot != -1) {
						BackpackItem.openOrCreateBackpackIfNotExists(player, backpackSlot);
					}
				});
			}
		});

		if (!EquipmentUtils.isTrinketsLoaded()) {
			ServerPlayNetworking.registerGlobalReceiver(OpenEquipmentPayload.ID, (payload, context) -> {
				if (payload.buf()) {
					context.server().execute(() -> {
						ServerPlayerEntity player = context.player();
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
				}
			});
		}

		ServerPlayNetworking.registerGlobalReceiver(BackpackCosmeticSyncPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();

				int backpackSlot = EquipmentUtils.getBackpackSlotIndex(player);
				if (backpackSlot == -1) {
					return;
				}

				ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, backpackSlot);
				if (backpack.isEmpty() || !backpack.contains(GBComponents.BACKPACK_MODEL_ID.get())) {
					return;
				}

				backpack.set(GBComponents.BACKPACK_MODEL_ID.get(), payload.newId());

				if (!EquipmentUtils.isTrinketsLoaded() && backpackSlot >= 20000) {
					GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(player).getInventory().markDirty();
				}
			});
		});
	}
}
