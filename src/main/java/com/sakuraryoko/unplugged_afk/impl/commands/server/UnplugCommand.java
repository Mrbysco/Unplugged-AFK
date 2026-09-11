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

package com.sakuraryoko.unplugged_afk.impl.commands.server;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.unplugged_afk.impl.Reference;
import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.commands.PermsWrap;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedServerPlayer;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.ProfileWrap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class UnplugCommand implements IServerCommand
{
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment)
    {
        dispatcher.register(
                literal(this.getName())
                        .requires(PermsWrap.check(this.getNode(), ConfigWrap.cmdOpt().unplugCommandPermissions))
                        .executes(ctx -> this.setUnpluggedAfk(ctx, -1, ""))
                        .then(argument("minutes", IntegerArgumentType.integer(1))
                                      .requires(PermsWrap.check(this.getNode(), ConfigWrap.cmdOpt().unplugCommandPermissions))
                                      .executes(ctx -> this.setUnpluggedAfk(ctx, IntegerArgumentType.getInteger(ctx, "minutes"), ""))
                                      .then(argument("reason", StringArgumentType.greedyString())
                                                    .requires(PermsWrap.check(this.getNode(), ConfigWrap.cmdOpt().unplugCommandPermissions))
                                                    .executes(ctx -> this.setUnpluggedAfk(ctx, IntegerArgumentType.getInteger(ctx, "minutes"), StringArgumentType.getString(ctx, "reason")))
                                      )
                        )
        );
    }

    @Override
    public String getName()
    {
        return "unplug";
    }

    @Override
    public String getModId()
    {
        return Reference.MOD_ID;
    }

    private int setUnpluggedAfk(CommandContext<CommandSourceStack> context, int time, String reason)
    {
        CommandSourceStack src = context.getSource();
        if (src.getPlayer() == null) { return 0; }

        if (!ConfigWrap.mainOpt().unpluggedAfkEnabled)
        {
            String msg = "§c/"+this.getName()+" Command is not enabled§r";
            context.getSource().sendSuccess(() -> InitWrap.text().formatTextSafe(msg), false);
            return 1;
        }

        MinecraftServer server = src.getServer();
        ServerPlayer player = src.getPlayer();
        GameProfile profile = player.getGameProfile();

	    if (server.isSingleplayerOwner(profile))
        {
            String msg = "§cCan't use unplugged as the single player server owner§r";
            context.getSource().sendSuccess(() -> InitWrap.text().formatTextSafe(msg), false);
            return 1;
        }

        if (time < 0)
        {
            time = ConfigWrap.unplugged().defaultUnpluggedTimeout;

            if (time < 0)
            {
                time = 129600;
            }
        }
        if (reason == null || reason.isEmpty())
        {
            reason = ConfigWrap.mess().defaultUnpluggedReason;

            if (reason == null || reason.isEmpty())
            {
                reason = "";
            }
        }

        final int finalTime = time;
        final String finalReason = reason;

        server.execute(() -> UnpluggedServerPlayer.createFromPlayer(server, player, finalTime, finalReason));

        UnpluggedAfk.debugLog("setUnpluggedAfk: player: ['{}'/{}] // T: {}m, R: '{}'", ProfileWrap.name(profile), ProfileWrap.id(profile), time, reason);
        return 1;
    }
}
