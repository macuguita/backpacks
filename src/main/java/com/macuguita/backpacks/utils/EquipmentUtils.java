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

import com.macuguita.backpacks.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.item.BackpackItem;
import com.macuguita.backpacks.reg.GBComponents;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

public class EquipmentUtils {

	private static final int TRINKET_SLOT_OFFSET = 10000;
	private static final int CUSTOM_EQUIPMENT_SLOT_OFFSET = 20000;

	public static boolean isTrinketsLoaded() {
		return FabricLoader.getInstance().isModLoaded("trinkets");
	}

	public static ItemStack getEquippedBackpack(PlayerEntity player) {
		if (!isTrinketsLoaded())
			return GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(player).getBackpack();
		return TrinketsApi.getTrinketComponent(player)
				.map(component -> component.getEquipped(stack -> stack.getItem() instanceof BackpackItem)
						.stream()
						.findFirst()
						.map(Pair::getRight)
						.orElse(ItemStack.EMPTY))
				.orElse(ItemStack.EMPTY);
	}

	/**
	 * Gets the slot index of the equipped backpack.
	 * For regular inventory: returns 0-40 (standard inventory slots)
	 * For trinkets: returns TRINKET_SLOT_OFFSET + encoded position
	 * For custom equipment: returns CUSTOM_EQUIPMENT_SLOT_OFFSET
	 *
	 * @param player The player to search
	 * @return The slot index, or -1 if no backpack is equipped
	 */
	public static int getBackpackSlotIndex(PlayerEntity player) {

		if (isTrinketsLoaded()) {
			return getTrinketBackpackSlotIndex(player);
		}

		ItemStack customBackpack = GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(player).getBackpack();
		if (!customBackpack.isEmpty() && customBackpack.getItem() instanceof BackpackItem) {
			return CUSTOM_EQUIPMENT_SLOT_OFFSET;
		}

		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.getItem() instanceof BackpackItem && stack.contains(GBComponents.BACKPACK_UUID.get())) {
				return i;
			}
		}

		return -1;
	}

	private static int getTrinketBackpackSlotIndex(PlayerEntity player) {
		TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
		if (trinketComponent == null) {
			return -1;
		}

		int groupIndex = 0;
		for (var groupEntry : trinketComponent.getGroups().entrySet()) {
			String groupId = groupEntry.getKey();
			int slotTypeIndex = 0;

			for (var slotEntry : groupEntry.getValue().getSlots().entrySet()) {
				String slotId = slotEntry.getKey();
				TrinketInventory trinketInv = trinketComponent.getInventory().get(groupId).get(slotId);

				for (int i = 0; i < trinketInv.size(); i++) {
					ItemStack stack = trinketInv.getStack(i);
					if (stack.getItem() instanceof BackpackItem && stack.contains(GBComponents.BACKPACK_UUID.get())) {
						return TRINKET_SLOT_OFFSET + (groupIndex * 1000) + (slotTypeIndex * 100) + i;
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
	 * @param player The player
	 * @param slotIndex The slot index (from getBackpackSlotIndex)
	 * @return The ItemStack, or ItemStack.EMPTY if not found
	 */
	public static ItemStack getBackpackFromSlotIndex(PlayerEntity player, int slotIndex) {
		if (slotIndex < 0) {
			return ItemStack.EMPTY;
		}

		if (slotIndex >= CUSTOM_EQUIPMENT_SLOT_OFFSET) {
			return GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(player).getBackpack();
		}

		if (slotIndex >= TRINKET_SLOT_OFFSET) {
			if (isTrinketsLoaded()) {
				return getBackpackFromTrinketSlotIndex(player, slotIndex);
			}
			return ItemStack.EMPTY;
		}

		if (slotIndex < player.getInventory().size()) {
			return player.getInventory().getStack(slotIndex);
		}

		return ItemStack.EMPTY;
	}

	private static ItemStack getBackpackFromTrinketSlotIndex(PlayerEntity player, int slotIndex) {
		TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
		if (trinketComponent == null) {
			return ItemStack.EMPTY;
		}

		int encoded = slotIndex - TRINKET_SLOT_OFFSET;
		int targetGroupIndex = encoded / 1000;
		int targetSlotTypeIndex = (encoded % 1000) / 100;
		int targetSlot = encoded % 100;

		int groupIndex = 0;
		for (var groupEntry : trinketComponent.getGroups().entrySet()) {
			if (groupIndex == targetGroupIndex) {
				String groupId = groupEntry.getKey();
				int slotTypeIndex = 0;
				for (var slotEntry : groupEntry.getValue().getSlots().entrySet()) {
					if (slotTypeIndex == targetSlotTypeIndex) {
						String slotId = slotEntry.getKey();
						TrinketInventory trinketInv = trinketComponent.getInventory().get(groupId).get(slotId);
						if (targetSlot < trinketInv.size()) {
							return trinketInv.getStack(targetSlot);
						}
						return ItemStack.EMPTY;
					}
					slotTypeIndex++;
				}
			}
			groupIndex++;
		}

		return ItemStack.EMPTY;
	}
}
