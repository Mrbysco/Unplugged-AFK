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

package com.sakuraryoko.corelib.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.corelib.api.log.AnsiLogger;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.impl.core.modinit.CoreInit;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class TestCommand implements IServerCommand
{
    private final AnsiLogger LOGGER = new AnsiLogger(this.getClass(), TestReference.DEBUG);

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment)
    {
        dispatcher.register(
                literal(this.getName())
                        //.requires((commandSourceStack) -> commandSourceStack.hasPermission(0))
                        .executes(ctx -> this.about(ctx.getSource(), ctx))
        );
    }

    @Override
    public String getName()
    {
        return this.getModId();
    }

    @Override
    public String getModId()
    {
        return TestReference.MOD_ID;
    }

    private int about(CommandSourceStack src, CommandContext<CommandSourceStack> ctx)
    {
        List<Component> info = CoreInit.getInstance().getVanillaFormatted(ModInitData.ALL_INFO);
        String user = src.getTextName();

        for (Component entry : info)
        {
	        ctx.getSource().sendSuccess(() -> entry, false);
        }

        this.LOGGER.debug("{} has executed /{} .", user, this.getName());
        return 1;
    }
}
