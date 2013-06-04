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

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.forgenz.lonelyspawn.LonelySpawn;
import com.forgenz.lonelyspawn.config.WorldConfig;

public class RandomLocationGen
{
	private static Random rand = new Random();
	
	/**
	 * Stop instances of the class from being created
	 */
	private RandomLocationGen()
	{
		
	}

	public static void findSpawn(Player player, WorldConfig cfg, Location loc)
	{
		getLocation(cfg, loc);
		// Check if the spawn is adequate
		LonelySpawn.i().spawnChecker.add(player, loc, cfg);
	}
	
	public static Location getLocation(WorldConfig cfg, Location cache)
	{
		// Make sure the centers world is valid
		if (cfg.center.getWorld() == null)
		{
			LonelySpawn.i().getLogger().warning("Null world passed to location generator");
			return null;
		}

		// Height range must be at least 1
		if (cfg.heightRange <= 0)
			return null;

		// Copy the world
		cache.setWorld(cfg.center.getWorld());
		
		// Short circuit check to always gen square locations
		if (null == null)
		{
			getCircularLocation(cfg.center, cfg.spawnRadius, cache);
		}
		else
		{
			getSquareLocation(cfg.center, cfg.spawnRadius, cache);
		}
		
		cache.setYaw(rand.nextFloat() * 360);
		
		return cache;
	}
	
	/**
	 * Generates a random location which is circular around the center
	 */
	private static Location getCircularLocation(Location center, int range, Location cacheLoc)
	{
		// Calculate a random direction for the X/Z values
		double theta = 2 * Math.PI * rand.nextDouble();
		
		// Generate a random radius
		double radius = rand.nextDouble() * range;
		
		// Set the X/Z coordinates
		double trig = Math.cos(theta);
		cacheLoc.setX(Location.locToBlock(radius * trig) + center.getBlockX() + 0.5);
		trig = Math.sin(theta);
		cacheLoc.setZ(Location.locToBlock(radius * trig) + center.getBlockZ() + 0.5);
		
		return cacheLoc;
	}
	
	/**
	 * Generates a random location which is square around the center
	 */
	private static Location getSquareLocation(Location center, int range, Location cacheLoc)
	{
		// Calculate the sum of all the block deviations from the center between minRange and range
		int totalBlockCount = (range * (++range)) >> 1;
		// Fetch a random number of blocks
		int blockCount = totalBlockCount - rand.nextInt(totalBlockCount);
		
		// While the block deviation from the center for the given range is
		// less than the number of blocks left we remove a layer of blocks
		while (range < blockCount)
			blockCount -= --range;
		
		// Pick a random location on the range line
		int lineLoc = rand.nextInt(range << 1);
		// Choose a line (North/East/West/South lines)
		// Then set the X/Z coordinates
		switch (rand.nextInt(4))
		{
		// East Line going North
		case 0:
			cacheLoc.setX(center.getBlockX() + range + 0.5D);
			cacheLoc.setZ(center.getBlockZ() + range - lineLoc + 0.5D);
			break;
		// South Line going East
		case 1:
			cacheLoc.setX(center.getBlockX() - range + lineLoc + 0.5D);
			cacheLoc.setZ(center.getBlockZ() + range + 0.5D);
			break;
		// West Line going South
		case 2:
			cacheLoc.setX(center.getBlockX() - range + 0.5D);
			cacheLoc.setZ(center.getBlockZ() - range + lineLoc + 0.5D);
			break;
		// North Line going west
		case 3:
		default:
			cacheLoc.setX(center.getBlockX() + range - lineLoc + 0.5D);
			cacheLoc.setZ(center.getBlockZ() - range + 0.5D);
		}
		
		return cacheLoc;
	}

}