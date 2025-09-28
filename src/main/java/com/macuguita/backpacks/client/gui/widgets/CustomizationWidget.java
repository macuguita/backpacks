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

import com.macuguita.backpacks.GuitaBackpacks;
import com.macuguita.backpacks.client.gui.BackpackCustomizationScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomizationWidget extends ClickableWidget {

	public static final Identifier WIDGET_ICON = GuitaBackpacks.id("textures/gui/widget/customize.png");
	private final ItemStack backpack;
	public final Screen parent;

	public CustomizationWidget(int x, int y, int width, int height, Text message, Screen parent, ItemStack backpack) {
		super(x, y, width, height, message);
		this.parent = parent;
		this.backpack = backpack;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (backpack.isEmpty()) return;
		context.drawTexture(WIDGET_ICON, getX(), getY(), 0, 0,
				this.width, this.height, 10, 10);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		if (backpack.isEmpty()) return;
		builder.put(NarrationPart.TITLE, Text.translatable("narration.gbackpacks.customization_widget"));
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (backpack.isEmpty()) return;
		super.onClick(mouseX, mouseY);
		MinecraftClient client = MinecraftClient.getInstance();
		client.setScreen(new BackpackCustomizationScreen(Text.translatable("gui.gbackpacks.customization"), parent, backpack));
	}
}
