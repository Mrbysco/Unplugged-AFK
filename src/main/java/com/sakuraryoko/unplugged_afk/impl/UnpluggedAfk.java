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

package com.sakuraryoko.unplugged_afk.impl;

import com.mojang.logging.LogUtils;
import com.sakuraryoko.corelib.impl.modinit.ModInitManager;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.UnpluggedInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Reference.MOD_ID)
public class UnpluggedAfk {
	public static final Logger LOGGER = LogUtils.getLogger();

	public static void debugLog(String key, Object... args) {
		if (InitWrap.debug()) {
			LOGGER.info(String.format("[DEBUG] %s", key), args);
		}
	}

	public UnpluggedAfk(IEventBus eventBus, Dist dist, ModContainer container) {
		ModInitManager.getInstance().registerModInitHandler(UnpluggedInit.getInstance());
	}
}
