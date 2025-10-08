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

package com.macuguita.backpacks.client.gui;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.client.gui.slots.BackpackSlot;
import com.macuguita.backpacks.common.item.BackpackItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class EquipmentScreenHandler extends ScreenHandler {

	private final Inventory inventory;

	public EquipmentScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(1));
	}

	public EquipmentScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(GuitaBackpacks.EQUIPMENT_SCREEN_HANDLER, syncId);
		checkSize(inventory, 1);
		this.inventory = inventory;
		inventory.onOpen(playerInventory.player);

		int m;
		int l;
		this.addSlot(new BackpackSlot(inventory, 0, 80, 43, stack -> stack.getItem() instanceof BackpackItem));

		for (m = 0; m < 3; ++m) {
			for (l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		for (m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot sourceSlot = this.slots.get(index);

		if (sourceSlot.hasStack()) {
			ItemStack originalStack = sourceSlot.getStack();
			newStack = originalStack.copy();

			int containerSlotCount = this.inventory.size();

			if (index < containerSlotCount) {
				if (!this.insertItem(originalStack, containerSlotCount, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.insertItem(originalStack, 0, containerSlotCount, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (originalStack.isEmpty()) {
				sourceSlot.setStack(ItemStack.EMPTY);
			} else {
				sourceSlot.markDirty();
			}
		}

		return newStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
}
