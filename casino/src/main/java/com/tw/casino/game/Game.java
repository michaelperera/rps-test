package com.tw.casino.game;

import java.util.Collection;

import com.tw.casino.actor.PlayerProfile;

public interface Game
{
    /**
     * The unique system generated identifier for a game.
     * @return
     */
    GameId getGameId();
    
    /**
     * The user friendly name of a game. 
     * @return
     */
    String getName();
    
    /**
     * Minimum number of players before the game can be allowed to execute.
     * @return
     */
    int requiredNumberOfPlayers();
    
    /**
     * Casino assigned fee for participating in the game.
     * @return
     */
    double entryFee();
    
    /**
     * Method to 'play' the game with the provided players. It returns the
     * winner profile.
     * 
     * @param players
     * @return
     */
    PlayerProfile executeGame(PlayerProfile[] players);
}
