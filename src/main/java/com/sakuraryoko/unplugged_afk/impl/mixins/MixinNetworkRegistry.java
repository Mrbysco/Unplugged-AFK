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

import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedConnection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkRegistry.class)
@ApiStatus.Internal
public class MixinNetworkRegistry
{
	@Inject(method = "hasChannel(Lnet/minecraft/network/Connection;Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/resources/ResourceLocation;)Z",
			at = @At("HEAD"), cancellable = true, remap = false)
	private static void unplugged$ignoreChannels(net.minecraft.network.Connection connection, ConnectionProtocol protocol, ResourceLocation payloadId, CallbackInfoReturnable<Boolean> cir)
	{
		if (connection instanceof UnpluggedConnection)
		{
			cir.setReturnValue(true);
		}
	}
}
