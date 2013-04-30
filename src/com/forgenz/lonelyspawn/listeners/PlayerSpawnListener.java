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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.forgenz.lonelyspawn.config.Config;
import com.forgenz.lonelyspawn.config.WorldConfig;
import com.forgenz.lonelyspawn.util.PlayerFinder;
import com.forgenz.lonelyspawn.util.RandomLocationGen;

public class PlayerSpawnListener implements Listener
{
	/**
	 * Handles player spawns
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		// If the player has spawned on their bed, do nothing
		if (event.isBedSpawn())
			return;
		
		// Fetch the worlds config
		WorldConfig cfg = Config.i().getWorldCfg(event.getPlayer().getWorld());
		// If the world config was not found or randomSpawn is disabled for this world
		// Use the default world
		if (cfg == null || !cfg.useRandomSpawn)
			cfg = Config.i().defaultWorld;
		
		// If there is no default world, let minecraft do its thing
		if (cfg == null)
			return;
		
		// Fetch a random spawn location
		Location randomSpawn;
		
		do
		{
			randomSpawn = RandomLocationGen.getLocation(cfg);
		}
		while (PlayerFinder.playerNear(randomSpawn));
		
		// If no location was found just use the world location
		World world = Bukkit.getWorld(cfg.worldName);
		// If the world does not exist.... Let minecraft do its thing :/
		if (world == null)
			return;
		
		if (randomSpawn == cfg.center)
			randomSpawn = world.getSpawnLocation();
		
		event.setRespawnLocation(randomSpawn);
	}
}
