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

package com.macuguita.backpacks.client.gui;

import com.macuguita.backpacks.common.GuitaBackpacks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EquipmentScreen extends AbstractContainerScreen<EquipmentScreenHandler> {

	private static final Identifier BACKGROUND_TEXTURE = GuitaBackpacks.id("background");
	private static final Identifier INVENTORY_AND_HOTBAR_TEXTURE = GuitaBackpacks.id("inventory/inventory_and_hotbar");
	private static final Identifier SLOT_TEXTURE = Identifier.withDefaultNamespace("container/slot");

	public EquipmentScreen(EquipmentScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		int guiX = (this.width - this.imageWidth) / 2;
		int guiY = (this.height - this.imageHeight) / 2;

		int yDisplacement = 66;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, guiX, guiY + yDisplacement, this.imageWidth, this.imageHeight - yDisplacement);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, guiX + 75, guiY + 38, 26, 26);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INVENTORY_AND_HOTBAR_TEXTURE, guiX + 7, guiY + 83, 162, 76);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, guiX + 79, guiY + 42, 18, 18);
	}
}
