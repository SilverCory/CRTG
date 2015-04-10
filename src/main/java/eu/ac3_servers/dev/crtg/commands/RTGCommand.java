package eu.ac3_servers.dev.crtg.commands;

import eu.ac3_servers.dev.crtg.CRTGPlugin;
import eu.ac3_servers.dev.crtg.GameState;
import eu.ac3_servers.dev.crtg.Worlds;
import eu.ac3_servers.dev.crtg.event.StartCRTGEvent;
import eu.ac3_servers.dev.crtg.event.WinCRTGEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class RTGCommand implements CommandExecutor
{
	private final CRTGPlugin plugin;

	public RTGCommand( CRTGPlugin plugin ) { this.plugin = plugin; }

	@Override public boolean onCommand( CommandSender sender, Command command, String s, String[] args )
	{

		if ( args.length == 1 && args[ 0 ].equalsIgnoreCase( "start" ) ) {

			if ( !sender.hasPermission( "rtg.commands" ) ) {
				sender.sendMessage( c( "&e&lYou don't have permission to start the game!" ) );
				return true;
			}

			if ( GameState.getCurrentState().equals( GameState.INGAME ) ) {
				sender.sendMessage( c( "&d&lA game is already running!" ) );
				return true;
			}

			SimpleDateFormat sd = new SimpleDateFormat( "MM-dd_HH-mm-ss" );
			CRTGPlugin.setWorldName( sd.format( new Date() ) );

			GameState.setCurrentState( GameState.PREPARING );

			sender.sendMessage( c( "&aCreating worlds with name: &d" + CRTGPlugin.getWorldName() ) );
			Bukkit.broadcastMessage( c( "&9&lPreparing the worlds." ) );
			Bukkit.broadcastMessage( c( "&9&l   The server may lag." ) );

			// Create the nether world.
			WorldCreator wc_nether = new WorldCreator( CRTGPlugin.getWorldName() + "_nether" );
			wc_nether.environment( World.Environment.NORMAL );

			Worlds.setNetherWorld( wc_nether.createWorld() );

			Worlds.getNetherWorld().setKeepSpawnInMemory( false );
			Worlds.getNetherWorld().setDifficulty( Difficulty.HARD );

			// Create the normal world.
			WorldCreator wc = new WorldCreator( CRTGPlugin.getWorldName() );
			wc.environment( World.Environment.NORMAL );

			Worlds.setWorld( wc.createWorld() );

			Worlds.getWorld().setKeepSpawnInMemory( false );
			Worlds.getWorld().setDifficulty( Difficulty.HARD );
			Worlds.getWorld().setPVP( false );

			Location loc = Worlds.getWorld().getSpawnLocation();
			Location point1 = loc.add( 7, 4, 7 );
			Location point2 = loc.subtract( 7, 4, 7 );

			int x, y, z;
			int maxX, maxY, maxZ;
			x = point2.getBlockX();
			y = point2.getBlockY();
			z = point2.getBlockZ();

			maxX = point1.getBlockX();
			maxY = point1.getBlockY();
			maxZ = point1.getBlockZ();

			while ( x <= maxX ) {
				while ( y <= maxY ) {
					while ( z <= maxZ ) {
						Block block = Worlds.getWorld().getBlockAt( x, y, z );
						if ( block == null || !block.getType().equals( Material.AIR ) ) continue;
						if ( block.getX() != point2.getBlockX() && block.getX() != point1.getBlockX() ) continue;
						if ( block.getZ() != point2.getBlockZ() && block.getZ() != point1.getBlockZ() ) continue;
						block.setType( Material.GLASS );
						z++;
					}
					y++;
				}
				x++;
			}

			Location beaconLoc = loc;
			while ( ( beaconLoc = beaconLoc.add( 0, 1, 0 ) ).getBlockY() <= Worlds.getWorld().getMaxHeight() ) {
				if ( beaconLoc.getBlock().getType() != null && !beaconLoc.getBlock()
						.getType()
						.equals( Material.AIR ) ) { beaconLoc.getBlock().setType( Material.AIR ); }
			}

			Bukkit.broadcastMessage( c( "&9&lWorld generation complete." ) );
			plugin.getServer().getScheduler().runTaskTimer( plugin, new TeleportTask( plugin ), 3, 3 );
			return true;

		}

		if ( args.length == 1 && args[ 0 ].equalsIgnoreCase( "stop" ) ) {

			if ( !sender.hasPermission( "rtg.commands" ) ) {
				sender.sendMessage( c( "&e&lYou don't have permission to stop the game!" ) );
				return true;
			}

			if ( !GameState.getCurrentState().equals( GameState.INGAME ) ) {
				sender.sendMessage( c( "&d&lA game needs to be running!" ) );
				return true;
			}

			if ( !( sender instanceof Player ) ) {
				sender.sendMessage( c( "&d&lYou need to be a player." ) );
				return true;
			}

			Bukkit.broadcastMessage( c( "&d&lThe game was forcefully stopped!" ) );
			plugin.getServer().getPluginManager().callEvent( new WinCRTGEvent( ( (Player) sender ) ) );

			return true;

		}

		return false;
	}

	private String c( String s ) { return ChatColor.translateAlternateColorCodes( '&', s ); }
}

class TeleportTask extends BukkitRunnable implements Runnable
{
	private final CRTGPlugin plugin;
	private final Player[] players;
	private final Location loc;
	private int i = -1;

	/**
	 * If there's lots of players it lags some people.
	 *
	 * @param plugin
	 */
	public TeleportTask( CRTGPlugin plugin )
	{
		this.plugin = plugin;
		this.players = (Player[]) Bukkit.getOnlinePlayers().toArray();
		this.loc = Worlds.getWorld().getSpawnLocation();
	}

	@Override public void run()
	{

		if ( i >= players.length - 1 ) {
			Bukkit.broadcastMessage( c( "&6&lStarting the game in 90 seconds!" ) );
			plugin.getServer().getScheduler().runTaskTimer( plugin, new CountDownTask( plugin ), 20, 20 );
			cancel();
			return;
		}


		for ( int j = 0; j < 3; j++ ) {
			Player p = players[ i++ ];
			// TODO save the inventory? O.o
			p.getInventory().clear();
			p.teleport( loc, PlayerTeleportEvent.TeleportCause.PLUGIN );
			p.sendMessage( c( "&dYou have been teleported to the game lobby." ) );
			plugin.addPlayer( p );
		}

	}

	private String c( String s ) { return ChatColor.translateAlternateColorCodes( '&', s ); }

}

class CountDownTask extends BukkitRunnable implements Runnable
{

	private static int seconds = 90;
	private final CRTGPlugin plugin;

	public CountDownTask( CRTGPlugin plugin )
	{
		this.plugin = plugin;
	}

	@Override public void run()
	{

		seconds--;

		switch ( seconds ) {
			case 60:
				Bukkit.broadcastMessage( c( "&560 seconds remaining." ) );
				break;
			case 30:
				Bukkit.broadcastMessage( c( "&530 seconds remaining." ) );
				break;
			case 10:
				Bukkit.broadcastMessage( c( "&510 seconds remaining." ) );
				break;
			case 5:
				Bukkit.broadcastMessage( c( "&55 seconds remaining." ) );
				break;
			case 0:
				Bukkit.getPluginManager().callEvent( new StartCRTGEvent() );
				cancel();
				break;
			default:
				break;
		}

	}

	private String c( String s ) { return ChatColor.translateAlternateColorCodes( '&', s ); }

}
