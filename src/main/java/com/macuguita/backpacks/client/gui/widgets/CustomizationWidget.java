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

package com.macuguita.backpacks.client.gui.widgets;

import com.macuguita.backpacks.client.gui.BackpackCustomizationScreen;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.utils.EquipmentUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CustomizationWidget extends AbstractWidget {

	public static final ResourceLocation WIDGET_ICON = GuitaBackpacks.id("textures/gui/widget/customize.png");
	private final int slotIndex;
	private final ItemStack backpack;
	public final Screen parent;

	public CustomizationWidget(int x, int y, int width, int height, Component message, Screen parent, int slotIndex) {
		super(x, y, width, height, message);
		this.parent = parent;
		this.slotIndex = slotIndex;
		this.backpack = EquipmentUtils.getBackpackFromSlotIndex(Minecraft.getInstance().player, slotIndex);
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (backpack.isEmpty()) return;
		context.blit(RenderPipelines.GUI_TEXTURED, WIDGET_ICON, getX(), getY(), 0, 0,
				this.width, this.height, 10, 10);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		if (backpack.isEmpty()) return;
		builder.add(NarratedElementType.TITLE, Component.translatable("narration.gbackpacks.customization_widget"));
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (backpack.isEmpty()) return;
		super.onClick(click, doubled);
		Minecraft client = Minecraft.getInstance();
		client.setScreen(new BackpackCustomizationScreen(Component.translatable("gui.gbackpacks.customization"), parent, slotIndex));
	}
}
