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

package com.macuguita.backpacks.common.payload;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.attachments.EquipmentAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.utils.EquipmentUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record BackpackCosmeticSyncPayload(int slotIndex, ResourceLocation newId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BackpackCosmeticSyncPayload> ID = new CustomPacketPayload.Type<>(GuitaBackpacks.id("backpack_cosmetic_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BackpackCosmeticSyncPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			BackpackCosmeticSyncPayload::slotIndex,
			ResourceLocation.STREAM_CODEC,
			BackpackCosmeticSyncPayload::newId,
			BackpackCosmeticSyncPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static void send(int slotIndex, ResourceLocation newId) {
		ClientPlayNetworking.send(new BackpackCosmeticSyncPayload(slotIndex, newId));
	}

	public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<BackpackCosmeticSyncPayload> {

		@Override
		public void receive(BackpackCosmeticSyncPayload payload, ServerPlayNetworking.Context context) {
			ServerPlayer player = context.player();

			if (payload.slotIndex == -1) {
				return;
			}

			ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, payload.slotIndex);
			if (backpack.isEmpty() || !backpack.has(GBComponents.BACKPACK_MODEL_ID.get())) {
				return;
			}

			System.out.println(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));
			backpack.set(GBComponents.BACKPACK_MODEL_ID.get(), payload.newId());
			System.out.println(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));

			if (!EquipmentUtils.isAccessoriesLoaded() && payload.slotIndex >= 20000) {
				EquipmentAttachedData currentData = player.getAttachedOrCreate(
						GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE,
						() -> EquipmentAttachedData.DEFAULT
				);

				EquipmentAttachedData updatedData = currentData.setBackpack(backpack);
				player.setAttached(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, updatedData);
			}
		}
	}
}
