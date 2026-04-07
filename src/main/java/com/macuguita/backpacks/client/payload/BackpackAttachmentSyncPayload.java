package com.macuguita.backpacks.client.payload;

import java.util.UUID;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.lib.network.NetworkManager;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record BackpackAttachmentSyncPayload(UUID playerId, ItemStack backpack) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BackpackAttachmentSyncPayload> ID = new CustomPacketPayload.Type<>(GuitaBackpacks.id("backpack_update_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BackpackAttachmentSyncPayload> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			BackpackAttachmentSyncPayload::playerId,
			ItemStack.OPTIONAL_STREAM_CODEC,
			BackpackAttachmentSyncPayload::backpack,
			BackpackAttachmentSyncPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static void send(ServerPlayer player, UUID playerId, ItemStack backpack) {
		NetworkManager.sendS2C(player, new BackpackAttachmentSyncPayload(playerId, backpack));
	}
}
