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

import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
@ApiStatus.Internal
public abstract class MixinEntity
{
	@Shadow public abstract @Nullable LivingEntity getControllingPassenger();
	@Shadow private Level level;

	@Inject(method = "isControlledByLocalInstance", at = @At("HEAD"), cancellable = true)
	private void unplugged$isControlledByLocalInstance(CallbackInfoReturnable<Boolean> cir)
	{
		if ((Object) this instanceof UnpluggedServerPlayer ||
		    this.getControllingPassenger() instanceof UnpluggedServerPlayer)
		{
			cir.setReturnValue(!this.level.isClientSide());
		}
	}
}
