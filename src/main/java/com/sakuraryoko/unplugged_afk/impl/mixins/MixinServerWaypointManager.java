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

package com.sakuraryoko.unplugged_afk.impl.mixins;

import com.sakuraryoko.unplugged_afk.impl.player.interfaces.IWaypointManagerInvoker;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedPlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWaypointManager.class)
public abstract class MixinServerWaypointManager implements IWaypointManagerInvoker
{
	@Shadow
	public abstract void removePlayer(ServerPlayer player);

	@Shadow
	public abstract void addPlayer(ServerPlayer player);

	@Inject(method = "addPlayer", at = @At("HEAD"))
	private void unplugged$onAddPlayerWaypoint(ServerPlayer player, CallbackInfo ci)
	{
		UnpluggedPlayerUtils.onAddOrUpdateWaypoint((ServerWaypointManager) (Object) this, player);
	}

	@Inject(method = "updatePlayer", at = @At("HEAD"))
	private void unplugged$onUpdatePlayerWaypoint(ServerPlayer player, CallbackInfo ci)
	{
		UnpluggedPlayerUtils.onAddOrUpdateWaypoint((ServerWaypointManager) (Object) this, player);
	}

	@Override
	public void unplugged$addPlayer(ServerPlayer player)
	{
		this.addPlayer(player);
	}

	@Override
	public void unplugged$removePlayer(ServerPlayer player)
	{
		this.removePlayer(player);
	}
}
