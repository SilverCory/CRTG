package eu.ac3_servers.dev.crtg;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 10/04/2015.
 */
public enum GameState
{

	INGAME, PREPARING, NONE;

	@Getter
	@Setter
	private static GameState currentState = GameState.NONE;

}
