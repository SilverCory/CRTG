package eu.ac3_servers.dev.crtg.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class WinCRTGEvent extends Event
{
	public static HandlerList handlers = new HandlerList();
	@Getter
	private final Player winner;

	public WinCRTGEvent( Player winner )
	{
		this.winner = winner;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override public HandlerList getHandlers()
	{
		return handlers;
	}
}
