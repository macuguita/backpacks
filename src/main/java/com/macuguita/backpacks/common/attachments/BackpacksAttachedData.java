package com.macuguita.backpacks.common.attachments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.common.utils.BackpackUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;

import org.jspecify.annotations.Nullable;

public record BackpacksAttachedData(Map<UUID, SimpleContainer> backpacks) {

	public static Codec<BackpacksAttachedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, BackpackUtils.SIMPLE_CONTAINER_CODEC)
					.fieldOf("containers")
					.forGetter(BackpacksAttachedData::backpacks)).apply(instance, BackpacksAttachedData::new));
	public static StreamCodec<ByteBuf, BackpacksAttachedData> PACKET_CODEC = ByteBufCodecs.fromCodec(CODEC);

	public static BackpacksAttachedData DEFAULT = new BackpacksAttachedData(new HashMap<>());

	public BackpacksAttachedData removeBackpack(UUID uuid) {
		if (!backpacks.containsKey(uuid)) return this;

		Map<UUID, SimpleContainer> newMap = new HashMap<>(backpacks);
		newMap.remove(uuid);

		return new BackpacksAttachedData(newMap);
	}

	public BackpacksAttachedData addInventory(UUID uuid, int size) {
		Map<UUID, SimpleContainer> newMap = new HashMap<>(backpacks);
		newMap.put(uuid, new SimpleContainer(size));

		return new BackpacksAttachedData(newMap);
	}

	public BackpacksAttachedData addInventory(UUID uuid) {
		Integer defaultSize = GBConfig.getDefaultBackpackSize();
		if (defaultSize == null) return this;

		return addInventory(uuid, defaultSize);
	}

	public @Nullable SimpleContainer getInventory(UUID uuid) {
		return backpacks.get(uuid);
	}

	public BackpacksAttachedData clear() {
		return DEFAULT;
	}
}
