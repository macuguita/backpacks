package com.macuguita.backpacks.common.attachments;

import java.util.function.Predicate;

import com.macuguita.backpacks.client.payload.BackpackAttachmentSyncPayload;

import com.macuguita.backpacks.common.GuitaBackpacks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

public class PlayerBackpackAttachment {

	private SimpleContainer inventory;
	private final Player player;

	public PlayerBackpackAttachment(Player player) {
		this.player = player;
		this.inventory = new SimpleContainer(1);
	}

	public static PlayerBackpackAttachment get(Player player) {
		return ((PlayerBackpackAttachment.Provider) player).gbackpacks$getAttachment();
	}

	public SimpleContainer getInventory() {
		return inventory;
	}

	public void setInventory(SimpleContainer inventory) {
		this.inventory = inventory;
		if (!player.level().isClientSide()) {
			sendUpdatePacket();
		}
	}

	public ItemStack getBackpack() {
		return inventory.getItem(0);
	}

	public void setBackpack(ItemStack stack) {
		inventory.setItem(0, stack.copy());
		if (!player.level().isClientSide()) {
			sendUpdatePacket();
		}
	}

	public void clear() {
		inventory.setItem(0, ItemStack.EMPTY);
		if (!player.level().isClientSide()) {
			sendUpdatePacket();
		}
	}

	public boolean isEquipped(Predicate<ItemStack> predicate) {
		return predicate.test(inventory.getItem(0));
	}

	public void readData(ValueInput input) {
		inventory.setItem(0, input.read("backpack", ItemStack.CODEC).orElse(ItemStack.EMPTY));
	}

	public void writeData(ValueOutput output) {
		output.store("backpack", ItemStack.CODEC, inventory.getItem(0));
	}

	private void sendUpdatePacket() {
		var stack = getBackpack();

		PlayerLookup.tracking(this.player)
				.forEach(receiver -> BackpackAttachmentSyncPayload.send(receiver, player.getUUID(), stack));

		if (this.player instanceof ServerPlayer serverPlayer) {
			BackpackAttachmentSyncPayload.send(serverPlayer, player.getUUID(), stack);
		}
	}

	public static void copyData(Player from, Player to, ConversionParams conversionParams) {
		if (!conversionParams.keepEquipment()) {
			return;
		}

		copyData(from, to);
	}

	public static void copyData(Player from, Player to, boolean restoreAll) {
		copyData(from, to);
	}

	public static void copyData(Player from, Player to) {
		try (var errorReporter = new ProblemReporter.ScopedCollector(GuitaBackpacks.LOGGER)) {
			TagValueOutput writeView = TagValueOutput.createWithContext(errorReporter, from.registryAccess());
			get(from).writeData(writeView);
			get(to).readData(TagValueInput.create(errorReporter, to.registryAccess(), writeView.buildResult()));
		}
	}

	public interface Provider {
		PlayerBackpackAttachment gbackpacks$getAttachment();
	}
}
