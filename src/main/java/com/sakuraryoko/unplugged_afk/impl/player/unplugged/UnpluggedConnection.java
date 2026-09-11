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

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import javax.annotation.Nonnull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@ApiStatus.Internal
public class UnpluggedConnection extends Connection
{
	protected static final SocketAddress address = new InetSocketAddress("127.0.0.1", 65535);

	public UnpluggedConnection(PacketFlow receiving)
	{
		super(receiving);
		((IUnpluggedConnection) this).setChannel(new EmbeddedChannel());
	}

	@Override
	public void send(@Nonnull Packet<?> packet, @Nullable PacketSendListener sendListener)
	{
	}

	@Override
	public void setReadOnly()
	{
	}

	@Override
	public void handleDisconnection()
	{
	}

	@Override
	public void setListenerForServerboundHandshake(@Nonnull PacketListener packetListener)
	{
	}

	@Override
	public <T extends PacketListener> void setupInboundProtocol(@Nonnull ProtocolInfo<T> protocolInfo, @Nonnull T packetListener)
	{
	}

	@Override
	public void tick()
	{
		// NO-OP
	}

	@Override
	public @Nonnull SocketAddress getRemoteAddress()
	{
		return address;
	}
}
