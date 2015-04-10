package eu.ac3_servers.dev.crtg.listeners;

import com.sk89q.worldedit.MaxChangedBlocksException;
import eu.ac3_servers.dev.crtg.CRTGPlugin;
import eu.ac3_servers.dev.crtg.Worlds;
import eu.ac3_servers.dev.crtg.event.StartCRTGEvent;
import eu.ac3_servers.dev.crtg.event.WinCRTGEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class GameListener implements Listener
{
	private final CRTGPlugin plugin;

	private long startTime;

	public GameListener( CRTGPlugin plugin )
	{
		this.plugin = plugin;
	}

	/**
	 * Convert a millisecond duration to a string format
	 *
	 * @param millis A duration to convert to a string form
	 * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
	 */
	public static String getDurationBreakdown( long millis )
	{

		if ( millis < 1 ) return "a short amount of time!";

		long days = TimeUnit.MILLISECONDS.toDays( millis );
		millis -= TimeUnit.DAYS.toMillis( days );
		long hours = TimeUnit.MILLISECONDS.toHours( millis );
		millis -= TimeUnit.HOURS.toMillis( hours );
		long minutes = TimeUnit.MILLISECONDS.toMinutes( millis );
		millis -= TimeUnit.MINUTES.toMillis( minutes );
		long seconds = TimeUnit.MILLISECONDS.toSeconds( millis );

		StringBuilder sb = new StringBuilder();
		if ( days > 0 ) {
			sb.append( days ).append( " day" );
			if ( days > 1 ) sb.append( "s" );
		}
		if ( hours > 0 ) {
			if ( days > 0 ) sb.append( ", " );
			sb.append( hours ).append( " hour" );
			if ( hours > 1 ) sb.append( "s" );
		}
		if ( minutes > 0 ) {
			if ( hours > 0 || days > 0 ) sb.append( ", " );
			sb.append( minutes ).append( " minute" );
			if ( minutes > 1 ) sb.append( "s" );
		}
		if ( seconds > 0 ) {
			if ( minutes > 0 || hours > 0 || days > 0 ) sb.append( ", " );
			sb.append( seconds ).append( " second" );
			if ( seconds > 1 ) sb.append( "s" );
		}

		return ( sb.append( "." ).toString() );
	}

	@EventHandler
	public void onGameStart( StartCRTGEvent e )
	{

		try {
			Worlds.addWall();
			CRTGPlugin.D( "Added the wall around spawn." );
			Worlds.placeBeacon();
		} catch ( MaxChangedBlocksException ex ) {
			CRTGPlugin.getLog().log( Level.WARNING, "The max number of blocks was changed when removing the wall." );
		}

		startTime = System.currentTimeMillis();

		Worlds.getWorld().setPVP( plugin.getConfig().getBoolean( "allow-pvp", true ) );

	}

	@EventHandler
	public void onGameWin( WinCRTGEvent e )
	{
		Bukkit.broadcastMessage( c( "&a&l" + e.getWinner().getName() + " HAS PLACED THE GLOWSTONE ON THE BEACON!" ) );
		Bukkit.broadcastMessage( c( "&aThis game lasted a total of:" ) );
		Bukkit.broadcastMessage( c( "&c " + getDurationBreakdown( System.currentTimeMillis() - startTime ) ) );
		e.getWinner().getInventory().forEach( new Consumer<ItemStack>()
		{
			@Override public void accept( ItemStack itemStack )
			{
				itemStack.setType( Material.COOKIE );
				itemStack.setAmount( 9999 );
			}
		} );
		plugin.getServer().getScheduler().runTaskLater( plugin, new Runnable()
		{
			@Override public void run()
			{
				for ( Player player : Bukkit.getOnlinePlayers() ) {
					if ( !plugin.playerIsInGame( player ) ) continue;
					player.setHealth( -3 );
				}
			}
		}, 20 * 20 );

		Worlds.cleanup();

	}

	private String c( String s ) { return ChatColor.translateAlternateColorCodes( '&', s ); }

}
