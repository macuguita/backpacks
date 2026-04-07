package com.macuguita.backpacks.mixin;

import com.macuguita.backpacks.client.payload.BackpackAttachmentSyncPayload;
import com.macuguita.backpacks.common.attachments.PlayerBackpackAttachment;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

	@Inject(method = "triggerDimensionChangeTriggers(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
	private void gbackpacks$onDimensionChange(ServerLevel oldLevel, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		ItemStack ownBackpack = PlayerBackpackAttachment.get(player).getBackpack();

		PlayerLookup.tracking(player)
				.forEach(other -> BackpackAttachmentSyncPayload.send(other, player.getUUID(), ownBackpack));

		BackpackAttachmentSyncPayload.send(player, player.getUUID(), ownBackpack);

		player.level().players().forEach(other -> {
			ItemStack backpack = PlayerBackpackAttachment.get(other).getBackpack();
			BackpackAttachmentSyncPayload.send(player, other.getUUID(), backpack);
		});
	}
}
