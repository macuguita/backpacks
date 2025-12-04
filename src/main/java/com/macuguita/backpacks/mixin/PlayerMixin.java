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

import com.llamalad7.mixinextras.sugar.Local;
import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.common.attachments.EquipmentAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

@Mixin(Player.class)
public class PlayerMixin {

	@Inject(
			method = "dropEquipment",
			at = @At("TAIL")
	)
	private void gbackpacks$dropInventory(
			CallbackInfo info,
			@Local(argsOnly = true) ServerLevel level
	) {
		if (EquipmentUtils.isAccessoriesLoaded()) return;
		if (Boolean.FALSE.equals(GBConfig.getBackpackDropsOnDeath())) return;
		Player player = (Player) (Object) this;
		boolean keepInv = ((ServerLevel) player.level()).getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

		ItemStack stack = EquipmentUtils.getEquippedBackpack(player);
		if (!keepInv && !stack.isEmpty()) {
			player.drop(stack.copy(), true, false);
			EquipmentAttachedData equipmentAttachedData = player.getAttachedOrCreate(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, () -> EquipmentAttachedData.DEFAULT);
			player.setAttached(GBAttachmentTypes.EQUIPMENT_ATTACHMENT_TYPE, equipmentAttachedData.clear());
		}
	}
}
