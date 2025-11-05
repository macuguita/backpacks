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

package com.macuguita.backpacks.common.reg;

import java.util.UUID;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.lib.platform.registry.GuitaRegistries;
import com.macuguita.lib.platform.registry.GuitaRegistry;
import com.macuguita.lib.platform.registry.GuitaRegistryEntry;
import com.mojang.serialization.Codec;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class GBComponents {

	static final GuitaRegistry<DataComponentType<?>> COMPONENTS = GuitaRegistries.create(BuiltInRegistries.DATA_COMPONENT_TYPE, GuitaBackpacks.MOD_ID);

	public static final GuitaRegistryEntry<DataComponentType<UUID>> BACKPACK_UUID = COMPONENTS.register("backpack_uuid",
			() -> DataComponentType.<UUID>builder().persistent(UUIDUtil.AUTHLIB_CODEC).build());

	public static final GuitaRegistryEntry<DataComponentType<Boolean>> VISIBLE = COMPONENTS.register("visible",
			() -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

	public static final GuitaRegistryEntry<DataComponentType<ResourceLocation>> BACKPACK_MODEL_ID = COMPONENTS.register("backpack_model",
			() -> DataComponentType.<ResourceLocation>builder().persistent(ResourceLocation.CODEC).build());

	public static void init() {
		COMPONENTS.init();
	}
}
