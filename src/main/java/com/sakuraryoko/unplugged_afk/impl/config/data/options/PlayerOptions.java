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

package com.sakuraryoko.unplugged_afk.impl.config.data.options;

import com.mojang.authlib.GameProfile;
import com.sakuraryoko.corelib.api.config.IConfigOption;
import com.sakuraryoko.corelib.api.log.AnsiLogger;
import com.sakuraryoko.unplugged_afk.api.state.GameState;
import com.sakuraryoko.unplugged_afk.api.state.PosState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.GameWrap;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.PosWrap;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.ProfileWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.UUID;

@ApiStatus.Internal
public class PlayerOptions implements IConfigOption
{
	public UUID uuid;
	public String name;
	public UnpluggedState state;
	public PosState pos;
	public GameState game;

	public PlayerOptions()
	{
		this.defaults();
	}

	public PlayerOptions(PlayerOptions other)
	{
		this.defaults();
		this.copy(other);
	}

	@Override
	public void defaults()
	{
		this.uuid = UUID.randomUUID();
		this.name = this.uuid.toString();
		this.state = UnpluggedState.DEFAULT;
		this.pos = PosWrap.defaultPos();
		this.game = GameWrap.defMode();
	}

	@Override
	public PlayerOptions copy(IConfigOption other)
	{
		if (other instanceof PlayerOptions opts)
		{
			this.uuid = opts.uuid;
			this.name = opts.name;
			this.state = opts.state.ensureValid();
			this.pos = opts.pos;
			this.game = opts.game;
		}

		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		if (o instanceof PlayerOptions opt)
		{
			// Only match the UUID
			return opt.uuid.equals(this.uuid);
		}

		return false;
	}

	public static PlayerOptions fromProfile(@NotNull GameProfile profile)
	{
		return fromProfile(profile, UnpluggedState.DEFAULT);
	}

	public static PlayerOptions fromProfile(@NotNull GameProfile profile, UnpluggedState state)
	{
		PlayerOptions opts = new PlayerOptions();

		opts.uuid = ProfileWrap.id(profile);
		opts.name = ProfileWrap.name(profile);
		opts.state = state.ensureValid();
		opts.pos = PosWrap.defaultPos();
		opts.game = GameWrap.defMode();

		return opts;
	}

	@VisibleForTesting
	public void dump()
	{
		AnsiLogger logger = new AnsiLogger(PlayerOptions.class, true, true);

		logger.debug("Player Options:");
		logger.debug(" - Name : {}", this.name);
		logger.debug(" - UUID : {}", this.uuid.toString());
		logger.debug(" - State: {}", this.state.toString());
		logger.debug(" - Pos  : {}", this.pos.toString());
		logger.debug(" - Game : {}", this.game.toString());
	}
}
