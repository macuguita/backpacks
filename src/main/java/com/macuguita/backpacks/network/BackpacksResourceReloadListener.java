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

package com.macuguita.backpacks.network;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.macuguita.backpacks.GuitaBackpacks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2i;
import org.joml.Vector3f;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Box;

public class BackpacksResourceReloadListener implements SynchronousResourceReloader {

	public static final List<Backpack> BACKPACKS = new ArrayList<>();
	private static final Identifier BACKPACKS_DIR = GuitaBackpacks.id("backpacks");
	public static final Identifier ID = GuitaBackpacks.id("backpacks_resource_reload_listener");

	@Override
	public void reload(ResourceManager manager) {
		BACKPACKS.clear();

		var resources = manager.findResources(BACKPACKS_DIR.getPath(), path -> path.getPath().endsWith(".json"));
		for (var entry : resources.entrySet()) {
			var id = entry.getKey();

			try (var reader = new InputStreamReader(entry.getValue().getInputStream())) {
				var json = JsonHelper.deserialize(reader);

				var result = Backpack.CODEC.parse(JsonOps.INSTANCE, json);
				result.resultOrPartial(error -> GuitaBackpacks.LOGGER.warn("Failed to parse backpack at {}: {}", id, error))
						.ifPresent(BACKPACKS::add);
			} catch (Exception e) {
				GuitaBackpacks.LOGGER.error("Error reading backpack at {}: {}", id, e.getMessage(), e);
			}
		}
	}

	public record Backpack(Identifier id, String translationKey, Vector2i guiDisplacement, float guiScale, Box blockCollisionShape) {

		public static final Codec<Vector2i> VECTOR2I_CODEC =
				Codec.INT.listOf().comapFlatMap(
						list -> Util.decodeFixedLengthList(list, 2)
								.map(listi -> new Vector2i(listi.getFirst(), listi.get(1))),
						vector2i -> List.of(vector2i.x, vector2i.y)
				);

		public static final Codec<Box> BOX_CODEC =
				Codecs.VECTOR_3F.listOf().comapFlatMap(
						list -> Util.decodeFixedLengthList(list, 2)
								.map(listv3f -> {
									Vector3f min = listv3f.get(0);
									Vector3f max = listv3f.get(1);
									return new Box(
											min.x / 16.0, min.y / 16.0, min.z / 16.0,
											max.x / 16.0, max.y / 16.0, max.z / 16.0
									);
								}),
						box -> List.of(
								new Vector3f((float) (box.minX * 16), (float) (box.minY * 16), (float) (box.minZ * 16)),
								new Vector3f((float) (box.maxX * 16), (float) (box.maxY * 16), (float) (box.maxZ * 16))
						)
				);

		public static final Codec<Backpack> CODEC = RecordCodecBuilder.create(i -> i.group(
				Identifier.CODEC.fieldOf("id").forGetter(Backpack::id),
				Codec.STRING.fieldOf("translation_key").forGetter(Backpack::translationKey),
				VECTOR2I_CODEC.optionalFieldOf("gui_displacement", new Vector2i(0, 0))
						.forGetter(Backpack::guiDisplacement),
				Codec.FLOAT.optionalFieldOf("gui_scale", 1.0f).flatXmap(
						scale -> scale > 0
								? DataResult.success(scale)
								: DataResult.error(() -> "gui_scale must be higher than 0 " + scale),
						DataResult::success
				).forGetter(Backpack::guiScale),
				BOX_CODEC.optionalFieldOf("block_collision_shape", new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
						.forGetter(Backpack::blockCollisionShape)
		).apply(i, Backpack::new));
	}
}
