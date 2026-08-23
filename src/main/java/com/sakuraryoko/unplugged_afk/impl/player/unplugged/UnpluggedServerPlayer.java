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

package com.sakuraryoko.unplugged_afk.impl.player.unplugged;

import com.mojang.authlib.GameProfile;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;
import com.sakuraryoko.unplugged_afk.api.state.GameState;
import com.sakuraryoko.unplugged_afk.api.state.PosState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;
import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.unplugged_afk.impl.events.PlayerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.events.ServerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.GameWrap;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.PosWrap;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.ProfileWrap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Internal
@SuppressWarnings("EntityConstructor")
public class UnpluggedServerPlayer extends ServerPlayer
{
	public Runnable startingPosition = () -> {};
	private boolean freshPlayer;
	private long freshHoldTime;
	private int time = 0;
	private String reason;
	private long startTime = -1L;
	private long timeout = -1L;
	private long lastTick = -1L;
	private boolean isValid = false;
	private boolean expired = false;

	public UnpluggedServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation ci)
	{
		super(server, level, profile, ci);
	}

	private static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, final UUID uuid)
	{
		final ResolvableProfile resolver = ResolvableProfile.createUnresolved(uuid);
		return resolver.resolveProfile(server.services().profileResolver());
	}

	public static void createFromConfig(MinecraftServer server, PlayerOptions opts)
	{
		UUID uuid = opts.uuid;
		String name = opts.name;
		UnpluggedState state = opts.state;
		PosState pos = opts.pos;
		GameState game = opts.game;
		Identifier id = Identifier.tryParse(pos.location());
		AtomicReference<ResourceKey<Level>> ref = new AtomicReference<>(Level.OVERWORLD);

		if (id != null)
		{
			server.levelKeys().forEach(levelKey ->
			                           {
										   if (levelKey.identifier().equals(id))
										   {
											   ref.set(levelKey);
										   }
			                           });
		}

		ServerLevel level = server.getLevel(ref.get());
		server.services().nameToIdCache().resolveOfflineUsers(false);
		GameProfile profile;

		UUID tempUUID = OldUsersConverter.convertMobOwnerIfNecessary(server, name);
		if (tempUUID != null && !tempUUID.equals(uuid))
		{
			uuid = tempUUID;
			opts.uuid = uuid;
		}
		if (uuid == null)
		{
			uuid = UUIDUtil.createOfflinePlayerUUID(name);
		}
		server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
		profile = new GameProfile(uuid, name);

		if (server.getPlayerList().getBans().isBanned(new NameAndId(profile)))
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				UnpluggedAfk.LOGGER.warn("createFromConfig: Blocking banned player: ['{}'/{}]", name, uuid.toString());
			}

			PlayerManager.getInstance().remove(uuid, true, UnpluggedStatus.TERMINATED);
			return;
		}

		if (!UnpluggedPlayerUtils.ensureSafeForUUID(server, uuid))
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				UnpluggedAfk.LOGGER.error("createFromConfig: Blocking player: ['{}'/{}] -- Perhaps a duplicate UUID?", name, uuid.toString());
			}

			PlayerManager.getInstance().remove(uuid, true, UnpluggedStatus.TERMINATED);
			return;
		}

		if (pos.x() == 0 && pos.y() == 0 && pos.z() == 0)
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				UnpluggedAfk.LOGGER.error("createFromConfig: Blocking player: ['{}'/{}] -- We don't want someone suffocating in a wall", name, uuid.toString());
			}

			PlayerManager.getInstance().resetState(profile);
			return;
		}

		server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
		fetchGameProfile(server, profile.id()).whenCompleteAsync((p, throwable) ->
		{
			if (throwable != null) { return; }
			GameProfile temp;
			if (p.name().isEmpty())
			{
				temp = profile;
			}
			else
			{
				temp = p;
			}
			final GameProfile finalProfile = temp;
			server.execute(() -> createFromConfigPhase2(server, level, finalProfile, state, pos, game));
		});
	}

	private static UnpluggedServerPlayer createFromConfigPhase2(MinecraftServer server, ServerLevel level, GameProfile profile,
	                                                            UnpluggedState state, PosState pos, GameState game)
	{
		GameType gameType = GameType.byName(game.gameMode(), GameType.DEFAULT_MODE);
		PlayerList pl = server.getPlayerList();
		final String name = ProfileWrap.name(profile);

		UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, ClientInformation.createDefault());

		if (!pos.isEmpty())
		{
			shadow.startingPosition = () -> onStartingPositonWrap(shadow, pos);
		}

		if (ConfigWrap.mess().hideUnpluggedJoin)
		{
			PlayerEventsHandler.getInstance().addShouldHideJoin(name);
		}

		pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault(), true));

		UnpluggedPlayerUtils.loadPlayerNbt(shadow);

		shadow.setHealth(20.0f);
		if (!pos.matches(shadow) && !pos.isEmpty())
		{
			shadow.connection.teleport(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		}
		shadow.gameMode.changeGameModeForPlayer(gameType);
		shadow.unsetRemoved();
		shadow.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
		shadow.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);

		if (gameType.isSurvival())
		{
			// Survival players shouldn't be able to fly, or be invulnerable.
			shadow.getAbilities().flying = false;
			shadow.setInvulnerable(false);
		}
		else
		{
			shadow.getAbilities().flying = game.flying();
		}

		shadow.time = state.time();
		shadow.timeout = state.timeout();
		shadow.reason = state.reason();
		shadow.freshPlayer = true;
		shadow.freshHoldTime = System.currentTimeMillis();
		shadow.startTime = state.startTime() <= 0 ? shadow.freshHoldTime : state.startTime();

		if (shadow.getStartTime() != state.startTime())
		{
			state = new UnpluggedState(state.status(), state.time(), state.timeout(), shadow.getStartTime(), state.reason());
		}

		PlayerManager.getInstance().setState(profile, state);
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().add(shadow, state);

		if (entry != null)
		{
			entry.handler().registerUnpluggedAfk(shadow, state);
			entry.setPlayer(shadow);
		}

		UnpluggedAfk.debugLog("createFromConfigPhase2: player: ['{}'/{}], state: [{}]", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		shadow.isValid = true;

		return shadow;
	}

	public static void createFromPlayer(MinecraftServer server, ServerPlayer player, int time, String reason)
	{
		if (time <= 0)
		{
			time = 129600;      // Hard coded in case of stupid
		}

		final long timeout = (time * 60L) * 1000L;
		final String name = player.getName().getString();
		final String duration = ConfigWrap.mess().duration.option.format(timeout);
		final String kickStr = ConfigWrap.mess().unpluggedKickMessage
				+ (ConfigWrap.mess().displayDuration
				   ? ConfigWrap.mess().whenUnpluggedDurationPrefix + duration
				   : "");
		Component kickMsg = InitWrap.text().formatText(kickStr);

		final GameProfile profile = player.getGameProfile();
		final PosState pos = PosWrap.of(player);
		final GameState game = GameWrap.of(player);
		final int finalTime = time;
		final float health = player.getHealth();
		final boolean isFlying = player.getAbilities().flying;

		if (kickMsg == null || kickMsg.toString().isEmpty())
		{
			kickMsg = Component.translatable("multiplayer.disconnect.duplicate_login");
		}

		if (ConfigWrap.mess().hideUnpluggedJoin)
		{
			PlayerEventsHandler.getInstance().addShouldHideJoin(name);
		}

		server.getPlayerList().remove(player);
		player.connection.disconnect(kickMsg);

		server.execute(() -> createFromPlayerPhase2(server, profile, finalTime, timeout, reason, pos, game, health, isFlying));
	}

	public static void createFromPlayerPhase2(MinecraftServer server, GameProfile profile,
	                                          int time, final long timeout, String reason,
	                                          PosState pos, GameState game,
	                                          float health, boolean isFlying)
	{
		GameType gameType = GameType.byName(game.gameMode(), GameType.DEFAULT_MODE);
		final UUID uuid = ProfileWrap.id(profile);
		Identifier id = Identifier.tryParse(pos.location());
		AtomicReference<ResourceKey<Level>> ref = new AtomicReference<>(Level.OVERWORLD);

		if (id != null)
		{
			server.levelKeys().forEach(levelKey ->
			                           {
				                           if (levelKey.identifier().equals(id))
				                           {
					                           ref.set(levelKey);
				                           }
			                           });
		}

		PlayerList pl = server.getPlayerList();
		ServerLevel level = server.getLevel(ref.get());

		UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, ClientInformation.createDefault());

		if (!UnpluggedPlayerUtils.ensureSafeForUUID(server, uuid))
		{
			return;
		}

		if (!pos.isEmpty())
		{
			shadow.startingPosition = () -> onStartingPositonWrap(shadow, pos);
		}

		pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault(), true));

		UnpluggedPlayerUtils.loadPlayerNbt(shadow);

		shadow.setHealth(health);
		if (!pos.matches(shadow) && !pos.isEmpty())
		{
			shadow.connection.teleport(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		}
		shadow.gameMode.changeGameModeForPlayer(gameType);
		shadow.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
		shadow.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);

		if (shadow.gameMode.isSurvival())
		{
			// Survival players shouldn't be able to fly, or be invulnerable.
			shadow.getAbilities().flying = false;
			shadow.setInvulnerable(false);
		}
		else
		{
			shadow.getAbilities().flying = isFlying;
		}

		shadow.time = time;
		shadow.timeout = timeout;
		shadow.reason = reason;
		shadow.freshPlayer = true;
		shadow.freshHoldTime = System.currentTimeMillis();
		shadow.startTime = shadow.freshHoldTime;

		UnpluggedState state = new UnpluggedState(UnpluggedStatus.ACTIVE, time, shadow.timeout, shadow.startTime, reason);
		PlayerManager.getInstance().setState(profile, state);
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().add(shadow, state);

		if (entry != null)
		{
			entry.handler().registerUnpluggedAfk(shadow, state);
			entry.setPlayer(shadow);
		}

		UnpluggedAfk.debugLog("createFromPlayer: player: ['{}'/{}], state: [{}]", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		shadow.isValid = true;
	}

	public static UnpluggedServerPlayer respawnUnplugged(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation ci)
	{
		UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, ci);
		UnpluggedAfk.debugLog("respawnUnplugged: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		shadow.isValid = true;
		return shadow;
	}

	private static void onStartingPositonWrap(UnpluggedServerPlayer sp, PosState pos)
	{
		if (!pos.matches(sp) && !pos.isEmpty())
		{
			sp.startingPosition = () -> sp.snapTo(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		}
	}

	private void createUnpluggedPost(MinecraftServer server)
	{
		PlayerList pl = server.getPlayerList();
		GameProfile profile = this.getGameProfile();

		UnpluggedAfk.debugLog("createUnpluggedPost: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		pl.broadcastAll(new ClientboundRotateHeadPacket(this, (byte) (this.yHeadRot * 256 / 360)),
				this.level().dimension());
		pl.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this));
