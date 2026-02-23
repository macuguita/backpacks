package com.macuguita.backpacks.client.gui.slots;

import com.macuguita.backpacks.common.attachments.EquipmentAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class EquipmentSlot extends Slot {

	private final Predicate<ItemStack> insertPredicate;
	private final Predicate<ItemStack> canTakePredicate;

	public EquipmentSlot(Container inventory, int index, int x, int y, Predicate<ItemStack> insertPredicate, Predicate<ItemStack> canTakePredicate) {
		super(inventory, index, x, y);
		this.insertPredicate = insertPredicate;
		this.canTakePredicate = canTakePredicate;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (!insertPredicate.test(stack)) {
			return false;
		}
		return super.mayPlace(stack);
	}


	@Override
	public boolean mayPickup(Player player) {
		if (!canTakePredicate.test(this.getItem())) {
			return false;
		}
		return super.mayPickup(player);
	}
}
