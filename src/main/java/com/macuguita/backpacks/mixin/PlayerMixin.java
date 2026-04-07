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

package com.macuguita.backpacks.mixin;

import com.macuguita.backpacks.client.payload.BackpackAttachmentSyncPayload;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.attachments.PlayerBackpackAttachment;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

@Mixin(Player.class)
public class PlayerMixin extends LivingEntityMixin implements PlayerBackpackAttachment.Provider {

	@Unique
	private final PlayerBackpackAttachment playerBackpackAttachment = new PlayerBackpackAttachment((Player) (Object) this);

	@Unique
	private ItemStack gbackpacks$lastBackpack = ItemStack.EMPTY;

	@Override
	public PlayerBackpackAttachment gbackpacks$getAttachment() {
		return playerBackpackAttachment;
	}

	@Inject(
			method = "readAdditionalSaveData",
			at = @At("TAIL")
	)
	private void gbackpacks$readBackpackAttachment(ValueInput input, CallbackInfo ci) {
		if (input.contains("gbackpacks")) {
			this.playerBackpackAttachment.readData(input.childOrEmpty("gbackpacks"));
		} else {
			// Migrates from old version to the best of my abilities
			input.child("fabric:attachments")
					.filter(a -> a.contains("gbackpacks:equipment"))
					.flatMap(a -> a.child("gbackpacks:equipment"))
					.flatMap(e -> e.child("equipment"))
					.flatMap(inv -> inv.read("items", ItemStack.CODEC.listOf())
							.filter(list -> !list.isEmpty())).ifPresent(list ->
							this.playerBackpackAttachment.getInventory().setItem(0, list.getFirst()));
		}
	}

	@Inject(
			method = "addAdditionalSaveData",
			at = @At("TAIL")
	)
	private void gbackpacks$writeBackpackAttachment(ValueOutput output, CallbackInfo ci) {
		this.playerBackpackAttachment.writeData(output.child("gbackpacks"));
	}

	@Inject(
			method = "dropEquipment",
			at = @At("TAIL")
	)
	private void gbackpacks$dropInventory(CallbackInfo info) {
		if (EquipmentUtils.isAccessoriesLoaded()) return;
		if (!GuitaBackpacks.CONFIG.backpackDropsOnDeath) return;
		Player player = (Player) (Object) this;
		boolean keepInv = ((ServerLevel) player.level()).getGameRules().get(GameRules.KEEP_INVENTORY);

		ItemStack stack = EquipmentUtils.getEquippedBackpack(player);
		if (!keepInv && !stack.isEmpty()) {
			player.drop(stack.copy(), true, false);
			PlayerBackpackAttachment.get(player).clear();
		}
	}

	@Override
	protected void gbackpacks$onDetectEquipmentUpdates(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (player.level().isClientSide()) return;

		ItemStack current = this.playerBackpackAttachment.getBackpack();
		ItemStack previous = this.gbackpacks$lastBackpack;

		if (!ItemStack.isSameItemSameComponents(current, previous)) {
			this.gbackpacks$lastBackpack = current.isEmpty() ? ItemStack.EMPTY : current.copy();

			PlayerLookup.tracking(player)
					.forEach(receiver -> BackpackAttachmentSyncPayload.send(receiver, player.getUUID(), current));

			if (player instanceof ServerPlayer serverPlayer) {
				BackpackAttachmentSyncPayload.send(serverPlayer, player.getUUID(), current);
			}
		}
	}
}
