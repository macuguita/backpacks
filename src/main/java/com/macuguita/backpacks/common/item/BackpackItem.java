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

import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.macuguita.backpacks.client.gui.BackpackScreenHandler;
import com.macuguita.backpacks.client.gui.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.common.components.BackpacksComponent;
import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.utils.BackpackUtils;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

public class BackpackItem extends BlockItem {

	public BackpackItem(Block block, Settings settings) {
		super(block, settings);
	}

	private static void toggleVisibility(ItemStack stack) {
		Boolean visible = stack.get(GBComponents.VISIBLE.get());
		boolean newValue = visible == null || !visible;
		stack.set(GBComponents.VISIBLE.get(), newValue);
	}

	public static void openOrCreateBackpackIfNotExists(PlayerEntity player, int slotIndex) {
		ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, slotIndex);

		if (backpack.isEmpty()) {
			return;
		}

		UUID uuid = backpack.get(GBComponents.BACKPACK_UUID.get());
		if (uuid == null && player.getEntityWorld() instanceof ServerWorld) {
			if (!backpack.contains(GBComponents.BACKPACK_UUID.get())) {
				backpack.set(GBComponents.BACKPACK_UUID.get(), UUID.randomUUID());
			}
			uuid = backpack.get(GBComponents.BACKPACK_UUID.get());
		}

		BackpacksComponent backpacksComponent = GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(player.getEntityWorld().getScoreboard());
		SimpleInventory inventory = backpacksComponent.getInventory(uuid);
		if (inventory == null) {
			backpacksComponent.addInventory(uuid);
			inventory = backpacksComponent.getInventory(uuid);
		}
		openBackpack(player, inventory, slotIndex);
	}

	@SuppressWarnings("rawtypes")
	public static void openBackpack(PlayerEntity player, SimpleInventory inventory, int slotIndex) {
		var factory = new ExtendedScreenHandlerFactory() {

			@Override
			public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
				ItemStack backpack = EquipmentUtils.getBackpackFromSlotIndex(player, slotIndex);
				return new BackpackScreenHandler(syncId, playerInventory, inventory, backpack);
			}

			@Override
			public Text getDisplayName() {
				return Text.translatable("gui.gbackpacks.backpack");
			}

			@Override
			public Object getScreenOpeningData(ServerPlayerEntity player) {
				return new BackpackInventoryPayload(inventory.size(), slotIndex);
			}
		};

		player.openHandledScreen(factory);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, world, entity, slot);
		if (world instanceof ServerWorld) {
			if (!stack.isEmpty() && !stack.contains(GBComponents.BACKPACK_UUID.get())) {
				stack.set(GBComponents.BACKPACK_UUID.get(), UUID.randomUUID());
			}
			if (entity instanceof PlayerEntity player)
				BackpackUtils.checkForDuplicateBackpacks(player, stack.get(GBComponents.BACKPACK_UUID.get()), stack);
		}
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (stack.contains(GBComponents.BACKPACK_UUID.get())) {
			if (world instanceof ServerWorld) {
				// Find the slot index of the backpack
				int slotIndex = user.getInventory().getSlotWithStack(stack);
				if (slotIndex != -1) {
					openOrCreateBackpackIfNotExists(user, slotIndex);
				}
			} else if (world instanceof ClientWorld clientWorld) {
				Vec3d pos = user.getEntityPos();
				clientWorld.playSoundClient(
						pos.x, pos.y, pos.z,
						SoundEvents.ITEM_BUNDLE_INSERT,
						SoundCategory.PLAYERS,
						1.0f, 1.0f,
						false
				);
			}
		} else {
			return ActionResult.FAIL;
		}
		return super.use(world, user, hand);
	}

	@Override
	protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BackpackBlockEntity backpack) {
			if (stack.contains(GBComponents.BACKPACK_UUID.get())) {
				backpack.setUuid(stack.get(GBComponents.BACKPACK_UUID.get()));
			}
			if (stack.contains(GBComponents.BACKPACK_MODEL_ID.get())) {
				Identifier modelId = stack.get(GBComponents.BACKPACK_MODEL_ID.get());
				if (modelId == null) return super.postPlacement(pos, world, player, stack, state);
				backpack.setItemModelId(modelId);

				String namespace = modelId.getNamespace();
				String path = modelId.getPath();
				path = path.replaceFirst("^backpacks/", "backpacks/blocks/");

				Identifier newModelId = Identifier.of(namespace, path);

				backpack.setBlockModelId(newModelId);
			}
		}
		return super.postPlacement(pos, world, player, stack, state);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
			toggleVisibility(stack);
			return true;
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
	}

	@Override
	public void onItemEntityDestroyed(ItemEntity entity) {
		super.onItemEntityDestroyed(entity);
		ItemStack stack = entity.getStack();
		if (stack.contains(GBComponents.BACKPACK_UUID.get())) {
			UUID uuid = stack.get(GBComponents.BACKPACK_UUID.get());
			if (Boolean.TRUE.equals(GBConfig.getBackpackDropItemsOnDestroyed())) {
				SimpleInventory inv = GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(entity.getEntityWorld().getScoreboard()).getInventory(uuid);
				ItemUsage.spawnItemContents(entity, inv.heldStacks);
			}
			if (Boolean.TRUE.equals(GBConfig.getBackpackEntriesGetRemoved())) {
				GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(entity.getEntityWorld().getScoreboard()).removeBackpack(uuid);
			}
		}
	}
}
