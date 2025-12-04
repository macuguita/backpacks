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

import com.macuguita.backpacks.client.gui.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.client.gui.slots.BackpackSlot;
import com.macuguita.backpacks.client.gui.slots.CustomSlot;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.reg.GBItemTags;
import com.macuguita.backpacks.common.utils.EquipmentUtils;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackScreenHandler extends AbstractContainerMenu {

	private static final int VISIBLE_ROWS = 6;
	private static final int SLOT_SIZE = 18;
	private static final int GAP_BETWEEN_BACKPACK_AND_PLAYER = 13;
	private static final int TOP_PADDING = 14;
	private static final int SIDE_PADDING = 7;

	public final Container inventory;
	public final ItemStack backpack;
	public final int slotIndex;
	private final int totalRows;
	private final int backpackStartY;
	private final int playerInventoryStartY;
	private int scrollOffset = 0;

	// TODO: should probably migrate to PropertyDelegates https://wiki.fabricmc.net/tutorial:propertydelegates
	public BackpackScreenHandler(int syncId, Inventory playerInventory, BackpackInventoryPayload buf) {
		this(syncId, playerInventory, new SimpleContainer(buf.backpackSize()), buf.slotIndex());
	}

	public BackpackScreenHandler(int syncId, Inventory playerInventory, Container inventory, int slotIndex) {
		super(GuitaBackpacks.BACKPACK_SCREEN_HANDLER, syncId);
		checkContainerSize(inventory, inventory.getContainerSize());
		this.slotIndex = slotIndex;
		this.backpack = EquipmentUtils.getBackpackFromSlotIndex(playerInventory.player, slotIndex);
		this.inventory = inventory;
		this.totalRows = (int) Math.ceil((double) inventory.getContainerSize() / 9.0);

		int visibleRows = Math.min(totalRows, VISIBLE_ROWS);
		int backpackHeight = visibleRows * SLOT_SIZE;

		this.backpackStartY = TOP_PADDING;
		this.playerInventoryStartY = backpackStartY + backpackHeight + GAP_BETWEEN_BACKPACK_AND_PLAYER;

		inventory.startOpen(playerInventory.player);

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			this.addSlot(new BackpackSlot(inventory, i, -1000, -1000, backpack, GBItemTags.BACKPACK_BLACKLIST));
		}

		updateSlotPositions();

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int index = col + row * 9 + 9;
				this.addSlot(new BackpackSlot(playerInventory, index, SIDE_PADDING + col * SLOT_SIZE,
						playerInventoryStartY + row * SLOT_SIZE, backpack));
			}
		}

		int hotbarY = playerInventoryStartY + 3 * SLOT_SIZE + 4;
		for (int col = 0; col < 9; col++) {
			this.addSlot(new BackpackSlot(playerInventory, col, SIDE_PADDING + col * SLOT_SIZE, hotbarY, backpack));
		}
	}

	public void scroll(int direction) {
		int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
		int oldOffset = scrollOffset;

		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + direction));
		if (oldOffset != scrollOffset) updateSlotPositions();
	}

	public void updateSlotPositions() {
		int startIndex = scrollOffset * 9;
		int totalSlots = inventory.getContainerSize();
		int visibleSlots = Math.min(VISIBLE_ROWS * 9, totalSlots - startIndex);

		for (int i = 0; i < visibleSlots; i++) {
			int slotIndex = startIndex + i;
			CustomSlot slot = (CustomSlot) this.slots.get(slotIndex);

			int row = i / 9;
			int col = i % 9;

			int x = SIDE_PADDING + col * SLOT_SIZE;
			int y = backpackStartY + row * SLOT_SIZE;

			slot.gbackpacks$setX(x);
			slot.gbackpacks$setY(y);
		}

		for (int i = 0; i < totalSlots; i++) {
			if (i < startIndex || i >= startIndex + visibleSlots) {
				CustomSlot slot = (CustomSlot) this.slots.get(i);
				slot.gbackpacks$setX(-1000);
				slot.gbackpacks$setY(-1000);
			}
		}
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public int getPlayerInventoryStartY() {
		return playerInventoryStartY;
	}

	public int getBackpackStartY() {
		return backpackStartY;
	}

	public boolean needsScrolling() {
		return totalRows > VISIBLE_ROWS;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int invSlot) {
		Slot slot = this.slots.get(invSlot);
		if (!slot.hasItem()) return ItemStack.EMPTY;

		ItemStack originalStack = slot.getItem();
		ItemStack newStack = originalStack.copy();

		int playerInvStartIndex = inventory.getContainerSize();
		int playerInvEndIndex = this.slots.size();

		boolean moved;
		if (invSlot < playerInvStartIndex) {
			moved = this.moveItemStackTo(originalStack, playerInvStartIndex, playerInvEndIndex, true);
		} else {
			moved = insertItemIntoBackpack(originalStack);
		}

		if (!moved) return ItemStack.EMPTY;

		if (originalStack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return newStack;
	}

	private boolean insertItemIntoBackpack(ItemStack stack) {
		if (stack.is(GBItemTags.BACKPACK_BLACKLIST)) return false;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack slotStack = inventory.getItem(i);
			if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack)) {
				int combined = Math.min(stack.getCount() + slotStack.getCount(), slotStack.getMaxStackSize());
				int transferred = combined - slotStack.getCount();
				if (transferred > 0) {
					slotStack.setCount(combined);
					stack.shrink(transferred);
					if (stack.isEmpty()) return true;
				}
			}
		}

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack slotStack = inventory.getItem(i);
			if (slotStack.isEmpty()) {
				inventory.setItem(i, stack.copy());
				stack.setCount(0);
				return true;
			}
		}

		return false;
	}
}
