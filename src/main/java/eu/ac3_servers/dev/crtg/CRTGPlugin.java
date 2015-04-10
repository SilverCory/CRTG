package eu.ac3_servers.dev.crtg;

import eu.ac3_servers.dev.crtg.commands.RTGCommand;
import eu.ac3_servers.dev.crtg.listeners.GameListener;
import eu.ac3_servers.dev.crtg.listeners.PlayerListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class CRTGPlugin extends JavaPlugin
{

	/**
	 * Debugging boolean. For debugging.
	 */
	private static boolean D = true;

	/**
	 * The logger. For logging.
	 */
	private static Logger log = Logger.getGlobal();

	/**
	 * "this" but very still.
	 */
	private static CRTGPlugin instance = null;

	/**
	 * The name of the world.
	 */
	private static String worldName;

	/**
	 * The list of players.
	 */
	private HashSet<UUID> uuidList;

	/**
	 * Static debug message printing.
	 *
	 * @param obj The objects to print.
	 */
	public static void D( Object... obj )
	{
		if ( !D ) return;
		StringBuilder sb = new StringBuilder( "[D]" );
		for ( Object o : obj ) sb.append( " | " ).append( o );
		getLog().log( Level.INFO, sb.toString() );
	}

	/**
	 * The getter for debugging.
	 *
	 * @return Is debugging on?
	 */
	public static boolean isD() { return D; }

	/**
	 * That's this.
	 *
	 * @return The instance.
	 */
	public static CRTGPlugin getInstnace() { return instance; }

	/**
	 * The logger for logging.
	 *
	 * @return A logger.
	 */
	public static Logger getLog() { return log; }

	/**
	 * Returns the current world name to delete it.
	 *
	 * @return WorldName
	 */
	public static String getWorldName()
	{
		return worldName;
	}

	/**
	 * Set the worldname if it's possible.
	 *
	 * @param worldName
	 */
	public static void setWorldName( String worldName )
	{
		if ( GameState.getCurrentState().equals( GameState.INGAME ) || GameState.getCurrentState()
				.equals( GameState.PREPARING ) ) { return; }
		CRTGPlugin.worldName = worldName;
	}

	/**
	 * Erm... What does this do?!
	 */
	@Override public void onEnable()
	{

		instance = this;
		log = getLogger();

		// Check if the config file doesn't exists.
		File configFile = new File( getDataFolder(), "config.yml" );
		if ( !configFile.exists() ) saveDefaultConfig();

		D = getConfig().getBoolean( "debug", true );

		getCommand( "rtg" ).setExecutor( new RTGCommand( this ) );

		getServer().getPluginManager().registerEvents( new PlayerListener( this ), this );
		getServer().getPluginManager().registerEvents( new GameListener( this ), this );

	}

	/**
	 * Does this happen when it gets turned on or off?
	 */
	@Override public void onDisable()
	{

		Worlds.cleanup();
		instance = null;
		D = true;
		log = null;

	}

	/**
	 * Is the player in the current game?
	 *
	 * @param player Player to check.
	 * @return If they are or not.
	 */
	public boolean playerIsInGame( Player player )
	{
		if ( !GameState.getCurrentState().equals( GameState.INGAME ) ) return false;
		return uuidList.contains( player.getUniqueId() );
	}

	/**
	 * Clear the list of players for a new game.
	 */
	public void clearPlayers() { uuidList = new HashSet<UUID>(); }

	/**
	 * Add a player to the list of in game players.
	 *
	 * @param player The player to add.
	 */
	public void addPlayer( Player player ) { uuidList.add( player.getUniqueId() ); }

	/**
	 * Delete a player because they raged.
	 *
	 * @param uuid The uuid of the player to remove.
	 */
	public void delPlayer( UUID uuid ) { uuidList.remove( uuid ); }

}
