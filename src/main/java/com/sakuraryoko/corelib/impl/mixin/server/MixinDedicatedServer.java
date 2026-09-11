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

package com.sakuraryoko.corelib.impl.mixin.server;

import com.mojang.datafixers.DataFixer;
import com.sakuraryoko.corelib.impl.Reference;
import com.sakuraryoko.corelib.impl.config.ConfigManager;
import com.sakuraryoko.corelib.impl.core.modinit.CoreInit;
import com.sakuraryoko.corelib.impl.events.server.ServerEventsManager;
import com.sakuraryoko.corelib.impl.modinit.ModInitManager;
import com.sakuraryoko.corelib.impl.network.NetworkServiceManager;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void corelib$onDedicatedServer(Thread serverThread, LevelStorageSource.LevelStorageAccess levelStorageSource, PackRepository packRepository,
     WorldStem worldStem, Optional<GameRules> gameRules, DedicatedServerSettings settings, DataFixer fixerUpper,
     Services services, CallbackInfo ci)
    {
        ModInitManager.getInstance().registerModInitHandler(new CoreInit());
        ((ModInitManager) ModInitManager.getInstance()).onModInit();

        if (Reference.EXPERIMENTAL && !NetworkServiceManager.getInstance().isServerStarted())
        {
            NetworkServiceManager.getInstance().onStartServer();
        }
    }


    @Inject(method = "initServer", at = @At("RETURN"))
    private void corelib$onInitServer(CallbackInfoReturnable<Boolean> cir)
    {
        if (cir.getReturnValue())
        {
            ((ModInitManager) ModInitManager.getInstance()).setDedicatedServer(true);
            ((ServerEventsManager) ServerEventsManager.getInstance()).onDedicatedStartedInternal(((DedicatedServer) (Object) this));
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void corelib$onStopServer(CallbackInfo ci)
    {
        ((ServerEventsManager) ServerEventsManager.getInstance()).onDedicatedStoppingInternal(((DedicatedServer) (Object) this));
        ConfigManager.getInstance().saveAllConfigs();
        ((ModInitManager) ModInitManager.getInstance()).reset();

        if (Reference.EXPERIMENTAL && NetworkServiceManager.getInstance().isServerStarted())
        {
            NetworkServiceManager.getInstance().onReset();
        }
    }
}
