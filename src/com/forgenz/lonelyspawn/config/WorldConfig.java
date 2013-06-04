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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;

import com.forgenz.lonelyspawn.LonelySpawn;

public class WorldConfig extends AbstractConfig
{
	public final Location center;
	/** The name of the world this instance manages */
	public final String worldName;
	
	/** The X component of the center of the world */
	public final Integer centerX;
	/** The Z component of the center of the world */
	public final Integer centerZ;
	
	public final boolean useRandomSpawn;
	
	public final boolean useHighestY;
	
	/** The max distance above minY the player can spawn at */
	public final int heightRange;
	/** The min Y a player will spawn at */
	public final int minY;
	
	/** The radius around the center to spawn players */
	public final Integer spawnRadius;
	
	
	public WorldConfig(ConfigurationSection cfg, World world)
	{
		worldName = world.getName();
		
		// Fetch center location
		centerX = getIntegerValue(cfg, "CenterX");
		centerZ = getIntegerValue(cfg, "CenterZ");
		
		if (centerX != null && centerZ != null)
		{
			Integer temp;
			// Fetch min/max Y
			temp = cfg.getInt("MaxY", world.getEnvironment() == Environment.NORMAL ? 80 : 128);
			set(cfg, "MaxY", temp);
			
			Integer temp2;
			temp2 = cfg.getInt("MinY", world.getEnvironment() == Environment.NORMAL ? 50 : 40);
			set(cfg, "MinY", temp2);
			
			temp = temp < 0 ? (world.getEnvironment() == Environment.NORMAL ? 80 : 128) : temp;
			temp = temp < 0 ? (world.getEnvironment() == Environment.NORMAL ? 50 : 40) : temp;
			
			if (temp < temp2)
			{
				temp = temp ^ temp2;
				temp2 = temp ^ temp2;
				temp = temp ^ temp2;
			}
			
			temp = temp < 0 ? 80 : temp;
			temp2 = temp2 < 0 ? 50 : temp2;
			
			heightRange = temp - temp2;
			minY = temp2;
			
			useHighestY = cfg.getBoolean("SpawnOnHighestBlock", true);
			set(cfg, "SpawnOnHighestBlock", useHighestY);
			
			// Fetch the spawn radius
			spawnRadius = getIntegerValue(cfg, "SpawnRadius");
			set(cfg, "SpawnRadius", spawnRadius);
		}
		// If the center coords are invalid theres no point in gathering values for these variables
		else
		{
			heightRange = 0;
			minY = 0;
			spawnRadius = null;
			useHighestY = true;
		}
		
		if (cfg.getBoolean("UseRandomSpawn", false))
		{
			if (spawnRadius != null)
			{
				useRandomSpawn = true;
			}
			else
			{
				useRandomSpawn = false;
				LonelySpawn.i().getLogger().warning("RandomSpawn disabled in " + worldName + " due to invalid config");
			}
		}
		else
		{
			useRandomSpawn = false;
		}
		
		set(cfg, "UseRandomSpawn", useRandomSpawn);
		
		center = new Location(Bukkit.getWorld(worldName), centerX, 10000, centerZ);
	}
	
	public Integer getIntegerValue(ConfigurationSection cfg, String key)
	{
		int temp = cfg.getInt(key, Integer.MIN_VALUE);
		
		if (temp == Integer.MIN_VALUE)
			return null;
		
		return temp;
	}
	
	public boolean checkConfig()
	{
		if (centerX == null || centerZ == null)
		{
			LonelySpawn.i().getLogger().warning("Invalid center location for " + worldName);
			return false;
		}
		
		if (spawnRadius == null)
		{
			LonelySpawn.i().getLogger().warning("SpawnRadius is not set for " + worldName);
			return false;
		}
		
		return true;
	}
}
