package com.tw.casino.dataloader;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tw.casino.connection.messages.data.DealerGameDetails;
import com.tw.casino.connection.messages.data.GameDetails;
import com.tw.casino.util.Constants;

public class DefaultGameDataLoader implements GameDataLoader
{
    private final List<DealerGameDetails> dealerGameStore;
    
    private final List<GameDetails> playerGameStore;
    
    public DefaultGameDataLoader()
    {
        this.dealerGameStore = new CopyOnWriteArrayList<>();
        this.playerGameStore = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public void loadConfiguredGames()
    {
        GameDetails gameDetails = new GameDetails(Constants.RPS, 5.0);
        DealerGameDetails dealerGameDetails = new DealerGameDetails(Constants.RPS, 5.0, 2);
        
        this.dealerGameStore.add(dealerGameDetails);
        this.playerGameStore.add(gameDetails);    
    }

    @Override
    public List<DealerGameDetails> getGames()
    {
        return dealerGameStore;
    }

    @Override
    public List<GameDetails> availableGames()
    {
        return this.playerGameStore;
    }
}
