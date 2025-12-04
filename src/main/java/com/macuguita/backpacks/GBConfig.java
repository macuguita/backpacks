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

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.macuguita.backpacks.common.GuitaBackpacks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.GsonHelper;

import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("CallToPrintStackTrace")
public class GBConfig {

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("gbackpacks.json");

	private static @Nullable Configuration CONFIG = null;

	public static @Nullable Integer getDefaultBackpackSize() {
		return CONFIG != null ? CONFIG.defaultBackpackSize : null;
	}

	public static @Nullable Boolean getBackpackEntriesGetRemoved() {
		return CONFIG != null ? CONFIG.backpackEntriesGetRemoved : null;
	}

	public static @Nullable Boolean getBackpackDropItemsOnDestroyed() {
		return CONFIG != null ? CONFIG.backpackDropItemsOnDestroyed : null;
	}

	public static @Nullable Boolean getBackpackDropsOnDeath() {
		return CONFIG != null ? CONFIG.backpackDropsOnDeath : null;
	}

	public static void load() {
		try {
			if (!Files.exists(CONFIG_PATH)) {
				createDefaultConfig();
			}

			try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
				var json = GsonHelper.parse(reader);
				var result = Configuration.CODEC.parse(JsonOps.INSTANCE, json);

				if (result.error().isPresent()) {
					GuitaBackpacks.LOGGER.warn("Config file is invalid or missing fields: {}", result.error().get().message());
					GuitaBackpacks.LOGGER.warn("Regenerating config...");
					createDefaultConfig();

					try (var newReader = Files.newBufferedReader(CONFIG_PATH)) {
						json = GsonHelper.parse(newReader);
						result = Configuration.CODEC.parse(JsonOps.INSTANCE, json);
					}
				}

				CONFIG = result.resultOrPartial(msg ->
						GuitaBackpacks.LOGGER.error("Config parse error after regeneration: {}", msg)
				).orElse(null);
			}

		} catch (Exception e) {
			GuitaBackpacks.LOGGER.error("Failed to load config:");
			e.printStackTrace();
		}
	}

	private static void createDefaultConfig() throws IOException {
		Configuration defaultConfig = new Configuration(
				27,
				true,
				true,
				true
		);

		var result = Configuration.CODEC.encodeStart(JsonOps.INSTANCE, defaultConfig);
		var jsonElement = result.getOrThrow();

		if (!Files.exists(CONFIG_PATH.getParent())) {
			Files.createDirectories(CONFIG_PATH.getParent());
		}

		writePrettyJson(jsonElement, CONFIG_PATH);
		GuitaBackpacks.LOGGER.info("Created default config: {}", CONFIG_PATH);
	}

	private static void writePrettyJson(JsonElement element, Path path) throws IOException {
		var gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		try (Writer writer = Files.newBufferedWriter(path)) {
			gson.toJson(element, writer);
		}
	}

	public record Configuration(
			int defaultBackpackSize,
			boolean backpackEntriesGetRemoved,
			boolean backpackDropItemsOnDestroyed,
			boolean backpackDropsOnDeath
	) {

		public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("default_backpack_size").flatXmap(
						size -> size > 0 && size <= 247
								? DataResult.success(size)
								: DataResult.error(() -> "default_backpack_size must be between 1 and 247, got " + size),
						DataResult::success
				).forGetter(Configuration::defaultBackpackSize),
				Codec.BOOL.fieldOf("backpack_entries_get_removed").forGetter(Configuration::backpackEntriesGetRemoved),
				Codec.BOOL.fieldOf("backpack_drop_items_on_destroyed").forGetter(Configuration::backpackDropItemsOnDestroyed),
				Codec.BOOL.fieldOf("backpack_drops_on_death").forGetter(Configuration::backpackDropsOnDeath)
		).apply(instance, Configuration::new));
	}
}
