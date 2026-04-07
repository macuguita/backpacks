package com.macuguita.backpacks.common.saveddata;

import java.util.Map;
import java.util.UUID;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.utils.BackpackUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

public class BackpacksSavedData extends SavedData {

	private static final Identifier BACKPACK_SAVE_ID = GuitaBackpacks.id("backpacks");
	public static Codec<BackpacksSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, BackpackUtils.SIMPLE_CONTAINER_CODEC)
					.fieldOf("containers")
					.forGetter(b -> b.backpacks)).apply(instance, BackpacksSavedData::new));
	public static StreamCodec<ByteBuf, BackpacksSavedData> PACKET_CODEC = ByteBufCodecs.fromCodec(CODEC);
	public static final SavedDataType<BackpacksSavedData> TYPE = new SavedDataType<>(BACKPACK_SAVE_ID, BackpacksSavedData::new, CODEC, null);

	private Map<UUID, SimpleContainer> backpacks = new Object2ObjectOpenHashMap<>();

	public BackpacksSavedData() {
		this.setDirty();
	}

	private BackpacksSavedData(Map<UUID, SimpleContainer> backpacks) {
		this.backpacks = new Object2ObjectOpenHashMap<>();

		for (var entry : backpacks.entrySet()) {
			this.backpacks.put(entry.getKey(), wrapContainer(entry.getValue()));
		}
	}

	public static BackpacksSavedData get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public void removeBackpack(UUID uuid) {
		if (backpacks.remove(uuid) != null) {
			setDirty();
		}
	}

	public void addInventory(UUID uuid, int size) {
		backpacks.put(uuid, new SimpleContainer(size) {
			@Override
			public void setChanged() {
				super.setChanged();
				BackpacksSavedData.this.setDirty();
			}
		});
		setDirty();
	}

	public void addInventory(UUID uuid) {
		int defaultSize = GuitaBackpacks.CONFIG.defaultBackpackSize;

		addInventory(uuid, defaultSize);
	}

	@Nullable
	public SimpleContainer getInventory(UUID uuid) {
		return backpacks.get(uuid);
	}

	private SimpleContainer wrapContainer(SimpleContainer original) {
		SimpleContainer container = new SimpleContainer(original.getContainerSize()) {
			@Override
			public void setChanged() {
				super.setChanged();
				BackpacksSavedData.this.setDirty();
			}
		};

		for (int i = 0; i < original.getContainerSize(); i++) {
			container.setItem(i, original.getItem(i));
		}

		return container;
	}
}
