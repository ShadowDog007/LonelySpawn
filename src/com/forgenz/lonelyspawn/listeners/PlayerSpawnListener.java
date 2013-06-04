/*
 * Copyright 2013 Michael McKnight. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.forgenz.lonelyspawn.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.forgenz.lonelyspawn.LonelySpawn;
import com.forgenz.lonelyspawn.config.Config;
import com.forgenz.lonelyspawn.config.WorldConfig;

public class PlayerSpawnListener implements Listener
{
	private final LonelySpawn plugin;
	
	public PlayerSpawnListener(LonelySpawn plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerFirstJoin(PlayerJoinEvent event)
	{
		// If the player has played before we do nothing
		if (event.getPlayer().hasPlayedBefore())
		{
			return;
		}
		
		// Fetch the config
		WorldConfig cfg = getConfig(event.getPlayer());
		
		if (cfg == null)
		{
			return;
		}
		
		// Queue player for spawning
		plugin.spawnFinder.addSpawningPlayer(event.getPlayer(), cfg);
		
		event.getPlayer().teleport(cfg.center);
	}
	
	/**
	 * Handles player spawns
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(final PlayerRespawnEvent event)
	{		
		// If the player has spawned on their bed, do nothing
		if (event.isBedSpawn())
			return;
		
		final WorldConfig cfg = getConfig(event.getPlayer());
		
		if (cfg == null)
		{
			return;
		}
		
		plugin.spawnFinder.addSpawningPlayer(event.getPlayer(), cfg);
		
		event.setRespawnLocation(cfg.center);
	}
	
	public WorldConfig getConfig(Player player)
	{
		// Fetch the worlds config
		WorldConfig cfg = Config.i().getWorldCfg(player.getWorld());
		// If the world config was not found or randomSpawn is disabled for this world
		// Use the default world
		if (cfg == null || !cfg.useRandomSpawn) cfg = Config.i().defaultWorld;

		return cfg;
	}
}
