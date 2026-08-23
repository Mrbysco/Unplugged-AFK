/*
 * This file is part of the Unplugged-AFK project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura-Ryoko and contributors
 *
 * Unplugged-AFK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Unplugged-AFK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Unplugged-AFK.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.unplugged_afk.api;

import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * UnpluggedAFK API Events
 */
public class UnpluggedAfkEvents extends Event
{

	public UnpluggedAfkEvents(@Nullable UUID player, UnpluggedState state) {
		this.player = player;
		this.state = state;
	}

	@Nullable
	private final UUID player;
	private final UnpluggedState state;


	public @Nullable UUID getPlayer() {
		return player;
	}

	public UnpluggedState getState() {
		return state;
	}

	/**
	 * Executes when a Player goes Unplugged
	 * Check the Status portion of the State.
	 */
	public static class Start extends UnpluggedAfkEvents {
		public Start(@Nullable UUID player, UnpluggedState state) {
			super(player, state);
		}
	}

	public static class Respawn extends UnpluggedAfkEvents {
		public Respawn(@Nullable UUID player, UnpluggedState state) {
			super(player, state);
		}
	}

	public static class End extends UnpluggedAfkEvents {
		public End(@Nullable UUID player, UnpluggedState state) {
			super(player, state);
		}
	}
}
