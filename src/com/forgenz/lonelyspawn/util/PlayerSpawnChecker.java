package com.forgenz.lonelyspawn.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.util.QueuedProcess;
import org.bukkit.util.Vector;

import com.forgenz.lonelyspawn.LonelySpawn;
import com.forgenz.lonelyspawn.config.Config;
import com.forgenz.lonelyspawn.config.WorldConfig;

public class PlayerSpawnChecker implements Runnable
{
	private AtomicBoolean scheduled = new AtomicBoolean(false);
	private ConcurrentLinkedQueue<Player> players = new ConcurrentLinkedQueue<Player>();
	private ConcurrentHashMap<String, Location> playerLocations = new ConcurrentHashMap<String, Location>();
	private ConcurrentHashMap<String, WorldConfig> playerConfigs = new ConcurrentHashMap<String, WorldConfig>();
	
	public boolean checkSpawn(final Player player, final WorldConfig cfg, final Location spawn)
	{
		if (player == null)
		{
			LonelySpawn.i().getLogger().severe("Invalid player was provided to the spawn checker");
			return true;
		}
		
		if (!player.isValid())
			return true;
		
		if (cfg == null || spawn == null || spawn.getWorld() == null)
		{
			LonelySpawn.i().getLogger().severe("Invalid parameters were provided to spawn checker when spawning player: " + player.getName());
			return false;
		}
		
		// Ignore player if we are not in the main thread
		if (!Bukkit.isPrimaryThread())
		{
			LonelySpawn.i().getLogger().severe("Spawn was checked in async");
			return true;
		}
				
		// Wait for the chunk to be loaded
		spawn.getWorld().loadChunkWithCallback(spawn.getBlockX() >> 4, spawn.getBlockZ() >> 4, new QueuedProcess<Chunk>()
				{				
					public void accept(Chunk chunk)
					{
						if (!player.isValid())
							return;
						
						// If the spawn is bad we find a new one
						if (!checkSpawn(player, cfg, spawn, chunk))
						{
							LonelySpawn.i().spawnFinder.addSpawningPlayer(player, cfg);
						}
					}
				});
		return true;
	}
	
	private boolean checkSpawn(final Player player, final WorldConfig cfg, final Location spawn, Chunk chunk)
	{
		// If the biome is Ocean we do not want to spawn the player there
		Biome spawnBiome = spawn.getWorld().getBiome(spawn.getBlockX(), spawn.getBlockZ());
		if (spawnBiome == Biome.OCEAN || spawnBiome == Biome.FROZEN_OCEAN)
		{
			// We don't need this chunk anymore, make sure we unload it
			chunk.unload();
			return false;
		}
		
		// Set the players spawn Y loc to a reasonable value
		spawn.setY(spawn.getWorld().getHighestBlockYAt(spawn));
		
		// Teleport the player
		player.teleport(spawn);
		// Stop the player falling to their death
		Vector v = player.getVelocity();
		v.setY(0);
		player.setVelocity(v);
		
		// Send the player a message if one exists
		if (cfg.spawnMessage.length() > 0)
			player.sendMessage(cfg.spawnMessage);
		else if (Config.i().spawnMessage.length() > 0)
			player.sendMessage(Config.i().spawnMessage);
		
		// Mark Location as available for use
		LonelySpawn.i().spawnFinder.addUnusedLocation(spawn);
		
		return true;
	}

	public void add(Player player, Location loc, WorldConfig cfg)
	{
		// If the player is invalid return
		if (player == null || !player.isValid() || loc == null)
		{
			return;
		}
		
		players.add(player);
		playerLocations.put(player.getName(), loc);
		playerConfigs.put(player.getName(), cfg);
		
		if (scheduled.compareAndSet(false, true))
		{
			Bukkit.getScheduler().runTask(LonelySpawn.i(), this);
		}
	}

	public void run()
	{
		scheduled.set(false);
		
		while (!players.isEmpty())
		{
			// Fetch the player
			Player player = players.poll();
			
			// No player??
			if (player == null)
			{
				continue;
			}
			
			// Fetch the players spawn and config
			Location spawn = playerLocations.remove(player.getName());
			WorldConfig cfg = playerConfigs.remove(player.getName()); 
			
			// If the player is invalid we ignore it
			if (!player.isValid())
			{
				continue;
			}
			
			// If the spawn is bad we find a new one
			if (!checkSpawn(player, cfg, spawn))
			{
				LonelySpawn.i().spawnFinder.addSpawningPlayer(player, cfg);
			}
		}
	}
}
