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

package com.macuguita.backpacks.client.gui.slots;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;

public class BackpackSlot extends Slot {

	@Nullable
	private final TagKey<Item> backpackBlacklist;
	@Nullable
	private final Predicate<ItemStack> insertPredicate;
	private final ItemStack backpack;

	public BackpackSlot(Inventory inventory, int index, int x, int y, @Nullable Predicate<ItemStack> insertPredicate) {
		this(inventory, index, x, y, null, null, insertPredicate);
	}

	public BackpackSlot(Inventory inventory, int index, int x, int y, ItemStack backpack, @Nullable TagKey<Item> backpackBlacklist) {
		this(inventory, index, x, y, backpack, backpackBlacklist, null);
	}

	public BackpackSlot(Inventory inventory, int index, int x, int y, ItemStack backpack, @Nullable TagKey<Item> backpackBlacklist, @Nullable Predicate<ItemStack> insertPredicate) {
		super(inventory, index, x, y);
		this.backpack = backpack;
		this.backpackBlacklist = backpackBlacklist;
		this.insertPredicate = insertPredicate;
	}

	public BackpackSlot(Inventory inventory, int index, int x, int y, ItemStack backpack) {
		this(inventory, index, x, y, backpack, null);
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		if (backpackBlacklist != null && stack.isIn(backpackBlacklist)) {
			return false;
		}

		if (insertPredicate != null && !insertPredicate.test(stack)) {
			return false;
		}

		return super.canInsert(stack);
	}

	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		if (backpack != null && !backpack.isEmpty()) {
			ItemStack current = this.getStack();
			if (ItemStack.areItemsEqual(current, backpack)) {
				return false;
			}
		}
		return super.canTakeItems(playerEntity);
	}
}
