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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Player;

public class PlayerFinder
{
	private static Location pLoc = new Location(null, 0.0, 0.0, 0.0);
	
	public static boolean mobFlys(Entity entity)
	{
		if (entity instanceof Flying || entity instanceof Bat)
			return true;
		return false;
	}
	
	/**
	 * Scans nearby chunks for players on layers which cross a given height
	 * 
	 * @return True if there is a player within range of the location
	 */
	public static boolean playerNear(Location loc, int minPlayerDistanceSquared)
	{		
		// Fetch the entities location and sets the pLoc object to use
		// If the function is run in the primary thread we use a cached object else we make new ones
		Location pLoc = Bukkit.isPrimaryThread() ? PlayerFinder.pLoc : new Location(null, 0.0, 0.0, 0.0);
		
		// Iterate through each player to check if there is a player nearby
		for (Player player : Bukkit.getOnlinePlayers())
		{
			// Skip the player if they are in creative mode (And we should be skipping them)
			if (player.getGameMode() == GameMode.CREATIVE)
				continue;
			
			// If the worlds differ the player is not nearby
			if (player.getWorld() != loc.getWorld())
				continue;
			
			// Copy the players location into pLoc
			player.getLocation(pLoc);
			
			// Any players above y = 1000 are waiting for spawns
			if (pLoc.getBlockY() > 1000)
				continue;
			
			// Check the if the distance between the location and the player is less than the minimum
			int x = pLoc.getBlockX() - loc.getBlockX();
			int z = pLoc.getBlockZ() - loc.getBlockZ();
			if (x * x + z * z <= minPlayerDistanceSquared)
				return true;
		}
		
		// Return false if no nearby player was found
		return false;
	}
}
