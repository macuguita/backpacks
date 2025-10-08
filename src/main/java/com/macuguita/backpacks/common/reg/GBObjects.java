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
import com.macuguita.backpacks.common.block.BackpackBlock;
import com.macuguita.backpacks.common.item.BackpackItem;
import com.macuguita.lib.platform.registry.GuitaRegistries;
import com.macuguita.lib.platform.registry.GuitaRegistry;
import com.macuguita.lib.platform.registry.GuitaRegistryEntry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public class GBObjects {

	static final GuitaRegistry<Block> BLOCKS = GuitaRegistries.create(Registries.BLOCK, GuitaBackpacks.MOD_ID);
	static final GuitaRegistry<Item> ITEMS = GuitaRegistries.create(Registries.ITEM, GuitaBackpacks.MOD_ID);

	public static final GuitaRegistryEntry<BackpackBlock> BACKPACK_BLOCK = BLOCKS.register("backpack", () -> new BackpackBlock(AbstractBlock.Settings.create()
			.nonOpaque()
			.noBlockBreakParticles()));

	public static final GuitaRegistryEntry<BackpackItem> BACKPACK = ITEMS.register("backpack", () -> new BackpackItem(BACKPACK_BLOCK.get(), new Item.Settings()
			.maxCount(1).component(GBComponents.VISIBLE.get(), true).component(GBComponents.BACKPACK_MODEL_ID.get(), GuitaBackpacks.DEFAULT_BACKPACK_MODEL_ID)));

	public static void init() {
		BLOCKS.init();
		ITEMS.init();
	}
}
