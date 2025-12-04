package com.macuguita.backpacks.common.attachments;

import com.macuguita.backpacks.common.utils.BackpackUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public record EquipmentAttachedData(SimpleContainer inventory) {

	public static final Codec<EquipmentAttachedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BackpackUtils.SIMPLE_CONTAINER_CODEC
					.fieldOf("equipment")
					.forGetter(EquipmentAttachedData::inventory)
	).apply(instance, EquipmentAttachedData::new));

	public static final StreamCodec<ByteBuf, EquipmentAttachedData> PACKET_CODEC =
			ByteBufCodecs.fromCodec(CODEC);

	public static final EquipmentAttachedData DEFAULT =
			new EquipmentAttachedData(new SimpleContainer(1));

	public SimpleContainer getInventory() {
		SimpleContainer copy = new SimpleContainer(inventory.getContainerSize());
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			copy.setItem(i, inventory.getItem(i).copy());
		}
		return copy;
	}

	public ItemStack getBackpack() {
		return inventory.getItem(0).copy();
	}

	public EquipmentAttachedData setBackpack(ItemStack stack) {
		SimpleContainer newInventory = new SimpleContainer(1);
		newInventory.setItem(0, stack.copy());
		return new EquipmentAttachedData(newInventory);
	}

	public EquipmentAttachedData clear() {
		return new EquipmentAttachedData(new SimpleContainer(1));
	}
}
