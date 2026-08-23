/*
 * This file is part of the CoreLib project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura Ryoko and contributors
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

package com.sakuraryoko.corelib.impl.network;

import com.sakuraryoko.corelib.api.network.payload.NetworkSide;
import com.sakuraryoko.corelib.api.network.payload.NetworkSidedPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload Manager that store's the {@link NetworkSidedPayload} references
 * that is primarily for Payload registration in later (1.20.6+) versions.
 */
public class PayloadManager implements IPayloadManager
{
	private static final PayloadManager INSTANCE = new PayloadManager();
	public static PayloadManager getInstance() { return INSTANCE; }
	private final List<NetworkSidedPayload<?, ?>> payloads = new ArrayList<>();

	@Override
	public void registerPayload(NetworkSidedPayload<?, ?> sidedPayload)
	{
		this.payloads.add(sidedPayload);
	}

	@ApiStatus.Internal
	@SuppressWarnings("unchecked")
	public <B extends FriendlyByteBuf, P extends CustomPacketPayload> List<CustomPacketPayload.TypeAndCodec<B, P>> onRegisterC2SPayloads()
	{
		 List<CustomPacketPayload.TypeAndCodec<B, P>> list = new ArrayList<>();

		 for (NetworkSidedPayload<?, ?> entry : this.payloads)
		 {
			 if (entry.getSide() == NetworkSide.C2S)
			 {
				 list.add((CustomPacketPayload.TypeAndCodec<B, P>) entry.getPayload().getTypeAndCodec());
			 }
		 }

		 return list;
	}

	@ApiStatus.Internal
	@SuppressWarnings("unchecked")
	public <B extends FriendlyByteBuf, P extends CustomPacketPayload> List<CustomPacketPayload.TypeAndCodec<B, P>> onRegisterS2CPayloads()
	{
		 List<CustomPacketPayload.TypeAndCodec<B, P>> list = new ArrayList<>();

		 for (NetworkSidedPayload<?, ?> entry : this.payloads)
		 {
			 if (entry.getSide() == NetworkSide.S2C)
			 {
				 list.add((CustomPacketPayload.TypeAndCodec<B, P>) entry.getPayload().getTypeAndCodec());
			 }
		 }

		 return list;
	}
}
