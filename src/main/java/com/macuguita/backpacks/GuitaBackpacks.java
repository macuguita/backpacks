/*
 * Copyright (c) 2025 macuguita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.macuguita.backpacks;

import com.macuguita.backpacks.client.gui.BackpackScreenHandler;
import com.macuguita.backpacks.client.gui.EquipmentScreenHandler;
import com.macuguita.backpacks.config.GBConfig;
import com.macuguita.backpacks.network.GBNetworking;
import com.macuguita.backpacks.network.payload.BackpackInventoryPayload;
import com.macuguita.backpacks.reg.GBBlockEntities;
import com.macuguita.backpacks.reg.GBComponents;
import com.macuguita.backpacks.reg.GBItemGroups;
import com.macuguita.backpacks.reg.GBObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

public class GuitaBackpacks implements ModInitializer {
	public static final String MOD_ID = "gbackpacks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier DEFAULT_BACKPACK_MODEL_ID = GuitaBackpacks.id("backpacks/backpack");

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	public static final ExtendedScreenHandlerType<BackpackScreenHandler, BackpackInventoryPayload> BACKPACK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(BackpackScreenHandler::new, BackpackInventoryPayload.CODEC);
	public static final ScreenHandlerType<EquipmentScreenHandler> EQUIPMENT_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, id("equipment"), new ScreenHandlerType<>(EquipmentScreenHandler::new, FeatureSet.empty()));

	@Override
	public void onInitialize() {
		GBNetworking.init();
		Registry.register(Registries.SCREEN_HANDLER, id("backpack"), BACKPACK_SCREEN_HANDLER);
		GBConfig.load();
		GBComponents.init();
		GBObjects.init();
		GBBlockEntities.init();
		GBItemGroups.init();
		GBNetworking.initEvents();
		GBNetworking.initGlobalReceivers();
	}
}
