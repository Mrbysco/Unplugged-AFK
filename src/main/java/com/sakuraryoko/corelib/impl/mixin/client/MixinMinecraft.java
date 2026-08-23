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

package com.sakuraryoko.corelib.impl.mixin.client;

import com.sakuraryoko.corelib.impl.Reference;
import com.sakuraryoko.corelib.impl.config.ConfigManager;
import com.sakuraryoko.corelib.impl.core.modinit.CoreInit;
import com.sakuraryoko.corelib.impl.events.client.ClientEventsManager;
import com.sakuraryoko.corelib.impl.events.tick.TickManager;
import com.sakuraryoko.corelib.impl.modinit.ModInitManager;
import com.sakuraryoko.corelib.impl.network.NetworkServiceManager;
import com.sakuraryoko.corelib.impl.network.thread.CoreNetworkThreadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft
{
    @Shadow public abstract boolean isLocalServer();
	@Shadow @Nullable public ClientLevel level;
	@Unique ClientLevel lastLevel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void corelib$onGameInit(GameConfig gameConfig, CallbackInfo ci)
    {
        ModInitManager.getInstance().registerModInitHandler(new CoreInit());
        ((ModInitManager) ModInitManager.getInstance()).onModInit();
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void corelib$onWorldJoinPre(ClientLevel clientLevel, CallbackInfo ci)
    {
        if (this.level == null)
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).onJoining(clientLevel);

            if (Reference.EXPERIMENTAL && NetworkServiceManager.getInstance().isClientStarted())
            {
                NetworkServiceManager.getInstance().onStopClient();
            }

            this.lastLevel = null;
        }
        else
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).onDimensionChangePre(clientLevel);
            this.lastLevel = clientLevel;
        }
    }

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void corelib$onWorldJoinPost(ClientLevel clientLevel, CallbackInfo ci)
    {
        if (this.lastLevel != null)
        {
            ConfigManager.getInstance().reloadAllConfigs();
            ((ClientEventsManager) ClientEventsManager.getInstance()).onDimensionChangePost(clientLevel);
        }
        else
        {
            if (this.isLocalServer())
            {
                ((ClientEventsManager) ClientEventsManager.getInstance()).onOpenConnection(clientLevel);
            }

            ((ClientEventsManager) ClientEventsManager.getInstance()).onJoined(clientLevel);
        }

        this.lastLevel = clientLevel;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
    private void corelib$onDisconnectPre(Screen screen, boolean transferring, CallbackInfo ci)
    {
        if (this.lastLevel == null)
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).worldChangePre(this.level);
        }
        else
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).onDisconnecting(this.level);

            if (Reference.EXPERIMENTAL && NetworkServiceManager.getInstance().isClientStarted())
            {
                NetworkServiceManager.getInstance().onStopClient();
            }
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("RETURN"))
    private void corelib$onDisconnectPost(Screen screen, boolean transferring, CallbackInfo ci)
    {
        if (this.lastLevel != null)
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).onDisconnected(this.lastLevel);
            //ConfigManager.getInstance().saveAllConfigs();

            if (Reference.EXPERIMENTAL && NetworkServiceManager.getInstance().isClientStarted())
            {
                NetworkServiceManager.getInstance().onStopClient();
            }

            this.lastLevel = null;
        }
        else
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).worldChangePost(null);
        }
    }

    @Inject(method = "clearDownloadedResourcePacks", at = @At("HEAD"))
    private void corelib$onDisconnected(CallbackInfo ci)
    {
        if (this.isLocalServer())
        {
            ((ClientEventsManager) ClientEventsManager.getInstance()).onCloseConnection(null);
        }

        ConfigManager.getInstance().saveAllConfigs();
        ((ModInitManager) ModInitManager.getInstance()).reset();

        if (Reference.EXPERIMENTAL)
        {
            NetworkServiceManager.getInstance().onReset();
        }
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void corelib$onTick(CallbackInfo ci)
    {
        TickManager.getInstance().onClientTick((Minecraft) (Object) this);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void corelib$onRunStop(CallbackInfo ci)
    {
        CoreNetworkThreadHandler.getInstance().endAll();
    }
}
