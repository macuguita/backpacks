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

package com.macuguita.backpacks.client.payload;

import java.util.List;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.client.GuitaBackpacksClient;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record BackpackListSyncPayload(List<BackpacksResourceReloadListener.Backpack> list) implements CustomPayload {

	public static final CustomPayload.Id<BackpackListSyncPayload> ID = new CustomPayload.Id<>(GuitaBackpacks.id("backpack_list_sync"));

	public static final PacketCodec<RegistryByteBuf, BackpackListSyncPayload> CODEC = PacketCodecs.unlimitedRegistryCodec(
			BackpacksResourceReloadListener.Backpack.CODEC.listOf()
	).xmap(
			BackpackListSyncPayload::new,
			BackpackListSyncPayload::list
	);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void send(ServerPlayerEntity player, List<BackpacksResourceReloadListener.Backpack> list) {
		ServerPlayNetworking.send(player, new BackpackListSyncPayload(list));
	}

	public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<BackpackListSyncPayload> {

		@Override
		public void receive(BackpackListSyncPayload payload, ClientPlayNetworking.Context context) {
			GuitaBackpacksClient.BACKPACKS.clear();
			GuitaBackpacksClient.BACKPACKS.addAll(payload.list());
		}
	}
}
