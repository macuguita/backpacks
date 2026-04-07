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

import java.util.Map;

import javax.annotation.Nullable;

import com.macuguita.backpacks.common.attachments.PlayerBackpackAttachment;
import com.macuguita.backpacks.common.item.BackpackItem;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.loader.api.FabricLoader;

//import io.wispforest.accessories.api.AccessoriesCapability;
//import io.wispforest.accessories.api.slot.SlotEntryReference;

public class EquipmentUtils {

	private static final int MODDED_SLOT_OFFSET = 10000;
	public static final int CUSTOM_EQUIPMENT_SLOT_OFFSET = 200000;

	public static boolean isAccessoriesLoaded() {
		return FabricLoader.getInstance().isModLoaded("accessories");
	}

	public static boolean isTrinketsLoaded() {
		return FabricLoader.getInstance().isModLoaded("trinkets");
	}

	public static boolean isCompatibleModLoaded() {
		return isTrinketsLoaded() || isAccessoriesLoaded();
	}

	public static ItemStack getEquippedBackpack(Player player) {
		if (isAccessoriesLoaded()) {
//		return AccessoriesCapability.getOptionally(player)
//				.map(c -> c.getEquipped(stack -> stack.getItem() instanceof BackpackItem))
//				.flatMap(list -> list.stream().findFirst())
//				.map(SlotEntryReference::stack)
//				.orElse(ItemStack.EMPTY);
		} else if (isTrinketsLoaded()) {
			return TrinketsApi.getAttachment(player)
					.getEquipped(stack -> stack.getItem() instanceof BackpackItem)
					.stream()
					.findFirst()
					.map(Tuple::getB)
					.orElse(ItemStack.EMPTY);
		}

		return PlayerBackpackAttachment.get(player).getBackpack();
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
		} else if (isTrinketsLoaded()) {
			result = getTrinketsBackpackSlotIndex(player);
		} else {

			ItemStack customBackpack = PlayerBackpackAttachment.get(player).getBackpack();
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
//		Optional<AccessoriesCapability> capOpt = AccessoriesCapability.getOptionally(player);
//		if (capOpt.isEmpty()) return -1;
//
//		AccessoriesCapability cap = capOpt.get();
//
//		int index = 0;
//		for (SlotEntryReference ref : cap.getAllEquipped()) {
//			ItemStack stack = ref.stack();
//			if (stack.getItem() instanceof BackpackItem) {
//				return ACCESSORIES_SLOT_OFFSET + index;
//			}
//			index++;
//		}

		return -1;
	}

	private static int getTrinketsBackpackSlotIndex(Player player) {
		TrinketAttachment comp = TrinketsApi.getAttachment(player);
		if (comp == null) return -1;

		int groupIndex = 0;

		var sortedGroups = comp.getGroups().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.toList();

		for (var groupEntry : sortedGroups) {
			String groupId = groupEntry.getKey();

			int slotTypeIndex = 0;

			var sortedSlots = groupEntry.getValue().slots().entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.toList();

			for (var slotEntry : sortedSlots) {
				String slotId = slotEntry.getKey();
				TrinketInventory inv = comp.getInventory().get(groupId).get(slotId);

				for (int i = 0; i < inv.getContainerSize(); i++) {
					ItemStack stack = inv.getItem(i);
					if (stack.getItem() instanceof BackpackItem) {

						return MODDED_SLOT_OFFSET
								+ (groupIndex << 16)
								+ (slotTypeIndex << 8)
								+ i;
					}
				}
				slotTypeIndex++;
			}
			groupIndex++;
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
			return PlayerBackpackAttachment.get(player).getBackpack();
		}

		if (slotIndex >= MODDED_SLOT_OFFSET) {
			if (isAccessoriesLoaded()) {
				return getBackpackFromAccessorySlotIndex(player, slotIndex);
			} else if (isTrinketsLoaded()) {
				return getBackpackFromTrinketSlotIndex(player, slotIndex);
			}
			return ItemStack.EMPTY;
		}

		if (slotIndex < player.getInventory().getContainerSize()) {
			return player.getInventory().getItem(slotIndex);
		}

		return ItemStack.EMPTY;
	}

	private static ItemStack getBackpackFromAccessorySlotIndex(Player player, int encoded) {
//		Optional<AccessoriesCapability> capOpt = AccessoriesCapability.getOptionally(player);
//		if (capOpt.isEmpty()) return ItemStack.EMPTY;
//
//		AccessoriesCapability cap = capOpt.get();
//
//		int encodedIndex = encoded - ACCESSORIES_SLOT_OFFSET;
//		var allEquipped = cap.getAllEquipped();
//
//		if (encodedIndex < 0 || encodedIndex >= allEquipped.size()) {
//			return ItemStack.EMPTY;
//		}
//
//		ItemStack stack = allEquipped.get(encodedIndex).stack();
//		return stack.getItem() instanceof BackpackItem ? stack : ItemStack.EMPTY;
		return ItemStack.EMPTY;
	}

	private static ItemStack getBackpackFromTrinketSlotIndex(Player player, int encoded) {
		if (encoded < 0) return ItemStack.EMPTY;

		TrinketAttachment comp = TrinketsApi.getAttachment(player);
		if (comp == null) return ItemStack.EMPTY;

		int raw = encoded - MODDED_SLOT_OFFSET;

		int groupIndex = (raw >> 16) & 0xFF;
		int slotTypeIndex = (raw >> 8) & 0xFF;
		int slotIndex = raw & 0xFF;

		var sortedGroups = comp.getGroups().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.toList();

		if (groupIndex >= sortedGroups.size()) return ItemStack.EMPTY;

		var groupEntry = sortedGroups.get(groupIndex);
		String groupId = groupEntry.getKey();

		var sortedSlots = groupEntry.getValue().slots().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.toList();

		if (slotTypeIndex >= sortedSlots.size()) return ItemStack.EMPTY;

		var slotEntry = sortedSlots.get(slotTypeIndex);
		String slotId = slotEntry.getKey();

		TrinketInventory inv = comp.getInventory().get(groupId).get(slotId);

		if (slotIndex >= inv.getContainerSize()) return ItemStack.EMPTY;

		ItemStack stack = inv.getItem(slotIndex);
		return stack.getItem() instanceof BackpackItem ? stack : ItemStack.EMPTY;
	}
}
