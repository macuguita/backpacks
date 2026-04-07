package com.macuguita.backpacks.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.macuguita.backpacks.client.payload.BackpackAttachmentSyncPayload;
import com.macuguita.backpacks.common.attachments.PlayerBackpackAttachment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Definition(id = "onPlayerConnect", method = "Lnet/minecraft/server/bossevents/CustomBossEvents;onPlayerConnect(Lnet/minecraft/server/level/ServerPlayer;)V")
	@Expression("?.onPlayerConnect(?)")
	@Inject(
			method = "placeNewPlayer",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		syncBackpacksToPlayer(player);
	}

	@Definition(id = "addRespawnedPlayer", method = "Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V")
	@Expression("?.addRespawnedPlayer(?)")
	@Inject(
			method = "respawn",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private void onPlayerRespawn(ServerPlayer oldPlayer, boolean keepAllPlayerData, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
		ServerPlayer newPlayer = cir.getReturnValue();
		PlayerBackpackAttachment.copyData(oldPlayer, newPlayer);
		syncBackpacksToPlayer(newPlayer);
	}

	@Unique
	private void syncBackpacksToPlayer(ServerPlayer player) {
		player.level().players().forEach(other -> {
			ItemStack backpack = PlayerBackpackAttachment.get(other).getBackpack();
			BackpackAttachmentSyncPayload.send(player, other.getUUID(), backpack);
		});
	}
}
