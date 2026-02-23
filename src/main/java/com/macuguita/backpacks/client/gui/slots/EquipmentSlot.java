package com.macuguita.backpacks.client.gui.slots;

import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.Predicate;

public class EquipmentSlot extends Slot {

	private final Predicate<ItemStack> insertPredicate;
	private final Predicate<ItemStack> canTakePredicate;

	public EquipmentSlot(Inventory inventory, int index, int x, int y, Predicate<ItemStack> insertPredicate, Predicate<ItemStack> canTakePredicate) {
		super(inventory, index, x, y);
		this.insertPredicate = insertPredicate;
		this.canTakePredicate = canTakePredicate;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		if (this.insertPredicate != null && !insertPredicate.test(stack)) {
			return false;
		}
		return super.canInsert(stack);
	}

	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		ItemStack stack = this.getStack();
		if (this.canTakePredicate != null && !canTakePredicate.test(stack)) {
			return false;
		}
		return super.canTakeItems(playerEntity);
	}
}
