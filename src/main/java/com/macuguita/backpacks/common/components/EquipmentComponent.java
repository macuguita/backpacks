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

package com.macuguita.backpacks.common.components;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public class EquipmentComponent implements Component, AutoSyncedComponent {

	private final PlayerEntity player;
	private final SimpleInventory inventory = new SimpleInventory(1) {
		@Override
		public void markDirty() {
			super.markDirty();
			if (!player.getWorld().isClient) {
				GuitaBackpacksComponents.EQUIPMENT_COMPONENT.sync(player);
			}
		}
	};

	public EquipmentComponent(PlayerEntity player) {
		this.player = player;
	}

	public void clearInventory() {
		inventory.clear();
		GuitaBackpacksComponents.EQUIPMENT_COMPONENT.sync(player);
	}

	public SimpleInventory getInventory() {
		return inventory;
	}

	public ItemStack getBackpack() {

		return inventory.getStack(0);
	}

	@Override
	public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		this.inventory.clear();
		Inventories.readNbt(nbtCompound, this.inventory.heldStacks, wrapperLookup);
	}

	@Override
	public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		Inventories.writeNbt(nbtCompound, this.inventory.heldStacks, wrapperLookup);
	}
}
