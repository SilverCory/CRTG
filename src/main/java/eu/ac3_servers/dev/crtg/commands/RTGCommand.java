package eu.ac3_servers.dev.crtg.commands;

import com.sk89q.worldedit.MaxChangedBlocksException;
import eu.ac3_servers.dev.crtg.CRTGPlugin;
import eu.ac3_servers.dev.crtg.GameState;
import eu.ac3_servers.dev.crtg.Worlds;
import eu.ac3_servers.dev.crtg.event.StartCRTGEvent;
import eu.ac3_servers.dev.crtg.event.WinCRTGEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class RTGCommand implements CommandExecutor
{
	private final CRTGPlugin plugin;

	public RTGCommand( CRTGPlugin plugin )
	{
		this.plugin = plugin;
		CRTGPlugin.D( "Initialised the RTGCommand executor." );
	}

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
			Worlds.getNetherWorld().setPVP( plugin.getConfig().getBoolean( "allow-pvp", true ) );
			Worlds.getNetherWorld().setDifficulty( Difficulty.HARD );

			// Give the server a break.. It had a tough time you know!
			plugin.getServer().getScheduler().runTaskLater( plugin, new Runnable()
			{
				@Override public void run()
				{
					// Create the normal world.
					WorldCreator wc = new WorldCreator( CRTGPlugin.getWorldName() );
					wc.environment( World.Environment.NORMAL );

					Worlds.setWorld( wc.createWorld() );
					CRTGPlugin.D( "Normal world created." );

					Worlds.getWorld().setKeepSpawnInMemory( false );
					Worlds.getWorld().setDifficulty( Difficulty.HARD );
					Worlds.getWorld().setPVP( false );
					CRTGPlugin.D( "Defaults set." );

					Location loc = Worlds.getWorld().getSpawnLocation();
					Location point1 = loc.add( 7, 4, 7 );
					Location point2 = loc.subtract( 7, 4, 7 );

					Worlds.setSpawnPoint1( point1 );
					Worlds.setSpawnPoint2( point2 );

					try {
						Worlds.addWall();
						CRTGPlugin.D( "Added the wall around spawn." );
					} catch ( MaxChangedBlocksException e ) {
						CRTGPlugin.getLog()
								.log( Level.WARNING, "The max number of blocks was changed when creating the wall." );
					}

					Bukkit.broadcastMessage( c( "&9&lWorld generation complete." ) );
					plugin.getServer().getScheduler().runTaskTimer( plugin, new TeleportTask( plugin ), 0, 3 );
				}
			}, 20 );

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
		this.players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
		this.loc = Worlds.getWorld().getSpawnLocation();
	}

	@Override public void run()
	{


		for ( int j = 0; j < 3; j++ ) {
			
			if ( i >= players.length -1 ) {
				Bukkit.broadcastMessage( c( "&6&lStarting the game in 90 seconds!" ) );
				plugin.getServer().getScheduler().runTaskTimer( plugin, new CountDownTask( plugin ), 20, 20 );
				CRTGPlugin.D( "Ended the teleport task." );
				this.cancel();
				return;
			}
			
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
				CRTGPlugin.D( "Ended the CountDownTask." );
				this.cancel();
				break;
			default:
				break;
		}

	}

	private String c( String s ) { return ChatColor.translateAlternateColorCodes( '&', s ); }

}
