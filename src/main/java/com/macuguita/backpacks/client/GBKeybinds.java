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
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class GBKeybinds {

	public static void init() {
		var category = KeyBinding.Category.create(GuitaBackpacks.id("backpacks"));

		KeyBinding openBackpackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.gbackpacks.open_backpack",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				category
		));
		Optional<KeyBinding> maybeOpenEquipmentKey = Optional.empty();
		if (!EquipmentUtils.isTrinketsLoaded()) {
			maybeOpenEquipmentKey = Optional.of(KeyBindingHelper.registerKeyBinding(new KeyBinding(
					"key.gbackpacks.open_equipment",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_G,
					category
			)));
		}
		Optional<KeyBinding> finalMaybeOpenEquipmentKey = maybeOpenEquipmentKey;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openBackpackKey.wasPressed()) {
				OpenBackpackPayload.send();
				if (client.player != null && client.world != null) {
					int backpackSlot = EquipmentUtils.getBackpackSlotIndex(client.player);
					if (backpackSlot != -1) {
						Vec3d pos = client.player.getEntityPos();
						client.world.playSoundClient(
								pos.x, pos.y, pos.z,
								SoundEvents.ITEM_BUNDLE_INSERT,
								SoundCategory.PLAYERS,
								1.0f, 1.0f,
								false
						);
					}
				}
			}
			if (EquipmentUtils.isTrinketsLoaded()) return;
			finalMaybeOpenEquipmentKey.ifPresent(openEquipmentKey -> {
				while (openEquipmentKey.wasPressed()) {
					OpenEquipmentPayload.send();
				}
			});
		});
	}
}
