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
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedGamePacketListener;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedPlayerUtils;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
@ApiStatus.Internal
public abstract class MixinPlayerList_core
{
	@Shadow @Final private MinecraftServer server;
	@Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;

	@Inject(method = "load", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void unplugged$onLoad(ServerPlayer player, CallbackInfoReturnable<CompoundTag> cir)
	{
		if (player instanceof UnpluggedServerPlayer sp)
		{
			sp.startingPosition.run();
		}
	}

	@Inject(method = "remove", at = @At("HEAD"), cancellable = true)
	private void unplugged$suppressDuplicateRemove(ServerPlayer player, CallbackInfo ci)
	{
		ServerPlayer playerInList = this.playersByUUID.get(player.getUUID());

		if (player != playerInList)
		{
			ci.cancel();
		}
	}

	@WrapOperation(method = "placeNewPlayer",
	          at = @At(value = "NEW",
	                        target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"
	               )
	)
	private ServerGamePacketListenerImpl unplugged$spawnShadowPlayer(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie, Operation<ServerGamePacketListenerImpl> original)
	{
		if (player instanceof UnpluggedServerPlayer shadow)
		{
			return new UnpluggedGamePacketListener(this.server, connection, shadow, cookie);
		}

		return original.call(server, connection, player, cookie);
	}

	@WrapOperation(method = "respawn",
	               at = @At(value = "NEW",
	                        target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
	               )
	)
	private ServerPlayer unplugged$respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile,
													ClientInformation ci,
													Operation<ServerPlayer> original,
													@Local(argsOnly = true) ServerPlayer player,
													@Local(argsOnly = true) boolean keepEverything)
	{
		if (player instanceof UnpluggedServerPlayer sp && !sp.isValid())
		{
			UnpluggedServerPlayer newSp = UnpluggedServerPlayer.respawnUnplugged(server, level, profile, ci);
			UnpluggedPlayerUtils.respawnUnpluggedAfk(profile, sp, newSp);
			newSp.restoreFrom(player, keepEverything);
			return newSp;
		}

		return original.call(server, level, profile, ci);
	}
}
