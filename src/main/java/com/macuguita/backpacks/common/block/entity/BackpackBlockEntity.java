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
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BackpackBlockEntity extends BlockEntity {

	private ResourceLocation blockModelId;
	private ResourceLocation itemModelId;
	private UUID uuid;

	public BackpackBlockEntity(BlockPos pos, BlockState state) {
		super(GBBlockEntities.BACKPACK, pos, state);
	}

	public ResourceLocation getBlockModelId() {
		return blockModelId;
	}

	public void setBlockModelId(ResourceLocation modelId) {
		this.blockModelId = modelId;
	}

	public ResourceLocation getItemModelId() {
		return itemModelId;
	}

	public void setItemModelId(ResourceLocation itemModelId) {
		this.itemModelId = itemModelId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		view.store("UUID", UUIDUtil.AUTHLIB_CODEC, this.uuid);
		view.putString("Model", this.blockModelId.toString());
		view.putString("ItemModel", this.itemModelId.toString());
	}

	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		this.uuid = view.read("UUID", UUIDUtil.AUTHLIB_CODEC).orElse(null);
		this.blockModelId = ResourceLocation.parse(view.getStringOr("Model", ""));
		this.itemModelId = ResourceLocation.parse(view.getStringOr("ItemModel", ""));
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveWithoutMetadata(registryLookup);
	}
}
