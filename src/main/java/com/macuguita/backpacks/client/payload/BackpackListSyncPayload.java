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

import com.macuguita.backpacks.client.GuitaBackpacksClient;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record BackpackListSyncPayload(
		List<BackpacksResourceReloadListener.Backpack> list) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BackpackListSyncPayload> ID = new CustomPacketPayload.Type<>(GuitaBackpacks.id("backpack_list_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BackpackListSyncPayload> CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(
			BackpacksResourceReloadListener.Backpack.CODEC.listOf()
	).map(
			BackpackListSyncPayload::new,
			BackpackListSyncPayload::list
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static void send(ServerPlayer player, List<BackpacksResourceReloadListener.Backpack> list) {
		ServerPlayNetworking.send(player, new BackpackListSyncPayload(list));
	}
}
