package com.macuguita.backpacks.mixin;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(
			method = "detectEquipmentUpdates",
			at = @At("TAIL")
	)
	protected void gbackpacks$onDetectEquipmentUpdates(CallbackInfo ci) {}
}
