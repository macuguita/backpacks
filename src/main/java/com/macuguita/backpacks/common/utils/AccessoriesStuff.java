package com.macuguita.backpacks.common.utils;

import java.util.UUID;

import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.client.render.BackpackAccessoryRenderer;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.attachments.BackpacksAttachedData;
import com.macuguita.backpacks.common.attachments.GBAttachmentTypes;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.reg.GBObjects;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.events.CanUnequipCallback;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.TriState;

public class AccessoriesStuff {

	public static final Identifier BACKPACK_RENDERER = GuitaBackpacks.id("backpack_renderer");

	@Environment(EnvType.CLIENT)
	public static void accessoriesClientInit() {
		AccessoriesRendererRegistry.bindItemToRenderer(GBObjects.BACKPACK.get(), BACKPACK_RENDERER, BackpackAccessoryRenderer::new);
	}

	public static void accessoriesCommonInit() {
		CanUnequipCallback.EVENT.register((stack, reference) -> {
			UUID uuid = stack.get(GBComponents.BACKPACK_UUID.get());
			if (uuid == null) return TriState.DEFAULT;
			if (Boolean.TRUE.equals(GBConfig.getBackpackCanBeUnequippedWhenFull())) return TriState.DEFAULT;
			BackpacksAttachedData attachedData = reference.entity().level().getAttachedOrCreate(GBAttachmentTypes.BACKPACKS_ATTACHMENT_TYPE, () -> BackpacksAttachedData.DEFAULT);
			return TriState.of(attachedData.getInventory(uuid).isEmpty());
		});
	}
}
