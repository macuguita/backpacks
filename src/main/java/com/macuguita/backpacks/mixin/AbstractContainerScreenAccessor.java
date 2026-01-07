package com.macuguita.backpacks.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

	@Accessor("imageWidth")
	void gbackpacks$setImageWidth(int imageWidth);
	@Accessor("imageHeight")
	void gbackpacks$setImageHeight(int imageHeight);
}
