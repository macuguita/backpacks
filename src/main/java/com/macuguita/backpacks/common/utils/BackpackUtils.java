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

package com.macuguita.backpacks.common.utils;

import java.util.List;
import java.util.UUID;

import com.macuguita.backpacks.common.reg.GBComponents;
import org.jetbrains.annotations.NotNull;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public class BackpackUtils {

	public static void checkForDuplicateBackpacks(@NotNull Player player, UUID backpackUuid, ItemStack backpack) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack other = inv.getItem(i);
			if (other == backpack) continue;
			if (other.has(GBComponents.BACKPACK_UUID.get())) {
				UUID otherUuid = other.get(GBComponents.BACKPACK_UUID.get());
				if (backpackUuid.equals(otherUuid)) {
					other.remove(GBComponents.BACKPACK_UUID.get());
				}
			}
		}
	}

	public static void dedupeBackpackItemEntity(@NotNull ItemEntity newEntity) {
		ItemStack newStack = newEntity.getItem();
		if (!newStack.has(GBComponents.BACKPACK_UUID.get())) return;

		UUID newUuid = newStack.get(GBComponents.BACKPACK_UUID.get());

		List<ItemEntity> nearby = newEntity.level().getEntitiesOfClass(
				ItemEntity.class,
				newEntity.getBoundingBox().inflate(10),
				Entity::isAlive
		);

		for (ItemEntity otherEntity : nearby) {
			if (otherEntity == newEntity) continue;
			ItemStack otherStack = otherEntity.getItem();
			if (otherStack.has(GBComponents.BACKPACK_UUID.get())) {
				UUID otherUuid = otherStack.get(GBComponents.BACKPACK_UUID.get());
				if (newUuid != null && newUuid.equals(otherUuid)) {
					newStack.remove(GBComponents.BACKPACK_UUID.get());
					break;
				}
			}
		}
	}

	public static class DeduplicateBackpacks implements ServerEntityEvents.Load {

		@Override
		public void onLoad(Entity entity, ServerLevel serverWorld) {
			if (!(entity instanceof ItemEntity itemEntity)) return;

			ItemStack stack = itemEntity.getItem();
			if (!stack.has(GBComponents.BACKPACK_UUID.get())) return;

			dedupeBackpackItemEntity(itemEntity);
		}
	}
}
