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

import java.util.Optional;

import com.macuguita.backpacks.common.attachments.EquipmentAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;
import com.macuguita.backpacks.common.item.BackpackItem;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.loader.api.FabricLoader;

import javax.annotation.Nullable;

public class EquipmentUtils {

	private static final int ACCESSORIES_SLOT_OFFSET = 10000;
	private static final int CUSTOM_EQUIPMENT_SLOT_OFFSET = 20000;

	public static boolean isAccessoriesLoaded() {
		return FabricLoader.getInstance().isModLoaded("accessories");
	}

	public static ItemStack getEquippedBackpack(Player player) {
		if (!isAccessoriesLoaded())
			return player.getAttachedOrCreate(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, () -> EquipmentAttachedData.DEFAULT).getBackpack();

		return AccessoriesCapability.getOptionally(player)
				.map(c -> c.getEquipped(stack -> stack.getItem() instanceof BackpackItem))
				.flatMap(list -> list.stream().findFirst())
				.map(SlotEntryReference::stack)
				.orElse(ItemStack.EMPTY);
	}

	/**
	 * Gets the slot index of the equipped backpack.
	 * For regular inventory: returns 0-40 (standard inventory slots)
	 * For trinkets: returns {@code TRINKET_SLOT_OFFSET} + encoded position
	 * For custom equipment: returns {@code CUSTOM_EQUIPMENT_SLOT_OFFSET}
	 *
	 * @param player The player to search
	 * @return The slot index, or {@code -1} if no backpack is equipped
	 */
	public static int getBackpackSlotIndex(Player player) {

		int result = -1;

		if (isAccessoriesLoaded()) {
			result = getAccessoriesBackpackSlotIndex(player);
		} else {

			ItemStack customBackpack = player.getAttachedOrCreate(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, () -> EquipmentAttachedData.DEFAULT).getBackpack();
			if (!customBackpack.isEmpty() && customBackpack.getItem() instanceof BackpackItem) {
				result = CUSTOM_EQUIPMENT_SLOT_OFFSET;
			}
		}

		if (result != -1) return result;

		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.getItem() instanceof BackpackItem) {
				return i;
			}
		}
		return -1;
	}

	private static int getAccessoriesBackpackSlotIndex(Player player) {
		Optional<AccessoriesCapability> capOpt = AccessoriesCapability.getOptionally(player);
		if (capOpt.isEmpty()) return -1;

		AccessoriesCapability cap = capOpt.get();

		int index = 0;
		for (SlotEntryReference ref : cap.getAllEquipped()) {
			ItemStack stack = ref.stack();
			if (stack.getItem() instanceof BackpackItem) {
				return ACCESSORIES_SLOT_OFFSET + index;
			}
			index++;
		}

		return -1;
	}

	/**
	 * Gets the backpack ItemStack from a slot index.
	 * Handles regular inventory slots, trinket slots, and custom equipment slots.
	 *
	 * @param player    The player
	 * @param slotIndex The slot index (from getBackpackSlotIndex)
	 * @return The {@link ItemStack}, or {@code ItemStack.EMPTY} if not found
	 */
	public static ItemStack getBackpackFromSlotIndex(@Nullable Player player, int slotIndex) {
		if (slotIndex < 0 || player == null) {
			return ItemStack.EMPTY;
		}

		if (slotIndex >= CUSTOM_EQUIPMENT_SLOT_OFFSET) {
			return player.getAttachedOrCreate(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, () -> EquipmentAttachedData.DEFAULT).getBackpack();
		}

		if (slotIndex >= ACCESSORIES_SLOT_OFFSET) {
			if (isAccessoriesLoaded()) {
				return getBackpackFromAccessorySlotIndex(player, slotIndex);
			}
			return ItemStack.EMPTY;
		}

		if (slotIndex < player.getInventory().getContainerSize()) {
			return player.getInventory().getItem(slotIndex);
		}

		return ItemStack.EMPTY;
	}

	private static ItemStack getBackpackFromAccessorySlotIndex(Player player, int slotIndex) {
		Optional<AccessoriesCapability> capOpt = AccessoriesCapability.getOptionally(player);
		if (capOpt.isEmpty()) return ItemStack.EMPTY;

		AccessoriesCapability cap = capOpt.get();

		int encodedIndex = slotIndex - ACCESSORIES_SLOT_OFFSET;
		var allEquipped = cap.getAllEquipped();

		if (encodedIndex < 0 || encodedIndex >= allEquipped.size()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = allEquipped.get(encodedIndex).stack();
		return stack.getItem() instanceof BackpackItem ? stack : ItemStack.EMPTY;
	}
}
