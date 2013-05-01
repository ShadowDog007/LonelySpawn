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
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.forgenz.lonelyspawn.LonelySpawn;
import com.forgenz.lonelyspawn.config.Config;
import com.forgenz.lonelyspawn.config.WorldConfig;

public class RandomLocationGen
{
	private static Random rand = new Random();
	private static Location loc = new Location(null, 0.0, 0.0, 0.0);
	
	/**
	 * Stop instances of the class from being created
	 */
	private RandomLocationGen()
	{
		
	}

	/**
	 * Generates a random location around the center location
	 */
	public static Location getLocation(boolean circle, Location center, int range, int minHeight, int heightRange, boolean top)
	{
		Location loc = Bukkit.isPrimaryThread() ? RandomLocationGen.loc : new Location(null, 0.0, 0.0, 0.0);
		return getLocation(circle, center, range, minHeight, heightRange, top, loc);
	}
	
	/**
	 * Generates a random location around the center location
	 */
	public static Location getLocation(boolean circle, Location center, int range, int minHeight, int heightRange, boolean top, Location cacheLoc)
	{
		// Make sure the centers world is valid
		if (center.getWorld() == null)
		{
			LonelySpawn.i().getLogger().warning("Null world passed to location generator");
			return center;
		}
		
		// Height range must be at least 1
		if (heightRange < 0)
			heightRange = 1;
		
		int maxHeight = center.getWorld().getEnvironment() == Environment.NORMAL ? 256 : 128;
		if (minHeight + heightRange > maxHeight)
		{
			heightRange -= maxHeight - (minHeight + heightRange); 
		}
		
		// Copy the world
		cacheLoc.setWorld(center.getWorld());
		
		// Make 10 attempts to find a safe spawning location
		for (int i = 0; i < Config.i().locationFindAttempts; ++i)
		{
			// Generate the appropriate type of location
			if (circle)
			{
				getCircularLocation(center, range, cacheLoc);
			}
			else
			{
				getSquareLocation(center, range, cacheLoc);
			}
			
			// Generate coordinates for Y
			if (top || i + 1 == Config.i().locationFindAttempts)
			{
				Block block = cacheLoc.getWorld().getHighestBlockAt(cacheLoc);
				block.getLocation(cacheLoc);
				cacheLoc.setY(cacheLoc.getY() + 1.5);
				
				if (isBlockSafe(block))
				{
					return cacheLoc;
				}
			}
			else
			{
				cacheLoc.setY(rand.nextInt(heightRange) + minHeight + 0.5);
				
				// If the location is safe we can return the location
				if (isLocationSafe(cacheLoc, center.getBlockY(), heightRange))
				{
					// Generate a random Yaw/Pitch
					cacheLoc.setYaw(rand.nextFloat() * 360.0F);
					return cacheLoc;
				}
			}
		}
		
		// If no safe location was found in a reasonable timeframe just return the center
		return center;
	}
	
	private static boolean isBlockSafe(Block block)
	{
		return !block.isLiquid() && !block.isEmpty();
	}
	
	/**
	 * Makes sure the given location is safe to spawn something there
	 * @return true if the location is safe
	 */
	private static boolean isLocationSafe(Location location, int centerY, int heightRange)
	{
		// Check the location is safe
		Block block = location.getBlock();
		
		// If the location is not safe we try again			
		if (!(isSafeBlock(block) && isSafeBlock(block.getRelative(BlockFace.UP))))
		{
			return false;
		}
		
		// Calculate the height diff
		int heightDiff = Math.abs(location.getBlockY() - centerY);
		
		boolean onGround = false;
		// Move the position down as close to the ground as we can
		while (heightDiff < heightRange)
		{
			block = block.getRelative(BlockFace.DOWN);
			
			// If the below block is empty we shift the location down
			if (isSafeBlock(block))
			{
				location.setY(location.getY() - 1.0D);
				++heightDiff;
			}
			// If it isn't the mob is on the ground
			else
			{
				onGround = true;
				break;
			}
		}

		// If the location is on or near the ground the location is good
		if (onGround || !isSafeBlock(block.getRelative(BlockFace.DOWN)) || !isSafeBlock(block.getRelative(BlockFace.DOWN, 2)))
		{
			return true;
		}
		
		return false;
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
	
	/**
	 * Checks if the block is of a type which is safe for spawning inside of
	 * @return true if the block type is safe
	 */
	private static boolean isSafeBlock(Block block)
	{
		Material mat = block.getType();
		
		switch (mat)
		{
		case AIR:
		case WEB:
		case VINE:
		case SNOW:
		case LONG_GRASS:
		case DEAD_BUSH:
		case SAPLING:
			return true;
		default:
			return false;
		}
	}

	public static Location getLocation(WorldConfig cfg)
	{
		return getLocation(false, cfg.center, cfg.spawnRadius, cfg.minY, cfg.heightRange, cfg.useHighestY);
	}
}
