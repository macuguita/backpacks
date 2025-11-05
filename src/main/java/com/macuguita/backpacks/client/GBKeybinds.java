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

import java.util.Optional;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.payload.OpenBackpackPayload;
import com.macuguita.backpacks.common.payload.OpenEquipmentPayload;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class GBKeybinds {

	public static void init() {
		var category = KeyMapping.Category.register(GuitaBackpacks.id("backpacks"));

		KeyMapping openBackpackKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.gbackpacks.open_backpack",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				category
		));
		Optional<KeyMapping> maybeOpenEquipmentKey = Optional.empty();
		if (!EquipmentUtils.isTrinketsLoaded()) {
			maybeOpenEquipmentKey = Optional.of(KeyBindingHelper.registerKeyBinding(new KeyMapping(
					"key.gbackpacks.open_equipment",
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_G,
					category
			)));
		}
		Optional<KeyMapping> finalMaybeOpenEquipmentKey = maybeOpenEquipmentKey;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openBackpackKey.consumeClick()) {
				OpenBackpackPayload.send();
				if (client.player != null && client.level != null) {
					int backpackSlot = EquipmentUtils.getBackpackSlotIndex(client.player);
					if (backpackSlot != -1) {
						Vec3 pos = client.player.position();
						client.level.playLocalSound(
								pos.x, pos.y, pos.z,
								SoundEvents.BUNDLE_INSERT,
								SoundSource.PLAYERS,
								1.0f, 1.0f,
								false
						);
					}
				}
			}
			if (EquipmentUtils.isTrinketsLoaded()) return;
			finalMaybeOpenEquipmentKey.ifPresent(openEquipmentKey -> {
				while (openEquipmentKey.consumeClick()) {
					OpenEquipmentPayload.send();
				}
			});
		});
	}
}
