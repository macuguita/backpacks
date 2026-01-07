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
import com.macuguita.lib.reg.GuitaRegistries;
import com.macuguita.lib.reg.GuitaRegistry;
import com.macuguita.lib.reg.GuitaRegistryEntry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class GBItemGroups {

	static final GuitaRegistry<CreativeModeTab> ITEM_GROUPS = GuitaRegistries.create(BuiltInRegistries.CREATIVE_MODE_TAB, GuitaBackpacks.MOD_ID);

	public static final GuitaRegistryEntry<CreativeModeTab> GW_TAB = ITEM_GROUPS.register("gbackpacks", () ->
			CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup." + GuitaBackpacks.MOD_ID + ".gbackpacks"))
					.icon(() -> new ItemStack(GBObjects.BACKPACK.get().asItem()))
					.displayItems((itemDisplayParameters, output) ->
							GBObjects.ITEMS.stream().map(item -> item.get().getDefaultInstance()).forEach(output::accept)
					).build());

	public static void init() {
		ITEM_GROUPS.init();
	}
}
