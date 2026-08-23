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

package com.sakuraryoko.corelib.api.events;

import net.minecraft.client.multiplayer.ClientLevel;

import javax.annotation.Nullable;

public interface IClientEventsDispatch
{
 void worldChangePre(@Nullable ClientLevel world);

 void worldChangePost(@Nullable ClientLevel world);

 void onJoining(@Nullable ClientLevel world);

 void onOpenConnection(@Nullable ClientLevel world);

 void onJoined(@Nullable ClientLevel world);

 void onGameJoinPre(@Nullable ClientLevel lastWorld);

 void onGameJoinPost(@Nullable ClientLevel newWorld);

 void onDimensionChangePre(@Nullable ClientLevel world);

 void onDimensionChangePost(@Nullable ClientLevel world);

 void onDisconnecting(@Nullable ClientLevel world);

 void onCloseConnection(@Nullable ClientLevel world);

 void onDisconnected(@Nullable ClientLevel world);
}
