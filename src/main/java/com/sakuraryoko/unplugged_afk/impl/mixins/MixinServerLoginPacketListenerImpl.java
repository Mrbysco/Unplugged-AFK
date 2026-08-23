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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedPlayerUtils;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.ProfileWrap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.net.SocketAddress;

@Mixin(ServerLoginPacketListenerImpl.class)
@ApiStatus.Internal
public abstract class MixinServerLoginPacketListenerImpl
{
	@WrapOperation(method = "verifyLoginAndFinishConnectionSetup",
						at = @At(value = "INVOKE",
									target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"))
	private Component unplugged$checkForStaleShadow(PlayerList instance, SocketAddress socketAddress,
														NameAndId nameAndId,
														Operation<Component> original)
	{
		ServerPlayer player = instance.getPlayer(nameAndId.id());
		GameProfile profile;
		profile = ProfileWrap.profile(nameAndId);

		UnpluggedPlayerUtils.checkForUnpluggedAtPreLogin(instance, profile, player);

		return original.call(instance, socketAddress, nameAndId);
	}
}
