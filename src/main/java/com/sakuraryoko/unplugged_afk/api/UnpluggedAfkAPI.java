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
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedEntry;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedEntryList;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

/**
 * UnpluggedAFK API
 */
public interface UnpluggedAfkAPI
{
	/**
	 * Return if a Players's UUID is marked as Unplugged.
	 * @param uuid The UUID of the Player
	 * @return True or False
	 */
	static boolean isUnplugged(@Nonnull UUID uuid)
	{
		Optional<UnpluggedStatus> opt = getUnpluggedStatus(uuid);
		return opt.map(s -> s
				          .equals(UnpluggedStatus.ACTIVE)).orElse(false);
	}

	/**
	 * Return the {@link UnpluggedStatus} of the Player specified by UUIO
	 * @param uuid The UUID of the Player
	 * @return Optional of {@link UnpluggedStatus}
	 */
	static Optional<UnpluggedStatus> getUnpluggedStatus(@Nonnull UUID uuid)
	{
		Optional<UnpluggedState> opt = getUnpluggedState(uuid);
		return opt.map(UnpluggedState::status);
	}

	/**
	 * Return the {@link UnpluggedState} of the Player specified by UUIO
	 * @param uuid The UUID of the Player; whether they are Unplugged or not.
	 * @return Optional of {@link UnpluggedState}
	 */
	static Optional<UnpluggedState> getUnpluggedState(@Nonnull UUID uuid)
	{
		Optional<UnpluggedEntry> opt = Optional.ofNullable(UnpluggedEntryList.getInstance().get(uuid));
		return opt.map(UnpluggedEntry::toState)
		          .or(() -> Optional.ofNullable(PlayerManager.getInstance().getState(uuid)));
	}
}
