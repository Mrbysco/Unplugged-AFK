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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
@ApiStatus.Internal
public abstract class MixinServerPlayer_fakeMovement extends Player
{
	public MixinServerPlayer_fakeMovement(MinecraftServer server, Level level, GameProfile gameProfile, ClientInformation ci)
	{
		super(level, gameProfile);
	}

	@ModifyExpressionValue(method = {"getKnownMovement", "getKnownSpeed"},
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/server/level/ServerPlayer;lastKnownClientMovement:Lnet/minecraft/world/phys/Vec3;",
			opcode = Opcodes.GETFIELD),
		require = 2)
	private Vec3 unplugged$bypassClientMovementInfo(Vec3 original)
	{
		return (((Player) this) instanceof UnpluggedServerPlayer) ? super.getKnownMovement() : original;
	}
}
