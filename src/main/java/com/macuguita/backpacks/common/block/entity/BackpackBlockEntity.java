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

package com.macuguita.backpacks.common.block.entity;

import java.util.UUID;

import com.macuguita.backpacks.common.reg.GBBlockEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public class BackpackBlockEntity extends BlockEntity {

	private Identifier blockModelId;
	private Identifier itemModelId;
	private UUID uuid;

	public BackpackBlockEntity(BlockPos pos, BlockState state) {
		super(GBBlockEntities.BACKPACK, pos, state);
	}

	public Identifier getBlockModelId() {
		return blockModelId;
	}

	public void setBlockModelId(Identifier modelId) {
		this.blockModelId = modelId;
	}

	public Identifier getItemModelId() {
		return itemModelId;
	}

	public void setItemModelId(Identifier itemModelId) {
		this.itemModelId = itemModelId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		view.put("UUID", Uuids.CODEC, this.uuid);
		view.putString("Model", this.blockModelId.toString());
		view.putString("ItemModel", this.itemModelId.toString());
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		this.uuid = view.read("UUID", Uuids.CODEC).orElse(null);
		this.blockModelId = Identifier.of(view.getString("Model", ""));
		this.itemModelId = Identifier.of(view.getString("ItemModel", ""));
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createNbt(registryLookup);
	}
}
