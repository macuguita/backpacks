package com.macuguita.backpacks.common.attachments;

import com.macuguita.backpacks.common.GuitaBackpacks;

import com.macuguita.backpacks.common.utils.EquipmentUtils;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class GBAttachmentTypes {

	public static final AttachmentType<BackpacksAttachedData> BACKPACKS_ATTACHMENT_TYPE = AttachmentRegistry.create(
			GuitaBackpacks.id("backpacks"),
			builder -> builder.initializer(() -> BackpacksAttachedData.DEFAULT).persistent(BackpacksAttachedData.CODEC).syncWith(
					BackpacksAttachedData.PACKET_CODEC, AttachmentSyncPredicate.all()));

	public static final AttachmentType<EquipmentAttachedData> EQUIPMENT_ATTACHMENT_TYPE = AttachmentRegistry.create(
			GuitaBackpacks.id("equipment"),
			builder -> builder.initializer(() -> EquipmentAttachedData.DEFAULT).persistent(EquipmentAttachedData.CODEC).copyOnDeath()
					.syncWith(
							EquipmentAttachedData.PACKET_CODEC, AttachmentSyncPredicate.all()));

	public static void init() {
	}
}
