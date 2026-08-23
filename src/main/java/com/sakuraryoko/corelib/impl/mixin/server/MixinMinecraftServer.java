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

import com.sakuraryoko.corelib.impl.Reference;
import com.sakuraryoko.corelib.impl.events.server.ServerEventsManager;
import com.sakuraryoko.corelib.impl.events.tick.TickManager;
import com.sakuraryoko.corelib.impl.network.NetworkServiceManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
    @Inject(method = "runServer",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"))
    private void corelib$onServerStarting(CallbackInfo ci)
    {
        ((ServerEventsManager) ServerEventsManager.getInstance()).onStartingInternal((MinecraftServer) (Object) this);

        if (Reference.EXPERIMENTAL && !NetworkServiceManager.getInstance().isServerStarted())
        {
            NetworkServiceManager.getInstance().onStartServer();
        }
    }

    @Inject(method = "runServer",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;",
                     ordinal = 0))
    private void corelib$onServerStarted(CallbackInfo ci)
    {
        ((ServerEventsManager) ServerEventsManager.getInstance()).onStartedInternal((MinecraftServer) (Object) this);

        if (Reference.EXPERIMENTAL && !NetworkServiceManager.getInstance().isServerStarted())
        {
            NetworkServiceManager.getInstance().onStartServer();
        }
    }

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void corelib$onReloadResources(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir)
    {
        ((ServerEventsManager) ServerEventsManager.getInstance()).onReloadCompleteInternal((MinecraftServer) (Object) this, collection);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void corelib$onStoppingServer(CallbackInfo ci)
    {
        ((ServerEventsManager) ServerEventsManager.getInstance()).onStoppingInternal((MinecraftServer) (Object) this);

        if (Reference.EXPERIMENTAL && NetworkServiceManager.getInstance().isServerStarted())
        {
            NetworkServiceManager.getInstance().onStopServer();
        }
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void corelib$onStoppedServer(CallbackInfo ci)
    {
        ((ServerEventsManager) ServerEventsManager.getInstance()).onStoppedInternal((MinecraftServer) (Object) this);

        if (Reference.EXPERIMENTAL && NetworkServiceManager.getInstance().isServerStarted())
        {
            NetworkServiceManager.getInstance().onReset();
        }
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void corelib$onTickServer(BooleanSupplier booleanSupplier, CallbackInfo ci)
    {
        TickManager.getInstance().onServerTick((MinecraftServer) (Object) this);
    }
}
