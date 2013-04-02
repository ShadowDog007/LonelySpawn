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

package com.forgenz.lonelyspawn.util;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.forgenz.lonelyspawn.LonelySpawn;
import com.forgenz.lonelyspawn.config.Config;
import com.forgenz.lonelyspawn.config.WorldConfig;

public class RandomLocationGen
{
	private static Random rand = new Random();
	private static Location loc = new Location(null, 0.0, 0.0, 0.0);
	
	private RandomLocationGen()
	{
		
	}

	public static Location getLocation(WorldConfig cfg)
	{
		Location loc = Bukkit.isPrimaryThread() ? RandomLocationGen.loc : new Location(null, 0.0, 0.0, 0.0);
		return getLocation(cfg, loc);
	}
	
	public static Location getLocation(WorldConfig cfg, Location cacheLoc)
	{
		// Make sure spawning is enabled in the world
		if (!cfg.useRandomSpawn)
		{
			return null;
		}
		
		// Make sure the centers world is valid
		World world = Bukkit.getWorld(cfg.worldName);
		if (world == null)
		{
			LonelySpawn.i().getLogger().severe("Player spawned in a world which does not exist");
			return null;
		}

		// Copy the world
		cacheLoc.setWorld(world);
		
		// Make 10 attempts to find a safe spawning location
		for (int i = 0; i < Config.i().locationFindAttempts; ++i)
		{
			// Calculate a random direction for the X/Z values
			double theta = 2 * Math.PI * rand.nextDouble();
			
			// Generate random locations for X/Z
			double trig = Math.cos(theta);
			cacheLoc.setX(Location.locToBlock(rand.nextDouble() * cfg.heightRange * trig) + cfg.centerX + 0.5);
			trig = Math.sin(theta);
			cacheLoc.setZ(Location.locToBlock(rand.nextDouble() * cfg.heightRange * trig) + cfg.centerZ + 0.5);
			
			// Generate coordinates for Y
			cacheLoc.setY(rand.nextInt(cfg.heightRange) + cfg.minY + 0.5);
			
			// Generate a random Yaw/Pitch
			cacheLoc.setYaw(rand.nextFloat() * 360.0F);
			
			// Check the location is safe
			Block block = cacheLoc.getBlock();
			
			// If the location is not safe we try again			
			if (!(isSafe(block) && isSafe(block.getRelative(BlockFace.UP))))
			{
				continue;
			}
			
			boolean onGround = false;
			// Move the position down as close to the ground as we can
			// On our last attempt to find a spawn location we continue down till we find something
			while (cacheLoc.getBlockY() >= cfg.minY || i == Config.i().locationFindAttempts)
			{
				block = block.getRelative(BlockFace.DOWN);
				
				// If the below block is empty we shift the location down
				if (isSafe(block))
				{
					cacheLoc.setY(cacheLoc.getY() - 1.0D);
				}
				// If it isn't the mob is on the ground
				else
				{
					onGround = true;
					break;
				}
			}

			// If the location is on or near the ground the location is good
			if (onGround || !isSafe(block.getRelative(BlockFace.DOWN)) || !isSafe(block.getRelative(BlockFace.DOWN, 2)))
			{
				// If there are no nearby players, or we are on our last attempt, return the spawn location
				if (Config.i().minPlayerDistance == 0 || i == Config.i().locationFindAttempts  || !nearbyPlayers(cacheLoc))
					return cacheLoc;
			}
		}
		
		// If no safe location was found in a reasonable timeframe just return null
		return null;
	}
	
	private static Location loc2 = new Location(null, 0.0, 0.0, 0.0);
	
	private static boolean nearbyPlayers(Location loc)
	{
		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if (player.getLocation(loc2).distanceSquared(loc) < Config.i().minPlayerDistance)
				return true;
		}
			
		return false;
	}
	
	private static boolean isSafe(Block block)
	{
		Material mat = block.getType();
		
		switch (mat)
		{
		case AIR:
		case WEB:
		case VINE:
		case WATER:
		case STATIONARY_WATER:
		case SNOW:
		case LONG_GRASS:
		case DEAD_BUSH:
		case SAPLING:
			return true;
		default:
			return false;
		}
	}
}
