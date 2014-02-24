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

package com.forgenz.lonelyspawn.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;

public class WorldConfig extends AbstractConfig
{
	public final Location center;
	/** The name of the world this instance manages */
	public final String worldName;
	
	/** The X component of the center of the world */
	public final int centerX;
	/** The Z component of the center of the world */
	public final int centerZ;
	
	public final boolean useRandomSpawn;
	
	public final boolean useHighestY;
	
	/** The max distance above minY the player can spawn at */
	public final int heightRange;
	/** The min Y a player will spawn at */
	public final int minY;
	
	/** The radius around the center to spawn players */
	public final int spawnRadius;
	
	/** How lonely do players want to be? */
	public final int minPlayerDistance, minPlayerDistanceSquared;
	
	public final String spawnMessage;
	
	
	public WorldConfig(ConfigurationSection cfg, World world)
	{
		this.worldName = world.getName();
		
		// Fetch center location
		this.centerX = getAndSet(cfg, "CenterX", 0);
		this.centerZ = getAndSet(cfg, "CenterZ", 0);
		
		// Fetch min/max Y
		int maxy = getAndSet(cfg, "MaxY", world.getEnvironment() == Environment.NORMAL ? 80 : 128);
		int miny = getAndSet(cfg, "MinY", world.getEnvironment() == Environment.NORMAL ? 50 : 40);

		if (maxy < miny)
		{
			maxy = maxy ^ miny;
			miny = maxy ^ miny;
			maxy = maxy ^ miny;
		}
		
		maxy = maxy < 0 ? (world.getEnvironment() == Environment.NORMAL ? 80 : 128) : maxy;
		miny = miny < 0 ? (world.getEnvironment() == Environment.NORMAL ? 50 : 40) : miny;
		
		if (maxy > 256)
			maxy = 256;
		
		set(cfg, "MaxY", maxy);
		set(cfg, "MinY", miny);

		this.heightRange = maxy - miny;
		this.minY = miny;

		this.useHighestY = getAndSet(cfg, "SpawnOnHighestBlock", true);

		// Fetch the spawn radius
		int spawnRadius = getAndSet(cfg, "SpawnRadius", 3000);
		if (spawnRadius < 0)
			spawnRadius = 1000;
		set(cfg, "SpawnRadius", spawnRadius);
		this.spawnRadius = spawnRadius;
		
		int minPlayerDistance = getAndSet(cfg, "MinPlayerDistance", 300);
		if (minPlayerDistance < 0)
			minPlayerDistance = 0;
		
		this.minPlayerDistance = minPlayerDistance;
		this.minPlayerDistanceSquared = minPlayerDistance * minPlayerDistance;
		
		String spawnMessage = getAndSet(cfg, "SpawnMessage", "");
		this.spawnMessage = ChatColor.translateAlternateColorCodes('&', spawnMessage);
		
		this.useRandomSpawn = getAndSet(cfg, "UseRandomSpawn", false);
		
		this.center = new Location(Bukkit.getWorld(worldName), centerX, 10000, centerZ);
	}
}
