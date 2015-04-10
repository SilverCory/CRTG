package eu.ac3_servers.dev.crtg.listeners;

import eu.ac3_servers.dev.crtg.CRTGPlugin;
import eu.ac3_servers.dev.crtg.GameState;
import eu.ac3_servers.dev.crtg.Worlds;
import eu.ac3_servers.dev.crtg.event.WinCRTGEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class PlayerListener implements Listener
{

	private final CRTGPlugin plugin;

	public PlayerListener( CRTGPlugin plugin )
	{
		this.plugin = plugin;
		CRTGPlugin.D( "Initialised the PlayerListener." );
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerRespawn( PlayerRespawnEvent e )
	{
		if ( !plugin.playerIsInGame( e.getPlayer() ) ) return;
		if ( GameState.getCurrentState().equals( GameState.INGAME ) ) {
			e.setRespawnLocation( Worlds.getWorld().getSpawnLocation() );
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerOweie( EntityDamageEvent e )
	{
		if ( !( e.getEntity() instanceof Player ) ) return;
		Player player = ( (Player) e.getEntity() );
		if ( !plugin.playerIsInGame( player ) ) return;
		if ( Worlds.playerIsInSpawn( player ) ) e.setCancelled( true );
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerBed( PlayerBedEnterEvent e )
	{
		if ( !plugin.playerIsInGame( e.getPlayer() ) ) return;
		e.setCancelled( true );
		e.getPlayer().sendMessage( c( "&cFOOL! WE AIN'T GOT NO TIME FOR SLEEP!" ) );
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave( PlayerQuitEvent e )
	{
		final Player player = e.getPlayer();
		if ( !plugin.playerIsInGame( player ) ) return;
		e.setQuitMessage( c( "&c" + e.getPlayer().getName() + " rage quit." ) );
		final Location itemLocation = player.getLocation().add( 0, 4, 0 );
		player.getInventory().forEach( new Consumer<ItemStack>()
		{
			// Java 8 would be kinda sexy here..
			@Override public void accept( ItemStack itemStack )
			{
				ItemStack is = itemStack.clone();
				Worlds.getWorld().dropItem( itemLocation, is );
			}
		} );
		player.getInventory().clear();
		player.teleport( player.getBedSpawnLocation() );
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick( PlayerKickEvent e )
	{
		final Player player = e.getPlayer();
		if ( !plugin.playerIsInGame( player ) ) return;
		e.setLeaveMessage( c( "&c" + e.getPlayer().getName() + " was dragged out kicking and screaming." ) );
		final Location itemLocation = player.getLocation().add( 0, 4, 0 );
		player.getInventory().forEach( new Consumer<ItemStack>()
		{
			// Java 8 would be kinda sexy here..
			@Override public void accept( ItemStack itemStack )
			{
				ItemStack is = itemStack.clone();
				Worlds.getWorld().dropItem( itemLocation, is );
			}
		} );
		player.getInventory().clear();
		player.teleport( player.getBedSpawnLocation() );
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMine( PlayerPickupItemEvent e )
	{
		Player player = e.getPlayer();
		if ( !plugin.playerIsInGame( player ) ) return;
		if ( e.getItem().getItemStack().getType().equals( Material.GLOWSTONE_DUST ) ) {
			Bukkit.broadcastMessage( c( "&6&l" + player.getName() + " has glowstone dust! :o" ) );
			player.awardAchievement( Achievement.FLY_PIG );
			player.sendMessage( c( "&4&lQUICK. Get back and place it on top of the beacon!" ) );
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPlace( BlockPlaceEvent e )
	{
		if ( !plugin.playerIsInGame( e.getPlayer() ) ) return;
		if ( !e.getBlockPlaced().getType().equals( Material.GLOWSTONE ) ) return;
		Location loc = e.getBlockPlaced().getLocation().subtract( 0, 1, 0 );
		if ( loc.getBlock().getType().equals( Material.BEACON ) ) {
			plugin.getServer().getPluginManager().callEvent( new WinCRTGEvent( e.getPlayer() ) );
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPortal( PlayerPortalEvent e )
	{
		if ( !plugin.playerIsInGame( e.getPlayer() ) ) return;
		if ( e.getPlayer().getWorld().equals( Worlds.getWorld() ) ) {
			Location loc = e.getPlayer().getLocation();
			loc.setWorld( Worlds.getNetherWorld() );
			e.getPortalTravelAgent().findOrCreate( e.getPlayer().getLocation() );
		}
		else if ( e.getPlayer().getWorld().equals( Worlds.getNetherWorld() ) ) {
			Location loc = e.getPlayer().getLocation();
			loc.setWorld( Worlds.getNetherWorld() );
			e.getPortalTravelAgent().findOrCreate( e.getPlayer().getLocation() );
		}
		e.getPlayer().sendMessage( "WAT?!" );
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onMine( PlayerInteractEvent e )
	{
		if ( !e.getAction().equals( Action.LEFT_CLICK_BLOCK ) && !e.getAction().equals( Action.RIGHT_CLICK_BLOCK ) ) {
			return;
		}
		if ( !plugin.playerIsInGame( e.getPlayer() ) ) return;
		e.setCancelled( Worlds.locIsInSpawn( e.getClickedBlock().getLocation() ) );
	}

	private String c( String s ) { return ChatColor.translateAlternateColorCodes( '&', s ); }

}
