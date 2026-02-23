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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.macuguita.backpacks.GBConfig;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;

public class BackpacksComponent implements Component {

	private final Map<UUID, SimpleInventory> backpacks = new HashMap<>();

	public BackpacksComponent(Scoreboard provider, @Nullable MinecraftServer server) {
	}

	public void removeBackpack(UUID uuid) {
		backpacks.remove(uuid);
	}

	public void addInventory(UUID uuid) {
		assert GBConfig.getDefaultBackpackSize() != null;
		if (uuid == null) return;
		addInventory(uuid, GBConfig.getDefaultBackpackSize());
	}

	public void addInventory(UUID uuid, int size) {
		if (uuid == null) return;
		backpacks.put(uuid, new SimpleInventory(size));
	}

	public SimpleInventory getInventory(UUID uuid) {
		if (uuid == null) return null;
		return backpacks.get(uuid);
	}

	public boolean isEmpty(UUID uuid) {
		if (uuid == null) return true;
		SimpleInventory inventory = this.getInventory(uuid);
		if (inventory == null) return true;
		return inventory.isEmpty();
	}

	public boolean growBackpack(UUID uuid, int newSize) {
		if (uuid == null) return false;
		SimpleInventory oldInventory = backpacks.get(uuid);

		if (oldInventory != null) {
			int oldSize = oldInventory.size();
			if (newSize <= oldSize) return false;

			SimpleInventory newInventory = new SimpleInventory(newSize);
			for (int i = 0; i < oldSize; i++) {
				newInventory.setStack(i, oldInventory.getStack(i));
			}

			backpacks.put(uuid, newInventory);
			return true;
		}

		return false;
	}

	@Override
	public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		backpacks.clear();
		NbtList backpacksNbtList = nbtCompound.getList("Backpacks", NbtElement.COMPOUND_TYPE);

		for (int i = 0; i < backpacksNbtList.size(); i++) {
			NbtCompound backpackTag = backpacksNbtList.getCompound(i);
			UUID uuid = backpackTag.getUuid("UUID");
			int size = backpackTag.getInt("Size");

			SimpleInventory inventory = new SimpleInventory(size);
			Inventories.readNbt(backpackTag, inventory.heldStacks, wrapperLookup);
			backpacks.put(uuid, inventory);
		}
	}

	@Override
	public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
		NbtList backpacksNbtList = new NbtList();

		for (Map.Entry<UUID, SimpleInventory> entry : backpacks.entrySet()) {
			UUID uuid = entry.getKey();
			SimpleInventory inventory = entry.getValue();

			NbtCompound backpackTag = new NbtCompound();
			backpackTag.putUuid("UUID", uuid);
			backpackTag.putInt("Size", inventory.size());

			Inventories.writeNbt(backpackTag, inventory.heldStacks, wrapperLookup);
			backpacksNbtList.add(backpackTag);
		}

		nbtCompound.put("Backpacks", backpacksNbtList);
	}
}
