package eu.ac3_servers.dev.crtg;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
@Getter
public class Worlds
{

	private static World nether_world = null;
	private static World world = null;

	private static Location spawnPoint1;
	private static Location spawnPoint2;

	public static World getNetherWorld()
	{
		return nether_world;
	}

	public static void setNetherWorld( World netherWorld )
	{
		if ( GameState.getCurrentState().equals( GameState.INGAME ) ) return;
		Worlds.nether_world = netherWorld;
	}

	public static World getWorld()
	{
		return world;
	}

	public static void setWorld( World world )
	{
		if ( GameState.getCurrentState().equals( GameState.INGAME ) ) return;
		Worlds.world = world;
	}

	public static Location getSpawnPoint1()
	{
		return spawnPoint1;
	}

	public static void setSpawnPoint1( Location spawnPoint1 )
	{
		Worlds.spawnPoint1 = spawnPoint1;
	}

	public static Location getSpawnPoint2()
	{
		return spawnPoint2;
	}

	public static void setSpawnPoint2( Location spawnPoint2 )
	{
		Worlds.spawnPoint2 = spawnPoint2;
	}

	public static boolean locIsInSpawn( Location location )
	{
		if ( !location.getWorld().equals( getWorld() ) ) return false;
		return locationInCube( location, getSpawnPoint2(), getSpawnPoint1() );
	}

	public static boolean playerIsInSpawn( Player player )
	{
		if ( !player.getWorld().equals( getWorld() ) ) return false;
		return locationInCube( player.getLocation(), getSpawnPoint2(), getSpawnPoint1() );
	}

	public static boolean locationInCube( Location pLoc, Location min, Location max )
	{

		if ( pLoc.getWorld().equals( min.getWorld() ) || min.getWorld().equals( max.getWorld() ) ) return false;

		if ( pLoc.getBlockX() >= min.getBlockX() && pLoc.getBlockX() <= max.getBlockX() ) {
			if ( pLoc.getBlockY() >= min.getBlockY() && pLoc.getBlockY() <= max.getBlockY() ) {
				if ( pLoc.getBlockZ() >= min.getBlockZ() && pLoc.getBlockZ() <= max.getBlockZ() ) { return true; }
			}
		}

		if ( pLoc.getBlockX() <= min.getBlockX() && pLoc.getBlockX() >= max.getBlockX() ) {
			if ( pLoc.getBlockY() <= min.getBlockY() && pLoc.getBlockY() >= max.getBlockY() ) {
				if ( pLoc.getBlockZ() <= min.getBlockZ() && pLoc.getBlockZ() >= max.getBlockZ() ) { return true; }
			}
		}

		return false;

	}

	public static void cleanup()
	{
		if ( nether_world != null ) {
			File file = nether_world.getWorldFolder();
			Bukkit.unloadWorld( nether_world, false );
			deleteFolder( file );
			nether_world = null;
		}

		if ( world != null ) {
			File file = world.getWorldFolder();
			Bukkit.unloadWorld( world, false );
			deleteFolder( file );
			world = null;
		}

		spawnPoint1 = null;
		spawnPoint2 = null;

	}

	private static void deleteFolder( File file )
	{
		if ( !file.isDirectory() ) {
			file.delete();
			return;
		}
		File[] files = file.listFiles();
		for ( File f : files ) {
			deleteFolder( f );
		}
	}
}