//		pl.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this));
//		pl.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, this));


		PlayerEventsHandler.getInstance().removeShouldHideJoin(this.getName().getString());
		UnpluggedPlayerUtils.sendHidePlayerPacket(server, this);
	}

	public boolean isValid()
	{
		return this.isValid;
	}

	public int getTimer()
	{
		return this.time;
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public String getReason()
	{
		return this.reason;
	}

	public long getStartTime()
	{
		return this.startTime;
	}

	public void updateTimeOut(long timeout)
	{
		this.timeout = timeout;
	}

	public UnpluggedState toState()
	{
		return new UnpluggedState(this.isValid() ? UnpluggedStatus.ACTIVE : UnpluggedStatus.INTERRUPTED, this.getTimer(), this.getTimeout(), this.getStartTime(), this.getReason());
	}

	protected boolean fromState(UnpluggedState state)
	{
		boolean dirty = false;

		if (this.isValid())
		{
			if (this.getTimer() != state.time() &&
				state.time() > 0)
			{
				this.time = state.time();
				dirty = true;
			}
			if (this.getTimeout() != state.timeout() &&
				state.timeout() > 0 &&
				state.timeout() < this.getTimeout())
			{
				this.timeout = state.timeout();
				dirty = true;
			}
			if (this.getStartTime() != state.startTime() &&
				state.startTime() > 0 &&
				state.startTime() < this.getStartTime())
			{
				this.startTime = state.startTime();
				dirty = true;
			}
			if (!Objects.equals(this.getReason(), state.reason()) &&
				!state.reason().isEmpty())
			{
				this.reason = state.reason();
				dirty = true;
			}

//			UnpluggedAfk.debugLog("fromState: dirty: [{}]", dirty);
		}

		return dirty;
	}

	@Override
	public void onEquipItem(final @NonNull EquipmentSlot slot, final @NonNull ItemStack previous, final @NonNull ItemStack stack)
	{
		if (!this.isUsingItem())
		{
			super.onEquipItem(slot, previous, stack);
		}
	}

	@Override
	public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource damageSource, float amount)
	{
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(this);

		if (entry != null &&
			entry.status() == UnpluggedStatus.ACTIVE &&
			ConfigWrap.unplugged().unpluggedDisableDamage)
		{
			// If you want to be able to kill shadow bots;
			// then just disable this config.
			return false;
		}

		return super.hurtServer(level, damageSource, amount);
	}

	@Override
	public void kill(@NonNull ServerLevel level)
	{
		this.kill(BuiltinTextHandler.getInstance().formatTextSafe("Killed"));
	}

	public void kill(Component message)
	{
		this.dismount();
		this.killShadow(message);
		if (message.getContents() instanceof TranslatableContents text && text.getKey().equals("multiplayer.disconnect.duplicate_login"))
		{
			this.connection.onDisconnect(new DisconnectionDetails(message));
		}
		else
		{
			this.level().getServer().schedule(
				new TickTask(this.level().getServer().getTickCount(),
						() -> this.connection.onDisconnect(new DisconnectionDetails(message))
			));
		}
	}

	@Override
	public void tick()
	{
		MinecraftServer server = this.level().getServer();

		if (server.getTickCount() % 10 == 0)
		{
			if (this.freshPlayer)
			{
				final long now = System.currentTimeMillis();

				// Delay sending the ADD_PLAYER packets;
				// ... because Mojang.
				if ((now - this.freshHoldTime) >= 200L)
				{
					this.createUnpluggedPost(server);
					this.freshPlayer = false;
				}
			}

			// Remove invalid Shadows that are still ticking (Why?)
			if (!this.freshPlayer && !this.isValid())
			{
				final Component name = this.getName();
				final Component reason = InitWrap.text().formatTextSafe("Invalid");

				this.kill(reason);
//										   ((IMixinPlayerList) server.getPlayerList()).unplugged$save(this);
				server.getPlayerList().remove(this);

				if (!ConfigWrap.mess().hideUnpluggedJoin)
				{
					UnpluggedPlayerUtils.sendLeaveMessage(server, name);
				}
			}

			this.tickUnplugged(server);
			this.connection.resetPosition();
			this.level().getChunkSource().move(this);
//			this.hasChangedDimension();
		}

		try
		{
			super.tick();
			this.doTick();
		}
		catch (NullPointerException ignored) {}
	}

	private void tickUnplugged(MinecraftServer server)
	{
		final long now = System.currentTimeMillis();

		if (this.lastTick < 0L)
		{
			this.lastTick = now;
		}

		final long tickDelta = now - this.lastTick;
		this.lastTick = now;

		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(this);

		if (entry != null)
		{
			if (entry.status() != UnpluggedStatus.ACTIVE)
			{
				UnpluggedState state = PlayerManager.getInstance().getState(this.getGameProfile());
				entry.updateState(state);
				entry.setTimeout(this.timeout);
			}
			else
			{
				this.timeout = entry.timeout();
			}

			PosState pos = PlayerManager.getInstance().getPos(this.uuid);
			Vec3 currentPos = this.position();

			if (currentPos.x() != pos.x() || currentPos.y() != pos.y() || currentPos.z() != pos.z())
			{
				PlayerManager.getInstance().updatePlayerData(this);
			}

			if (!entry.tickTimeout(tickDelta))
			{
				PlayerList pl = server.getPlayerList();
				String mess = ConfigWrap.mess().unpluggedExpiredReason;

				if (mess == null || mess.isEmpty())
				{
					mess = "§eTimeout Expired§r";
				}

				final long delta = UnpluggedPlayerUtils.getStartTimeDelta(this.getStartTime());
				final String name = this.getName().getString();

				this.reason = (ConfigWrap.mess().displayDuration
				               ? ConfigWrap.mess().unpluggedSuccessfulPrefix
				                 + ConfigWrap.mess().duration.option.format(delta)
				                 + ConfigWrap.mess().unpluggedSuccessfulSuffix
				               : ConfigWrap.mess().unpluggedSuccessful)
						+ ConfigWrap.mess().unpluggedSuccessfulPunctuation
						+ mess;

				UnpluggedState newState = new UnpluggedState(UnpluggedStatus.EXPIRED, -1, -1L, -1L, this.getReason());
				entry.updateState(newState);
				UnpluggedEntryList.getInstance().syncEntry(this, entry);
				PlayerManager.getInstance().setState(this.getGameProfile(), newState);
				UnpluggedEntryList.getInstance().remove(this, false, UnpluggedStatus.EXPIRED);
				this.expired = true;

				if (ConfigWrap.mess().hideUnpluggedJoin)
				{
					PlayerEventsHandler.getInstance().addShouldHideJoin(name);
				}

				Component reason = InitWrap.text().formatTextSafe(mess);
				this.kill(reason);
//										   ((IMixinPlayerList) server.getPlayerList()).unplugged$save(this);
				server.getPlayerList().remove(this);

				if (ConfigWrap.mess().hideUnpluggedJoin)
				{
					PlayerEventsHandler.getInstance().removeShouldHideJoin(name);
				}
				else
				{
					UnpluggedPlayerUtils.sendLeaveMessage(server, this.getName());
				}
			}
		}
	}

	@Override
	public void die(@NonNull DamageSource damageSource)
	{
		this.dismount();
		super.die(damageSource);

		if (ConfigWrap.unplugged().resetHealthUponDeath)
		{
			this.setHealth(20.0F);
			this.foodData = new FoodData();
		}
		else
		{
			this.killShadow(InitWrap.text().formatTextSafe("Player has Died"));
		}

		this.kill(this.getCombatTracker().getDeathMessage());
	}

	public void killShadow(Component message)
	{
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(this);
		PlayerManager.getInstance().updatePlayerData(this);

		if (message.getString().equals(ConfigWrap.mess().unpluggedReplaced) || this.expired)
		{
			this.isValid = false;
			return;
		}

		// Active meaning, they were killed unexpectedly.
		if (entry == null || entry.status() == UnpluggedStatus.ACTIVE)
		{
			if (ServerEventsHandler.getInstance().isServerStopping())
			{
				return;
			}

			String mess = ConfigWrap.mess().unpluggedTerminated;

			if (mess == null || mess.isEmpty())
			{
				mess = "§cAFK session terminated§r";
			}

			final long delta = UnpluggedPlayerUtils.getStartTimeDelta(this.getStartTime());

			this.reason = ConfigWrap.mess().unpluggedUnsuccessful
					+ (ConfigWrap.mess().displayDuration
					   ? ConfigWrap.mess().unpluggedUnsuccessfulPrefix
					     + ConfigWrap.mess().duration.option.format(delta)
					   : "")
					+ ConfigWrap.mess().unpluggedUnsuccessfulPunctuation
					+ mess;

			UnpluggedState newState = new UnpluggedState(UnpluggedStatus.TERMINATED, -1, -1L, -1L, this.getReason());
			PlayerManager.getInstance().setState(this.getGameProfile(), newState);
			UnpluggedEntryList.getInstance().remove(this, false, UnpluggedStatus.TERMINATED);
		}

		this.isValid = false;
	}

	private void dismount()
	{
		if (this.getVehicle() != null)
		{
			if (this.getVehicle() instanceof Player)
			{
				this.stopRiding();
			}

			for (Entity entry : this.getVehicle().getPassengers())
			{
				if (entry instanceof Player)
				{
					entry.stopRiding();
				}
			}
		}
	}

//	@Override
//	public @NonNull String getIpAddress()
//	{
//		return "127.0.0.1";
//	}

//	@Override
//	protected void checkFallDamage(double y, boolean onGround, @NonNull BlockState state, @NonNull BlockPos pos)
//	{
//		this.doCheckFallDamage(0.0, y, 0.0, onGround);
//	}

	@Override
	public ServerPlayer teleport(@NonNull TeleportTransition transition)
	{
		super.teleport(transition);

		// Handle freeing the End
		if (this.wonGame)
		{
			ServerboundClientCommandPacket packet = new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN);
			this.connection.handleClientCommand(packet);
		}

		if (this.connection.player.isChangingDimension())
		{
			this.connection.player.hasChangedDimension();
		}

		return this.connection.player;
	}
}
