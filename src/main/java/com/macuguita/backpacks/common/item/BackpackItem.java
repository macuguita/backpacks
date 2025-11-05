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

package com.macuguita.backpacks.common.item;

import java.util.UUID;

import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.client.gui.BackpackScreenHandler;
import com.macuguita.backpacks.client.gui.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.macuguita.backpacks.common.components.BackpacksComponent;
import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.utils.BackpackUtils;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

public class BackpackItem extends BlockItem {

	public BackpackItem(Block block, Properties settings) {
		super(block, settings);
	}

	private static void toggleVisibility(@NotNull ItemStack stack) {
		Boolean visible = stack.get(GBComponents.VISIBLE.get());
		boolean newValue = visible == null || !visible;
		stack.set(GBComponents.VISIBLE.get(), newValue);
	}

	public static void openOrCreateBackpackIfNotExists(Player player, int slotIndex) {
		ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, slotIndex);

		if (backpack.isEmpty()) {
			return;
		}

		UUID uuid = backpack.get(GBComponents.BACKPACK_UUID.get());
		if (uuid == null && player.level() instanceof ServerLevel) {
			if (!backpack.has(GBComponents.BACKPACK_UUID.get())) {
				backpack.set(GBComponents.BACKPACK_UUID.get(), UUID.randomUUID());
			}
			uuid = backpack.get(GBComponents.BACKPACK_UUID.get());
		}

		BackpacksComponent backpacksComponent = GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(player.level().getScoreboard());
		SimpleContainer inventory = backpacksComponent.getInventory(uuid);
		if (inventory == null) {
			backpacksComponent.addInventory(uuid);
			inventory = backpacksComponent.getInventory(uuid);
		}
		openBackpack(player, inventory, slotIndex);
	}

	@SuppressWarnings("rawtypes")
	public static void openBackpack(@NotNull Player player, SimpleContainer inventory, int slotIndex) {
		var factory = new ExtendedScreenHandlerFactory() {

			@Override
			public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
				return new BackpackScreenHandler(syncId, playerInventory, inventory, slotIndex);
			}

			@Contract(value = " -> new", pure = true)
			@Override
			public @NotNull Component getDisplayName() {
				return Component.translatable("gui.gbackpacks.backpack");
			}

			@Contract("_ -> new")
			@Override
			public @NotNull Object getScreenOpeningData(ServerPlayer player) {
				return new BackpackInventoryPayload(inventory.getContainerSize(), slotIndex);
			}
		};

		player.openMenu(factory);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, entity, slot);
		if (level instanceof ServerLevel) {
			if (!stack.isEmpty() && !stack.has(GBComponents.BACKPACK_UUID.get())) {
				stack.set(GBComponents.BACKPACK_UUID.get(), UUID.randomUUID());
			}
			if (entity instanceof Player player)
				BackpackUtils.checkForDuplicateBackpacks(player, stack.get(GBComponents.BACKPACK_UUID.get()), stack);
		}
	}

	@Override
	public @NotNull InteractionResult use(Level level, @NotNull Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		if (stack.has(GBComponents.BACKPACK_UUID.get())) {
			if (level instanceof ServerLevel) {
				// Find the slot index of the backpack
				int slotIndex = user.getInventory().findSlotMatchingItem(stack);
				if (slotIndex != -1) {
					openOrCreateBackpackIfNotExists(user, slotIndex);
				}
			} else if (level instanceof ClientLevel clientLevel) {
				Vec3 pos = user.position();
				clientLevel.playLocalSound(
						pos.x, pos.y, pos.z,
						SoundEvents.BUNDLE_INSERT,
						SoundSource.PLAYERS,
						1.0f, 1.0f,
						false
				);
			}
		} else {
			return InteractionResult.FAIL;
		}
		return super.use(level, user, hand);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, @NotNull Level level, @Nullable Player player, ItemStack stack, BlockState state) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack) {
			if (stack.has(GBComponents.BACKPACK_UUID.get())) {
				backpack.setUuid(stack.get(GBComponents.BACKPACK_UUID.get()));
			}
			if (stack.has(GBComponents.BACKPACK_MODEL_ID.get())) {
				ResourceLocation modelId = stack.get(GBComponents.BACKPACK_MODEL_ID.get());
				if (modelId == null) return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
				backpack.setItemModelId(modelId);

				String namespace = modelId.getNamespace();
				String path = modelId.getPath();
				path = path.replaceFirst("^backpacks/", "backpacks/blocks/");

				ResourceLocation newModelId = ResourceLocation.fromNamespaceAndPath(namespace, path);

				backpack.setBlockModelId(newModelId);
			}
		}
		return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
		if (clickType == ClickAction.SECONDARY && otherStack.isEmpty()) {
			toggleVisibility(stack);
			return true;
		}
		return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
	}

	@Override
	public void onDestroyed(ItemEntity entity) {
		super.onDestroyed(entity);
		ItemStack stack = entity.getItem();
		if (stack.has(GBComponents.BACKPACK_UUID.get())) {
			UUID uuid = stack.get(GBComponents.BACKPACK_UUID.get());
			if (Boolean.TRUE.equals(GBConfig.getBackpackDropItemsOnDestroyed())) {
				SimpleContainer inv = GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(entity.level().getScoreboard()).getInventory(uuid);
				ItemUtils.onContainerDestroyed(entity, inv.items);
			}
			if (Boolean.TRUE.equals(GBConfig.getBackpackEntriesGetRemoved())) {
				GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(entity.level().getScoreboard()).removeBackpack(uuid);
			}
		}
	}
}
