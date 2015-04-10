package eu.ac3_servers.dev.crtg.event;

import eu.ac3_servers.dev.crtg.CRTGPlugin;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public class StartCRTGEvent extends Event
{

	public static HandlerList handlers = new HandlerList();

	public StartCRTGEvent()
	{
		CRTGPlugin.D( "Start event called!" );
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
