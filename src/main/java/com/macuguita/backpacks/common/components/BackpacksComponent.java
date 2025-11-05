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
import java.util.Optional;
import java.util.UUID;

import com.macuguita.backpacks.GBConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.Scoreboard;

public class BackpacksComponent implements Component {

	private final Map<UUID, SimpleContainer> backpacks = new HashMap<>();

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
		backpacks.put(uuid, new SimpleContainer(size));
	}

	public SimpleContainer getInventory(UUID uuid) {
		if (uuid == null) return null;
		return backpacks.get(uuid);
	}

	public boolean growBackpack(UUID uuid, int newSize) {
		if (uuid == null) return false;
		SimpleContainer oldInventory = backpacks.get(uuid);

		if (oldInventory != null) {
			int oldSize = oldInventory.getContainerSize();
			if (newSize <= oldSize) return false;

			SimpleContainer newInventory = new SimpleContainer(newSize);
			for (int i = 0; i < oldSize; i++) {
				newInventory.setItem(i, oldInventory.getItem(i));
			}

			backpacks.put(uuid, newInventory);
			return true;
		}

		return false;
	}

	@Override
	public void readData(@NotNull ValueInput readView) {
		backpacks.clear();
		ValueInput.ValueInputList backpacksList = readView.childrenListOrEmpty("Backpacks");

		for (ValueInput backpackReadView : backpacksList) {
			Optional<UUID> uuid = backpackReadView.read("UUID", UUIDUtil.AUTHLIB_CODEC);
			int size = backpackReadView.getIntOr("Size", 0);

			SimpleContainer inventory = new SimpleContainer(size);
			ContainerHelper.loadAllItems(backpackReadView, inventory.items);
			backpacks.put(uuid.orElse(null), inventory);
		}
	}

	@Override
	public void writeData(@NotNull ValueOutput writeView) {
		ValueOutput.ValueOutputList backpacksList = writeView.childrenList("Backpacks");

		for (Map.Entry<UUID, SimpleContainer> entry : backpacks.entrySet()) {
			UUID uuid = entry.getKey();
			SimpleContainer inventory = entry.getValue();

			ValueOutput backpackView = backpacksList.addChild();

			backpackView.store("UUID", UUIDUtil.AUTHLIB_CODEC, uuid);
			backpackView.putInt("Size", inventory.getContainerSize());

			ContainerHelper.saveAllItems(backpackView, inventory.items);
		}
	}
}
