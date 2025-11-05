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

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EquipmentComponent implements Component, AutoSyncedComponent {

	private final Player player;
	private final SimpleContainer inventory = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			if (!player.level().isClientSide()) {
				GuitaBackpacksComponents.EQUIPMENT_COMPONENT.sync(player);
			}
		}
	};

	public EquipmentComponent(Player player) {
		this.player = player;
	}

	public void clearInventory() {
		inventory.clearContent();
		GuitaBackpacksComponents.EQUIPMENT_COMPONENT.sync(player);
	}

	public SimpleContainer getInventory() {
		return inventory;
	}

	public ItemStack getBackpack() {

		return inventory.getItem(0);
	}

	@Override
	public void readData(ValueInput readView) {
		this.inventory.clearContent();
		ContainerHelper.loadAllItems(readView, this.inventory.items);
	}

	@Override
	public void writeData(ValueOutput writeView) {
		ContainerHelper.saveAllItems(writeView, this.inventory.items);
	}
}
