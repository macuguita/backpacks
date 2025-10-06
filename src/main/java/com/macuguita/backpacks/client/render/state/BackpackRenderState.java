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

package com.macuguita.backpacks.client.render.state;

import com.macuguita.backpacks.components.GuitaBackpacksComponents;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

@Environment(EnvType.CLIENT)
public class BackpackRenderState {

	public static final RenderStateDataKey<BackpackRenderState> KEY = RenderStateDataKey.create(() -> "backpack");

	public ItemStack backpack = ItemStack.EMPTY;
	public ItemStack chest = ItemStack.EMPTY;

	public static <E extends LivingEntity, S extends LivingEntityRenderState> void updateRenderState(E entity, S state) {
		BackpackRenderState backpackRenderState = new BackpackRenderState();
		if (!(entity instanceof PlayerEntity)) return;
		backpackRenderState.backpack = GuitaBackpacksComponents.EQUIPMENT_COMPONENT.get(entity).getBackpack();
		backpackRenderState.chest = entity.getEquippedStack(EquipmentSlot.CHEST);
		state.setData(KEY, backpackRenderState);
	}
}
