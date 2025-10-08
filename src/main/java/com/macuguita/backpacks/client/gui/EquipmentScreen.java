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

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EquipmentScreen extends HandledScreen<EquipmentScreenHandler> {

	private static final Identifier BACKGROUND_TEXTURE = GuitaBackpacks.id("background");
	private static final Identifier INVENTORY_AND_HOTBAR_TEXTURE = GuitaBackpacks.id("inventory/inventory_and_hotbar");
	private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

	public EquipmentScreen(EquipmentScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int guiX = (this.width - this.backgroundWidth) / 2;
		int guiY = (this.height - this.backgroundHeight) / 2;

		int yDisplacement = 66;
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, guiX, guiY + yDisplacement, this.backgroundWidth, this.backgroundHeight - yDisplacement);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, guiX + 75, guiY + 38, 26, 26);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INVENTORY_AND_HOTBAR_TEXTURE, guiX + 7, guiY + 83, 162, 76);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, guiX + 79, guiY + 42, 18, 18);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}
}
