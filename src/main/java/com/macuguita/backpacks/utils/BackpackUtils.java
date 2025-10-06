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

package com.macuguita.backpacks.utils;

import java.util.List;
import java.util.UUID;

import com.macuguita.backpacks.reg.GBComponents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class BackpackUtils {

	public static void checkForDuplicateBackpacks(PlayerEntity player, UUID backpackUuid, ItemStack backpack) {
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack other = inv.getStack(i);
			if (other == backpack) continue;
			if (other.contains(GBComponents.BACKPACK_UUID.get())) {
				UUID otherUuid = other.get(GBComponents.BACKPACK_UUID.get());
				if (backpackUuid.equals(otherUuid)) {
					other.remove(GBComponents.BACKPACK_UUID.get());
				}
			}
		}
	}

	public static void dedupeBackpackItemEntity(ItemEntity newEntity) {
		ItemStack newStack = newEntity.getStack();
		if (!newStack.contains(GBComponents.BACKPACK_UUID.get())) return;

		UUID newUuid = newStack.get(GBComponents.BACKPACK_UUID.get());

		List<ItemEntity> nearby = newEntity.getEntityWorld().getEntitiesByClass(
				ItemEntity.class,
				newEntity.getBoundingBox().expand(10),
				Entity::isAlive
		);

		for (ItemEntity otherEntity : nearby) {
			if (otherEntity == newEntity) continue;
			ItemStack otherStack = otherEntity.getStack();
			if (otherStack.contains(GBComponents.BACKPACK_UUID.get())) {
				UUID otherUuid = otherStack.get(GBComponents.BACKPACK_UUID.get());
				if (newUuid != null && newUuid.equals(otherUuid)) {
					newStack.remove(GBComponents.BACKPACK_UUID.get());
					break;
				}
			}
		}
	}
}
