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

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class GBItemGroups {

	static final GuitaRegistry<ItemGroup> ITEM_GROUPS = GuitaRegistries.create(Registries.ITEM_GROUP, GuitaBackpacks.MOD_ID);

	public static final GuitaRegistryEntry<ItemGroup> GW_TAB = ITEM_GROUPS.register("gbackpacks", () ->
			ItemGroup.create(ItemGroup.Row.TOP, 0)
					.displayName(Text.translatable("itemGroup." + GuitaBackpacks.MOD_ID + ".gbackpacks"))
					.icon(() -> new ItemStack(GBObjects.BACKPACK.get().asItem()))
					.entries((itemDisplayParameters, output) ->
							GBObjects.ITEMS.stream().map(item -> item.get().getDefaultStack()).forEach(output::add)
					).build());

	public static void init() {
		ITEM_GROUPS.init();
	}
}
