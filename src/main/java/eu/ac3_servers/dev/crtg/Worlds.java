package eu.ac3_servers.dev.crtg;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

	public static EditSession editSession = null;
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

	public static void addWall() throws MaxChangedBlocksException
	{
		CuboidRegion sel = new CuboidRegion( getEditSession().getWorld(), BukkitUtil.toVector( getSpawnPoint1() ), BukkitUtil
				.toVector( getSpawnPoint2() ) );

		Region wallsRegion = sel.getWalls();

		getEditSession().replaceBlocks( wallsRegion, new BlockMask( getEditSession().getSurvivalExtent(), new BaseBlock( 0 ) ), new SingleBlockPattern( new BaseBlock( Material.GLASS
																																											   .getId() ) ) );

	}

	public static void removeWall() throws MaxChangedBlocksException
	{
		CuboidRegion sel = new CuboidRegion( getEditSession().getWorld(), BukkitUtil.toVector( getSpawnPoint1() ), BukkitUtil
				.toVector( getSpawnPoint2() ) );

		Region wallsRegion = sel.getWalls();

		getEditSession().replaceBlocks( wallsRegion, new BlockMask( getEditSession().getSurvivalExtent(), new BaseBlock( Material.GLASS
																																 .getId() ) ), new SingleBlockPattern( new BaseBlock( 0 ) ) );

	}

	public static void placeBeacon() throws MaxChangedBlocksException
	{

		Location maxLoc = getWorld().getSpawnLocation();
		maxLoc.setY( 150 );

		CuboidRegion sel = new CuboidRegion( getEditSession().getWorld(), BukkitUtil.toVector( getWorld().getSpawnLocation()
																									   .add( 0, 1, 0 ) ), BukkitUtil
													 .toVector( maxLoc ) );

		getEditSession().setBlocks( sel, new SingleBlockPattern( new BaseBlock( 0 ) ) );
		getEditSession().setBlock( BukkitUtil.toVector( getWorld().getSpawnLocation() ), new SingleBlockPattern( new BaseBlock( Material.BEACON
																																		.getId() ) ) );

	}

	private static EditSession getEditSession()
	{
		if ( editSession != null ) return editSession;
		for ( com.sk89q.worldedit.world.World world : WorldEdit.getInstance().getServer().getWorlds() ) {
			if ( !( world instanceof BukkitWorld ) ) continue;
			BukkitWorld bworld = ( (BukkitWorld) world );
			if ( bworld.getWorld().equals( getWorld() ) ) {
				editSession = WorldEdit.getInstance()
						.getEditSessionFactory()
						.getEditSession( world, Integer.MAX_VALUE );
			}
		}
		return editSession;
	}

	public static void cleanup()
	{

		editSession = null;

		if ( nether_world != null ) {
			Bukkit.unloadWorld( nether_world, false );
			nether_world = null;
		}

		if ( world != null ) {
			Bukkit.unloadWorld( world, false );
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
