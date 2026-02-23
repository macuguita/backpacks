package com.macuguita.backpacks.common.utils;

import com.macuguita.backpacks.GBConfig;
import com.macuguita.backpacks.client.render.BackpackAccessoryRenderer;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.components.GuitaBackpacksComponents;
import com.macuguita.backpacks.common.reg.GBComponents;
import com.macuguita.backpacks.common.reg.GBObjects;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;

import io.wispforest.accessories.api.events.CanUnequipCallback;

import net.fabricmc.fabric.api.util.TriState;

import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

public class AccessoriesStuff {

	public static final Identifier BACKPACK_RENDERER = GuitaBackpacks.id("backpack_renderer");

	@Environment(EnvType.CLIENT)
	public static void accessoriesClientInit() {
		AccessoriesRendererRegistry.registerRenderer(GBObjects.BACKPACK.get(), BackpackAccessoryRenderer::new);
	}

	public static void accessoriesCommonInit() {
		CanUnequipCallback.EVENT.register((stack, reference) -> {
			UUID uuid = stack.get(GBComponents.BACKPACK_UUID.get());
			if (uuid == null) return TriState.DEFAULT;
			if (Boolean.TRUE.equals(GBConfig.getBackpackCanBeUnequippedWhenFull())) return TriState.DEFAULT;
			return TriState.of(GuitaBackpacksComponents.BACKPACKS_COMPONENT.get(reference.entity().getEntityWorld().getScoreboard()).isEmpty(uuid));
		});
	}
}
