package com.forgenz.lonelyspawn.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.forgenz.lonelyspawn.LonelySpawn;
import com.forgenz.lonelyspawn.config.Config;
import com.forgenz.lonelyspawn.config.WorldConfig;

public class PlayerSpawnFinder implements Runnable
{
	private AtomicBoolean scheduled = new AtomicBoolean(false);
	private ConcurrentLinkedQueue<Player> spawningPlayers = new ConcurrentLinkedQueue<Player>();
	private ConcurrentHashMap<String, WorldConfig> spawningPlayerConfigs = new ConcurrentHashMap<String, WorldConfig>();
	
	private ConcurrentLinkedQueue<Location> unusedLocations = new ConcurrentLinkedQueue<Location>();

	public void run()
	{
		scheduled.set(false);
		long start = System.nanoTime();
		
		// Try not to let the task run more than 2ms per tick
		while (!spawningPlayers.isEmpty() && System.nanoTime() - start < 2000000)
		{
			Player player = spawningPlayers.poll();
			WorldConfig cfg = spawningPlayerConfigs.remove(player.getName());
			
			// If the player or the config is invalid we continue
			if (player == null || !player.isValid() || cfg == null)
			{
				continue;
			}
	
			// Find a spawn for the player
			Location loc = unusedLocations.peek() != null ? unusedLocations.poll() : new Location(null, 0.0, 0.0, 0.0);
			RandomLocationGen.findSpawn(player, cfg, loc);
		}
		
		// Make sure the task is not already scheduled to run
		if (!spawningPlayers.isEmpty() && scheduled.compareAndSet(false, true))
		{
			if (Config.i().generateSpawnsAsynchronously)
			{
				Bukkit.getScheduler().runTaskAsynchronously(LonelySpawn.i(), this);
			}
			else
			{
				Bukkit.getScheduler().runTask(LonelySpawn.i(), this);
			}
			
		}
	}
	
	public void addSpawningPlayer(Player player, WorldConfig cfg)
	{
		if (player == null || cfg == null)
		{
			return;
		}
		
		spawningPlayers.add(player);
		spawningPlayerConfigs.put(player.getName(), cfg);
		
		// Make sure the task is not already scheduled to run
		if (scheduled.compareAndSet(false, true))
		{
			if (Config.i().generateSpawnsAsynchronously)
			{
				Bukkit.getScheduler().runTaskAsynchronously(LonelySpawn.i(), this);
			}
			else
			{
				Bukkit.getScheduler().runTask(LonelySpawn.i(), this);
			}
		}
	}
	
	public void addUnusedLocation(Location loc)
	{
		unusedLocations.add(loc);
	}
}
