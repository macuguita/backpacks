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

package com.macuguita.backpacks.common.components;

import com.macuguita.backpacks.common.GuitaBackpacks;
import com.macuguita.backpacks.common.utils.EquipmentUtils;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;

public class GuitaBackpacksComponents implements ScoreboardComponentInitializer, EntityComponentInitializer {

	public static final ComponentKey<BackpacksComponent> BACKPACKS_COMPONENT =
			ComponentRegistry.getOrCreate(GuitaBackpacks.id("backpacks"), BackpacksComponent.class);

	public static final ComponentKey<EquipmentComponent> EQUIPMENT_COMPONENT =
			ComponentRegistry.getOrCreate(GuitaBackpacks.id("equipment"), EquipmentComponent.class);

	@Override
	public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry scoreboardComponentFactoryRegistry) {
		scoreboardComponentFactoryRegistry.registerScoreboardComponent(
				GuitaBackpacksComponents.BACKPACKS_COMPONENT,
				BackpacksComponent::new
		);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry entityComponentFactoryRegistry) {
		if (!EquipmentUtils.isTrinketsLoaded()) {
			entityComponentFactoryRegistry.registerForPlayers(
					GuitaBackpacksComponents.EQUIPMENT_COMPONENT,
					EquipmentComponent::new,
					RespawnCopyStrategy.ALWAYS_COPY
			);
		}
	}
}
