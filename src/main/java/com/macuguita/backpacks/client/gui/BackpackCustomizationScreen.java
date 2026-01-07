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

import java.util.Objects;

import javax.annotation.Nullable;

import com.macuguita.backpacks.client.GuitaBackpacksClient;
import com.macuguita.backpacks.client.gui.widgets.ScrollBarWidget;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.payload.BackpackCosmeticSyncPayload;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.resourcereloader.BackpacksResourceReloadListener;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BackpackCustomizationScreen extends Screen {

	public static final Identifier BACKGROUND_TEXTURE = GuitaBackpacks.id("background");

	private static final int GUI_SHIFT_X = 25;
	private static final int BACKGROUND_WIDTH = 150;
	private static final int BACKGROUND_HEIGHT = 166;
	private static final int ITEM_LIST_GAP = 2;
	private static final int DEFAULT_ITEM_TEXT_COLOR = 0xFFCCCCCC;
	private static final int SELECTED_ITEM_TEXT_COLOR = 0xFFFFFFFF;
	private static final int HOVERED_ITEM_TEXT_COLOR = 0xFFE6E6E6;
	private static final int ITEM_WIDTH = 133;
	private static final int ITEM_HEIGHT = 24;

	final ItemStack backpack;
	final int slotIndex;
	private final Identifier currentModelId;
	public final Screen parent;
	private Identifier selectedModelId;
	private int scrollOffset = 0;
	private @Nullable ScrollBarWidget scrollBar = null;

	public BackpackCustomizationScreen(Component title, Screen parent, int slotIndex) {
		super(title);
		this.parent = parent;
		this.backpack = EquipmentUtils.getBackpackFromSlotIndex(Minecraft.getInstance().player, slotIndex);
		this.slotIndex = slotIndex;
		this.currentModelId = Objects.requireNonNull(backpack.get(GBComponents.BACKPACK_MODEL_ID.get()));
		this.selectedModelId = this.currentModelId;
	}

	@Override
	protected void init() {
		super.init();

		int i = (this.width - BACKGROUND_WIDTH) / 2 + GUI_SHIFT_X;
		int j = (this.height - BACKGROUND_HEIGHT) / 2;

		int scrollBarX = i + BACKGROUND_WIDTH - ScrollBarWidget.BACKGROUND_WIDTH - 10;
		int scrollBarY = j + 10;
		this.scrollBar = this.addRenderableWidget(
				new ScrollBarWidget(scrollBarX, scrollBarY, BACKGROUND_HEIGHT - 45, new ScrollBarWidget.ScrollCallback() {
					@Override
					public void onScroll(int delta) {
						scrollOffset += delta * 10;
						scrollOffset = Math.max(0, Math.min(scrollOffset, getMaxScrollOffset()));
					}

					@Override
					public void scrollTo(int offset) {
						scrollOffset = Math.max(0, Math.min(offset, getMaxScrollOffset()));
					}

					@Override
					public int getMaxScrollOffset() {
						if (scrollBar == null) return 0;
						return Math.max(0, GuitaBackpacksClient.BACKPACKS.size() * (ITEM_HEIGHT + ITEM_LIST_GAP) - (scrollBar.getHeight() - 30));
					}

					@Override
					public int getCurrentScrollOffset() {
						return scrollOffset;
					}

					@Override
					public boolean canScroll() {
						return getMaxScrollOffset() > 0;
					}
				})
		);

		this.addRenderableWidget(
				Button.builder(Component.translatable("gui.done"), _ -> {
							if (!this.selectedModelId.equals(this.currentModelId)) {
								BackpackCosmeticSyncPayload.send(this.slotIndex, this.selectedModelId);
							}
							this.onClose();
						})
						.bounds(i + 10, j + BACKGROUND_HEIGHT - 30, BACKGROUND_WIDTH - 20, 20)
						.build()
		);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);

		int i = (this.width - BACKGROUND_WIDTH) / 2 + GUI_SHIFT_X;
		int j = (this.height - BACKGROUND_HEIGHT) / 2;

		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, i, j, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

		int listBgX = i + 10;
		int listBgY = j + 10;
		int listBgWidth = BACKGROUND_WIDTH - ScrollBarWidget.BACKGROUND_WIDTH - 23;
		int listBgHeight = scrollBar != null ? scrollBar.getHeight() : 0;
		context.fill(listBgX, listBgY, listBgX + listBgWidth, listBgY + listBgHeight, 0xFF373737);

		context.enableScissor(listBgX, listBgY, listBgX + listBgWidth, listBgY + listBgHeight);

		int padding = 2;
		int slotWidth = listBgWidth - (padding * 2);
		int startX = listBgX + padding;
		int startY = listBgY + padding;

		for (int index = 0; index < GuitaBackpacksClient.BACKPACKS.size(); index++) {
			BackpacksResourceReloadListener.Backpack backpackItem = GuitaBackpacksClient.BACKPACKS.get(index);
			int y = startY + index * (ITEM_HEIGHT + ITEM_LIST_GAP) - scrollOffset;

			if (y + ITEM_HEIGHT < listBgY || y > listBgY + listBgHeight) continue;

			boolean isSelected = backpackItem.id().equals(this.selectedModelId);
			boolean isHovered = mouseX >= startX && mouseX <= startX + slotWidth &&
					mouseY >= y && mouseY <= y + ITEM_HEIGHT;

			int bgColor = isSelected ? 0xFF4A90E2 :
					isHovered ? 0xFF5A5A5A :
							0xFF454545;
			context.fill(startX, y, startX + slotWidth, y + ITEM_HEIGHT, bgColor);

			if (isSelected) {
				context.fill(startX, y, startX + slotWidth, y + 1, 0xFF6AB0FF);
				context.fill(startX, y + ITEM_HEIGHT - 1, startX + slotWidth, y + ITEM_HEIGHT, 0xFF2A70C2);
			}

			int textColor = isSelected ? SELECTED_ITEM_TEXT_COLOR :
					isHovered ? HOVERED_ITEM_TEXT_COLOR :
							DEFAULT_ITEM_TEXT_COLOR;

			context.drawString(this.font, Component.translatable(backpackItem.translationKey()),
					startX + 8, y + 8, textColor, isSelected);
		}
		context.disableScissor();

		int spacing = 10;
		int playerPadding = 10;
		int playerWidth = 100;
		int playerHeight = BACKGROUND_HEIGHT - 2 * playerPadding;
		float yOffset = 0.2625F;
		int renderSize = 120;

		int renderX0 = i - playerWidth - playerPadding - spacing;
		int renderY0 = j + playerPadding;
		int renderX1 = renderX0 + playerWidth;
		int renderY1 = renderY0 + playerHeight;

		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, i - playerWidth - playerPadding*2 - spacing, j, playerWidth + playerPadding*2, BACKGROUND_HEIGHT);

		context.fill(renderX0, renderY0, renderX1, renderY1, 0xFF373737);

		renderEntityInInventoryFollowsMouse(
				context,
				renderX0,
				renderY0,
				renderX1,
				renderY1,
				renderSize,
				yOffset,
				mouseX,
				mouseY,
				this.minecraft.player
		);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (scrollBar != null && scrollBar.getCallback().canScroll()) {
			int scrollDirection = verticalAmount > 0 ? -1 : 1;
			scrollOffset += scrollDirection * 10;
			scrollOffset = Math.max(0, Math.min(scrollOffset, scrollBar.getCallback().getMaxScrollOffset()));
			scrollBar.updateScrollPercent();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		int i = (this.width - BACKGROUND_WIDTH) / 2;
		int j = (this.height - BACKGROUND_HEIGHT) / 2;

		int startX = i + 12;
		int startY = j + 7;
		int listHeight = scrollBar != null ? scrollBar.getHeight() - 30 : 0;

		for (int index = 0; index < GuitaBackpacksClient.BACKPACKS.size(); index++) {
			int y = startY + index * (ITEM_HEIGHT + ITEM_LIST_GAP) - scrollOffset;

			if (click.x() >= startX && click.x() <= startX + ITEM_WIDTH &&
					click.y() >= y && click.y() <= y + ITEM_HEIGHT &&
					y >= j + 5 && y + ITEM_HEIGHT <= j + 5 + listHeight) {
				BackpacksResourceReloadListener.Backpack clicked = GuitaBackpacksClient.BACKPACKS.get(index);

				this.selectedModelId = clicked.id();
				BackpackCosmeticSyncPayload.send(this.slotIndex, this.selectedModelId);
				if (this.scrollBar != null) {
					this.scrollBar.updateScrollPercent();
				}
				return true;
			}
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	public static void renderEntityInInventoryFollowsMouse(
			final GuiGraphics graphics,
			final int x0,
			final int y0,
			final int x1,
			final int y1,
			final int size,
			final float offsetY,
			final float mouseX,
			final float mouseY,
			final LivingEntity entity
	) {
		float centerX = (x0 + x1) / 2.0F;
		float centerY = (y0 + y1) / 2.0F;

		float xAngle = (float) -Math.atan((centerX - mouseX) / 40.0F);
		float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI)
				.rotateY((float) Math.PI);
		Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));

		rotation.mul(xRotation);
		EntityRenderState renderState = extractRenderState(entity);
		if (renderState instanceof LivingEntityRenderState livingRenderState) {
			livingRenderState.bodyRot = 180.0F + xAngle * 20.0F;
			livingRenderState.yRot = xAngle * 20.0F;
			if (livingRenderState.pose != Pose.FALL_FLYING) {
				livingRenderState.xRot = -yAngle * 20.0F;
			} else {
				livingRenderState.xRot = 0.0F;
			}

			livingRenderState.boundingBoxWidth = livingRenderState.boundingBoxWidth / livingRenderState.scale;
			livingRenderState.boundingBoxHeight = livingRenderState.boundingBoxHeight / livingRenderState.scale;
			livingRenderState.scale = 1.0F;
		}

		Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + offsetY, 0.0F);
		graphics.submitEntityRenderState(renderState, size, translation, rotation, xRotation, x0, y0, x1, y1);
	}

	private static EntityRenderState extractRenderState(final LivingEntity entity) {
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
		EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
		renderState.shadowPieces.clear();
		renderState.outlineColor = 0;
		return renderState;
	}
}
