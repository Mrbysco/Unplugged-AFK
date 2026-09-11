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

package com.sakuraryoko.unplugged_afk.impl.player.unplugged;

import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.Set;

@ApiStatus.Internal
public class UnpluggedGamePacketListener extends ServerGamePacketListenerImpl
{
	public UnpluggedGamePacketListener(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie)
	{
		super(server, connection, player, cookie);
	}

	@Override
	public void disconnect(@Nonnull Component message)
	{
//		UnpluggedAfk.debugLog("UnpluggedGamePacketListener#disconnect(): message: {}", message.getString());
		UnpluggedServerPlayer sp = (UnpluggedServerPlayer) this.player;
		if (!sp.isValid()) { return; }

		if (message.getContents() instanceof TranslatableContents text &&
			(text.getKey().equals("multiplayer.disconnect.idling") ||
			 text.getKey().equals("multiplayer.disconnect.duplicate_login")))
		{
			sp.kill(message);
		}

		if (!ConfigWrap.unplugged().resetHealthUponDeath)
		{
			sp.kill(message);
		}
	}

	@Override
	public void teleport(double x, double y, double z, float yaw, float pitch, @Nonnull Set<RelativeMovement> relativeSet)
	{
		super.teleport(x, y, z, yaw, pitch, relativeSet);

		if (this.player.level().getPlayerByUUID(this.player.getUUID()) != null)
		{
			this.resetPosition();
			this.player.serverLevel().getChunkSource().move(this.player);
		}
	}
}
