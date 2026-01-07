package com.macuguita.backpacks.common.utils;

import com.macuguita.backpacks.client.render.BackpackAccessoryRenderer;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.reg.GBObjects;
//import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class AccessoriesStuff {

	public static final Identifier BACKPACK_RENDERER = GuitaBackpacks.id("backpack_renderer");

	@Environment(EnvType.CLIENT)
	public static void accessoriesClientInit() {
//		AccessoriesRendererRegistry.bindItemToRenderer(GBObjects.BACKPACK.get(), BACKPACK_RENDERER, BackpackAccessoryRenderer::new);
	}
}
