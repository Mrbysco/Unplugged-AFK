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

package com.sakuraryoko.unplugged_afk.impl.commands;

import com.google.common.base.Predicates;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * (Lucko) Fabric Permissions API support only begins with MC 1.16.4+
 */
@ApiStatus.Internal
public class PermsWrap
{
	public static Predicate<CommandSourceStack> check(@Nonnull String node, int level)
	{
		return Commands.hasPermission(new PermissionCheck.Require(new Permission.HasCommandLevel(permissionFromInt(level))));
////#if MC >= 1.16.5
////$$		return Permissions.require(node, permissionFromInt(level));
////#else
//		return (src -> src.hasPermission(permissionFromInt(level)));
////#endif
	}

	public static Predicate<CommandSourceStack> checkAdv(@Nonnull String node, int level)
	{
		if (!ConfigWrap.mainOpt().advancedAdminOptions)
		{
			return Predicates.alwaysFalse();
		}

////#if MC >= 1.16.5
////$$		return Permissions.require(node, permissionFromInt(level));
////#else
//		return (src -> src.hasPermission(permissionFromInt(level)));
////#endif
		return Commands.hasPermission(new PermissionCheck.Require(new Permission.HasCommandLevel(permissionFromInt(level))));
	}

//	public static boolean check(@Nonnull Entity entity, @Nonnull String node, int level)
//	{
////#if MC >= 1.16.5
////$$		return Permissions.check(entity, node, permissionFromInt(level));
////#else
//		return entity.hasPermissions(permissionFromInt(level));
////#endif
//	}

  	public static PermissionLevel permissionFromInt(int level)
  	{
  		return PermissionLevel.byId(Mth.clamp(level, 0, PermissionLevel.OWNERS.id()));
  	}
}
