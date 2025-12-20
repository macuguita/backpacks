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

import com.macuguita.backpacks.client.gui.EquipmentScreenHandler;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record OpenEquipmentPayload() implements CustomPayload {

	public static final Id<OpenEquipmentPayload> ID = new Id<>(GuitaBackpacks.id("open_equipment"));

	public static final PacketCodec<RegistryByteBuf, OpenEquipmentPayload> CODEC = PacketCodec.unit(new OpenEquipmentPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void send() {
		ClientPlayNetworking.send(new OpenEquipmentPayload());
	}

	public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<OpenEquipmentPayload> {

		@Override
		public void receive(OpenEquipmentPayload payload, ServerPlayNetworking.Context context) {
			if (EquipmentUtils.isAccessoriesLoaded()) return;
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
		}
	}
}
