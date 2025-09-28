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

package com.macuguita.backpacks.datagen;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.macuguita.backpacks.reg.GBObjects;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class GBLangProvider extends FabricLanguageProvider {

	public GBLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(dataOutput, "en_us", registryLookup);
	}

	@Override
	public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
		generateBlockTranslations(translationBuilder, GBObjects.BACKPACK_BLOCK.get());
		translationBuilder.add("itemGroup.gbackpacks.gbackpacks", "guita's Backpack");
		translationBuilder.add("gui.gbackpacks.backpack", "Backpack");
		translationBuilder.add("gui.gbackpacks.customization", "Backpack Customization");
		translationBuilder.add("item.gbackpacks.backpack.tooltip.hidden", "Hidden");
		translationBuilder.add("item.gbackpacks.backpack.tooltip.cosmetic", "Backpack cosmetic: ");
		translationBuilder.add("item.gbackpacks.backpack.tooltip.uuid", "UUID of backpack: %s");
		translationBuilder.add("item.gbackpacks.backpack.tooltip.uuid.hidden", "Press [SHIFT] to show");
		translationBuilder.add("backpack.gbackpacks.backpack", "Backpack");
		translationBuilder.add("backpack.gbackpacks.big_backpack", "Big backpack");
		translationBuilder.add("key.gbackpacks.open_backpack", "Open backpack");
		translationBuilder.add("key.gbackpacks.open_equipment", "Open equipment inventory");
		translationBuilder.add("key.categories.gbackpacks", "guita's Backpacks");
		translationBuilder.add("narration.gbackpacks.customization_widget", "Backpack customization widget");
		translationBuilder.add("narration.gbackpacks.scroll_bar", "Scroll bar widget");
	}

	private String capitalizeString(String string) {
		char[] chars = string.toLowerCase(Locale.getDefault()).toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; ++i) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
				found = false;
			}
		}
		return new String(chars);
	}

	private void generateBlockTranslations(TranslationBuilder translationBuilder, Block block) {
		String temp = capitalizeString(Registries.BLOCK.getId(block).getPath().replace("_", " "));
		translationBuilder.add(block, temp);
	}

	private void generateItemTranslations(TranslationBuilder translationBuilder, Item item) {
		String temp = capitalizeString(Registries.ITEM.getId(item).getPath().replace("_", " "));
		translationBuilder.add(item, temp);
	}
}
