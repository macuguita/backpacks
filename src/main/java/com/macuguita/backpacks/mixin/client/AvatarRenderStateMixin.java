package com.macuguita.backpacks.mixin.client;

import com.macuguita.backpacks.pond.AvatarRenderStateDuck;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateDuck {
	@Unique
	private @Nullable ItemStack gbackpacks$backpack;

	@Override
	public @Nullable ItemStack gbackpacks$backpack() {
		return gbackpacks$backpack;
	}

	@Override
	public void gbackpacks$setBackpack(ItemStack backpack) {
		gbackpacks$backpack = backpack;
	}
}
