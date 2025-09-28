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

package com.macuguita.backpacks.reg;

import java.util.UUID;

import com.macuguita.backpacks.GuitaBackpacks;
import com.macuguita.lib.platform.registry.GuitaRegistries;
import com.macuguita.lib.platform.registry.GuitaRegistry;
import com.macuguita.lib.platform.registry.GuitaRegistryEntry;
import com.mojang.serialization.Codec;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public class GBComponents {

	static final GuitaRegistry<ComponentType<?>> COMPONENTS = GuitaRegistries.create(Registries.DATA_COMPONENT_TYPE, GuitaBackpacks.MOD_ID);

	public static final GuitaRegistryEntry<ComponentType<UUID>> BACKPACK_UUID = COMPONENTS.register("backpack_uuid",
			() -> ComponentType.<UUID>builder().codec(Uuids.CODEC).build());

	public static final GuitaRegistryEntry<ComponentType<Boolean>> VISIBLE = COMPONENTS.register("visible",
			() -> ComponentType.<Boolean>builder().codec(Codec.BOOL).build());

	public static final GuitaRegistryEntry<ComponentType<Identifier>> BACKPACK_MODEL_ID = COMPONENTS.register("backpack_model",
			() -> ComponentType.<Identifier>builder().codec(Identifier.CODEC).build());

	public static void init() {
		COMPONENTS.init();
	}
}
