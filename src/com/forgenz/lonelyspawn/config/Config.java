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

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.forgenz.lonelyspawn.LonelySpawn;

public class Config extends AbstractConfig
{
	private static Config c = null;
	public static Config i()
	{
		return c;
	}
	
	
	public WorldConfig defaultWorld;
	
	/** The number of attempts to find a spawn away from players */
	public final int locationFindAttempts;
	
	/** If two players are within this distance when one is trying to spawn, the spawn will occur elsewhere */
	public final int minPlayerDistance;
	
	private final HashMap<String, WorldConfig> worlds = new HashMap<String, WorldConfig>();
	
	public Config()
	{
		c = this;
		FileConfiguration cfg = LonelySpawn.i().getConfig();
		
		int temp;
		
		temp = cfg.getInt("LocationFindAttempts", 20);
		locationFindAttempts = temp <= 0 ? 20 : temp;
		set(cfg, "LocaitonFindAttempts", locationFindAttempts);
		
		
		temp = cfg.getInt("MinPlayerDistance", 0);
		minPlayerDistance = temp < 0 ? 0 : temp; 
		set(cfg, "MinPlayerDistance", minPlayerDistance);
		
		String defWorldName = cfg.getString("DefaultWorld", "world");
		set(cfg, "DefaultWorld", defWorldName);
		
		// Configure worlds
		ConfigurationSection cfgSect = cfg.getConfigurationSection("Worlds");
		if (cfgSect == null)
		{
			cfgSect = cfg.createSection("Worlds");
		}
		
		for (String key : cfgSect.getKeys(false))
		{
			World world = Bukkit.getWorld(key);
			if (world == null)
			{
				LonelySpawn.i().getLogger().warning("The world " + key + " does not exist");
				continue;
			}
			
			ConfigurationSection worldCfgSect = cfgSect.getConfigurationSection(key);
			
			if (worldCfgSect != null)
			{
				WorldConfig worldCfg = new WorldConfig(worldCfgSect, world);
				
				if (!worldCfg.checkConfig())
				{
					LonelySpawn.i().getLogger().warning("Disabled " + world.getName() + " due to invalid configuration");
					continue;
				}
				
				worlds.put(world.getName().toLowerCase(), worldCfg);
			}
		}
		
		set(cfg, "Worlds", cfgSect);
		
		if (worlds.size() == 0)
		{
			return;
		}
		
		if (defWorldName != null)
		{
			defaultWorld = worlds.get(defWorldName.toLowerCase());
			
			if (defaultWorld == null)
				LonelySpawn.i().getLogger().warning("The default world " + defWorldName + " does not exist or has not be configured");
		}
		else
		{
			defaultWorld = null;
		}
	}
	
	public WorldConfig getWorldCfg(World world)
	{
		if (world == null)
			return null;
		return  worlds.get(world.getName().toLowerCase());
	}
	
	public WorldConfig[] getWorlds()
	{
		return worlds.values().toArray(new WorldConfig[0]);
	}
	
	public int getNumWorlds()
	{
		return worlds.size();
	}

	public boolean listenForSpawns()
	{
		if (worlds.size() == 0)
			return false;
		
		for (WorldConfig cfg : getWorlds())
		{
			if (cfg.useRandomSpawn)
				return true;
		}
		
		if (defaultWorld != null)
			return true;
		
		return false;
	}
}
