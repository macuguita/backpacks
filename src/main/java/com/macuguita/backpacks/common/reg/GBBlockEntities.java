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

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.block.entity.BackpackBlockEntity;
import com.macuguita.lib.reg.GuitaRegistries;
import com.macuguita.lib.reg.GuitaRegistry;
import com.macuguita.lib.reg.GuitaRegistryEntry;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;

public class GBBlockEntities {

	static final GuitaRegistry<BlockEntityType<?>> BLOCK_ENTITIES = GuitaRegistries.create(Registries.BLOCK_ENTITY_TYPE, GuitaBackpacks.MOD_ID);

	public static final GuitaRegistryEntry<BlockEntityType<BackpackBlockEntity>> BACKPACK = BLOCK_ENTITIES.register(
			"backpack", () -> BlockEntityType.Builder.create(BackpackBlockEntity::new, GBObjects.BACKPACK_BLOCK.get()).build());

	public static void init() {
		BLOCK_ENTITIES.init();
	}
}
