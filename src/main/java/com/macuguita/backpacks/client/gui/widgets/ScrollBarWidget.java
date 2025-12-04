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

import com.macuguita.backpacks.common.GuitaBackpacks;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ScrollBarWidget extends AbstractWidget {

	private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_TEXTURE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
	private static final ResourceLocation SCROLLER_BACK_TEXTURE = GuitaBackpacks.id("scroll_back");

	public static final int SCROLLER_WIDTH = 12;
	public static final int BACKGROUND_WIDTH = 14;
	public static final int SCROLLER_HEIGHT = 15;

	private final int innerHeight;
	final ScrollCallback callback;
	private float scrollPercent = 0f;
	private boolean scrolling = false;

	public ScrollBarWidget(int x, int y, int height, ScrollCallback callback) {
		super(x, y, BACKGROUND_WIDTH, height + 2, Component.empty());
		this.callback = callback;
		this.innerHeight = height;
	}

	public ScrollCallback getCallback() {
		return callback;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();

		context.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACK_TEXTURE, x, y, this.width, this.height);

		int scrollerX = x + (this.width - SCROLLER_WIDTH) / 2;

		int scrollerY;
		if (innerHeight <= SCROLLER_HEIGHT) {
			scrollerY = y + 1;
		} else {
			scrollerY = y + 1 + (int) (scrollPercent * (innerHeight - SCROLLER_HEIGHT));
		}

		ResourceLocation scrollerTexture = callback.canScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, scrollerTexture, scrollerX, scrollerY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!callback.canScroll() || click.button() != 0) {
			return super.mouseClicked(click, doubled);
		}

		if (!doubled) {
			this.scrolling = true;

			int currentScrollerY;
			if (innerHeight <= SCROLLER_HEIGHT) {
				currentScrollerY = getY() + 1;
			} else {
				currentScrollerY = getY() + 1 + (int) (scrollPercent * (innerHeight - SCROLLER_HEIGHT));
			}

			if (click.y() >= currentScrollerY && click.y() <= currentScrollerY + SCROLLER_HEIGHT) {
				return true;
			}

			updateScroll(click.y(), true);
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
		if (this.scrolling && callback.canScroll()) {
			updateScroll(click.y(), false);
			return true;
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (click.button() == 0) {
			this.scrolling = false;
		}
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (callback.canScroll() && this.isMouseOver(mouseX, mouseY)) {
			int scrollDirection = verticalAmount > 0 ? -1 : 1;
			callback.onScroll(scrollDirection);
			updateScrollPercent();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void updateScroll(double mouseY, boolean snap) {
		float position;
		if (innerHeight <= SCROLLER_HEIGHT) {
			position = 0f;
		} else {
			float denom = (float) (innerHeight - SCROLLER_HEIGHT);
			position = ((float) mouseY - (float) (getY() + 1) - (SCROLLER_HEIGHT / 2.0F)) / denom;
			position = Mth.clamp(position, 0.0F, 1.0F);
		}

		int maxOffset = callback.getMaxScrollOffset();
		int targetOffset = Math.round(position * maxOffset);
		int currentOffset = callback.getCurrentScrollOffset();

		if (targetOffset != currentOffset || snap) {
			callback.scrollTo(targetOffset);
			this.scrollPercent = maxOffset > 0 ? (float) targetOffset / maxOffset : 0f;
		}
	}

	public void updateScrollPercent() {
		int maxOffset = callback.getMaxScrollOffset();
		int currentOffset = callback.getCurrentScrollOffset();
		this.scrollPercent = maxOffset > 0 ? (float) currentOffset / maxOffset : 0f;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, Component.translatable("narration.gbackpacks.scroll_bar"));
	}

	/**
	 * Callback interface for scroll bar interactions
	 */
	public interface ScrollCallback {
		/**
		 * Called when scrolling by a delta amount (e.g., mouse wheel)
		 *
		 * @param delta positive or negative integer for scroll direction
		 */
		void onScroll(int delta);

		/**
		 * Called when scrolling to a specific offset (e.g., dragging)
		 *
		 * @param offset the target scroll offset
		 */
		void scrollTo(int offset);

		/**
		 * @return the maximum scroll offset
		 */
		int getMaxScrollOffset();

		/**
		 * @return the current scroll offset
		 */
		int getCurrentScrollOffset();

		/**
		 * @return whether scrolling is enabled
		 */
		boolean canScroll();
	}
}
