/*
 * This file is part of the CoreLib project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Sakura Ryoko and contributors
 *
 * CoreLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CoreLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CoreLib.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.corelib.impl.events.client;

import com.sakuraryoko.corelib.api.events.IClientEventsDispatch;
import com.sakuraryoko.corelib.impl.CoreLib;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientEventsManager implements IClientEventsManager
{
    private static final ClientEventsManager INSTANCE = new ClientEventsManager();
    private final List<IClientEventsDispatch> DISPATCH = new ArrayList<>();
    public static IClientEventsManager getInstance() { return INSTANCE; }

    @Override
    public void registerClientEvents(IClientEventsDispatch handler) throws RuntimeException
    {
        if (!this.DISPATCH.contains(handler))
        {
            this.DISPATCH.add(handler);
        }
        else
        {
            throw new RuntimeException("Client Events dispatcher has already been registered!");
        }
    }

    @ApiStatus.Internal
    public void worldChangePre(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("worldChangePre()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.worldChangePre(world));
        }
    }

    @ApiStatus.Internal
    public void worldChangePost(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("worldChangePost()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.worldChangePost(world));
        }
    }

    @ApiStatus.Internal
    public void onJoining(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onJoining()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onJoining(world));
        }
    }

    @ApiStatus.Internal
    public void onOpenConnection(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onOpenConnection()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onOpenConnection(world));
        }
    }

    @ApiStatus.Internal
    public void onJoined(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onJoined()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onJoined(world));
        }
    }

    @ApiStatus.Internal
    public void onGameJoinPre(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onGameJoinPre()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onGameJoinPre(world));
        }
    }

    @ApiStatus.Internal
    public void onGameJoinPost(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onGameJoinPost()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onGameJoinPost(world));
        }
    }

    @ApiStatus.Internal
    public void onDimensionChangePre(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onDimensionChangePre()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onDimensionChangePre(world));
        }
    }

    @ApiStatus.Internal
    public void onDimensionChangePost(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onDimensionChangePost()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onDimensionChangePost(world));
        }
    }

    @ApiStatus.Internal
    public void onDisconnecting(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onDisconnecting()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onDisconnecting(world));
        }
    }

    @ApiStatus.Internal
    public void onCloseConnection(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onCloseConnection()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onCloseConnection(world));
        }
    }

    @ApiStatus.Internal
    public void onDisconnected(@Nullable ClientLevel world)
    {
        CoreLib.debugLog("onDisconnected()");

        if (!this.DISPATCH.isEmpty())
        {
            this.DISPATCH.forEach((handler) -> handler.onDisconnected(world));
        }
    }
}
