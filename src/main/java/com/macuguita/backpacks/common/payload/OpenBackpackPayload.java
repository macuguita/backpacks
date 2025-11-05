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
import com.macuguita.backpacks.common.item.BackpackItem;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record OpenBackpackPayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<OpenBackpackPayload> ID = new CustomPacketPayload.Type<>(GuitaBackpacks.id("open_backpack"));

	public static final StreamCodec<RegistryFriendlyByteBuf, OpenBackpackPayload> CODEC = StreamCodec.unit(new OpenBackpackPayload());

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static void send() {
		ClientPlayNetworking.send(new OpenBackpackPayload());
	}

	public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<OpenBackpackPayload> {

		@Override
		public void receive(OpenBackpackPayload payload, ServerPlayNetworking.@NotNull Context context) {
			ServerPlayer player = context.player();
			int backpackSlot = EquipmentUtils.getBackpackSlotIndex(player);
			if (backpackSlot != -1) {
				BackpackItem.openOrCreateBackpackIfNotExists(player, backpackSlot);
			}
		}
	}
}
