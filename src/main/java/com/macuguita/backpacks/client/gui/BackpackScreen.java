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

import com.macuguita.backpacks.GuitaBackpacks;
import com.macuguita.backpacks.client.gui.widgets.CustomizationWidget;
import com.macuguita.backpacks.client.gui.widgets.ScrollBarWidget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value = EnvType.CLIENT)
public class BackpackScreen extends HandledScreen<BackpackScreenHandler> {

	private static final Identifier BACKGROUND_TEXTURE = GuitaBackpacks.id("background");
	private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
	private static final Identifier SCROLL_ADDON_TEXTURE = GuitaBackpacks.id("scroll_addon");
	private static final Identifier INVENTORY_AND_HOTBAR_TEXTURE = GuitaBackpacks.id("inventory/inventory_and_hotbar");

	private static final Identifier SLOTS_ROW1_TEXTURE = GuitaBackpacks.id("inventory/slots_row1");
	private static final Identifier SLOTS_ROW2_TEXTURE = GuitaBackpacks.id("inventory/slots_row2");
	private static final Identifier SLOTS_ROW3_TEXTURE = GuitaBackpacks.id("inventory/slots_row3");
	private static final Identifier SLOTS_ROW4_TEXTURE = GuitaBackpacks.id("inventory/slots_row4");
	private static final Identifier SLOTS_ROW5_TEXTURE = GuitaBackpacks.id("inventory/slots_row5");
	private static final Identifier SLOTS_ROW6_TEXTURE = GuitaBackpacks.id("inventory/slots_row6");
	private static final Identifier[] SLOTS_ROW = {
			SLOTS_ROW1_TEXTURE,
			SLOTS_ROW2_TEXTURE,
			SLOTS_ROW3_TEXTURE,
			SLOTS_ROW4_TEXTURE,
			SLOTS_ROW5_TEXTURE,
			SLOTS_ROW6_TEXTURE
	}; //doing this ugly thing to make less calls to draw()

	private static final int VISIBLE_ROWS = 6;
	private static final int SLOT_SIZE = 18;
	private static final int SIDE_PADDING = 7;
	private static final int TOP_PADDING = 14;
	private static final int BOTTOM_PADDING = 7;
	private static final int GAP_BETWEEN_BACKPACK_AND_PLAYER = 13;

	private final int backpackSize;
	private final int visibleBackpackRows;
	private final int backpackStartY;
	private final int playerInventoryStartY;
	private ScrollBarWidget scrollBar;

	public BackpackScreen(BackpackScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backpackSize = handler.inventory.size();

		int totalRows = handler.getTotalRows();
		this.visibleBackpackRows = Math.min(totalRows, VISIBLE_ROWS);

		this.backpackStartY = handler.getBackpackStartY();
		this.playerInventoryStartY = handler.getPlayerInventoryStartY();

		this.backgroundWidth = SIDE_PADDING + (9 * SLOT_SIZE) + SIDE_PADDING;
		this.backgroundHeight = TOP_PADDING +
				(visibleBackpackRows * SLOT_SIZE) +
				GAP_BETWEEN_BACKPACK_AND_PLAYER +
				(3 * SLOT_SIZE) + 4 + SLOT_SIZE + BOTTOM_PADDING;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		int backpackTitleY = 3;
		context.drawText(this.textRenderer, this.title, SIDE_PADDING, backpackTitleY, 4210752, false);

		int playerTitleY = backpackStartY + visibleBackpackRows * SLOT_SIZE + GAP_BETWEEN_BACKPACK_AND_PLAYER / 2 - (this.textRenderer.fontHeight / 2);
		context.drawText(this.textRenderer, this.playerInventoryTitle, SIDE_PADDING, playerTitleY, 4210752, false);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int guiX = (this.width - this.backgroundWidth) / 2;
		int guiY = (this.height - this.backgroundHeight) / 2;

		int backpackRows = Math.min(handler.getTotalRows(), VISIBLE_ROWS);

		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, guiX, guiY - 2, this.backgroundWidth, this.backgroundHeight + 2);

		if (handler.needsScrolling()) {
			int scrollAddonX = guiX + SIDE_PADDING + 9 * SLOT_SIZE + 4;
			int scrollAddonY = guiY + backpackStartY - 4;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SCROLL_ADDON_TEXTURE, scrollAddonX, scrollAddonY,
					ScrollBarWidget.BACKGROUND_WIDTH + 5, backpackRows * SLOT_SIZE + 8);
		}

		int scrollOffset = handler.getScrollOffset();

		int visibleSlots = Math.min(backpackSize - scrollOffset * 9, backpackRows * 9);

		int fullRows = visibleSlots / 9;
		int leftoverSlots = visibleSlots % 9;

		int rowX = guiX + SIDE_PADDING - 1;
		int rowY = guiY + backpackStartY - 1;

		if (fullRows > 0) {
			int rowTextureIndex = Math.min(fullRows - 1, SLOTS_ROW.length - 1);
			int rowsInTexture = rowTextureIndex + 1;

			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOTS_ROW[rowTextureIndex], rowX, rowY, 9 * SLOT_SIZE, rowsInTexture * SLOT_SIZE);

			rowY += rowsInTexture * SLOT_SIZE;
		}

		for (int col = 0; col < leftoverSlots; col++) {
			int slotX = rowX + col * SLOT_SIZE;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, slotX, rowY, SLOT_SIZE, SLOT_SIZE);
		}

		int inventoryX = guiX + SIDE_PADDING - 1;
		int inventoryY = guiY + playerInventoryStartY - 1;
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INVENTORY_AND_HOTBAR_TEXTURE, inventoryX, inventoryY, 162, 76);
	}

	@Override
	protected void init() {
		super.init();

		int guiX = (this.width - this.backgroundWidth) / 2;
		int guiY = (this.height - this.backgroundHeight) / 2;

		int widgetX = guiX + this.backgroundWidth - 18;
		int widgetY = guiY + 2;
		CustomizationWidget customizationWidget = new CustomizationWidget(widgetX, widgetY, 10, 10, null, this, handler.backpack);
		if (!handler.backpack.isEmpty())
			this.addDrawableChild(customizationWidget);

		if (handler.needsScrolling()) {
			int scrollBarX = guiX + SIDE_PADDING + (9 * SLOT_SIZE) + 5;
			int scrollBarY = guiY + backpackStartY;

			this.scrollBar = new ScrollBarWidget(scrollBarX, scrollBarY, visibleBackpackRows * SLOT_SIZE - 2, new ScrollBarWidget.ScrollCallback() {
				@Override
				public void onScroll(int delta) {
					handler.scroll(delta);
					scrollBar.updateScrollPercent();
				}

				@Override
				public void scrollTo(int offset) {
					int current = handler.getScrollOffset();
					handler.scroll(offset - current);
					scrollBar.updateScrollPercent();
				}

				@Override
				public int getMaxScrollOffset() {
					return handler.getTotalRows() - VISIBLE_ROWS;
				}

				@Override
				public int getCurrentScrollOffset() {
					return handler.getScrollOffset();
				}

				@Override
				public boolean canScroll() {
					return handler.needsScrolling();
				}
			});

			this.scrollBar.updateScrollPercent();
			this.addDrawableChild(scrollBar);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (handler.needsScrolling()) {
			int scrollDirection = verticalAmount > 0 ? -1 : 1;
			handler.scroll(scrollDirection);
			if (scrollBar != null) scrollBar.updateScrollPercent();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
}
