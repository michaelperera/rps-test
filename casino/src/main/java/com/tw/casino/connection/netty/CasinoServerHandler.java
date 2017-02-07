package com.tw.casino.connection.netty;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tw.casino.actor.CasinoManager;
import com.tw.casino.connection.messages.GameDataRequest;
import com.tw.casino.connection.messages.GameDataResponse;
import com.tw.casino.connection.messages.GameListRequest;
import com.tw.casino.connection.messages.GameListResponse;
import com.tw.casino.connection.messages.GameRejectResponse;
import com.tw.casino.connection.messages.GameRequest;
import com.tw.casino.connection.messages.GameWaitResponse;
import com.tw.casino.connection.messages.Message;
import com.tw.casino.game.DealerGameDetails;
import com.tw.casino.game.Game;
import com.tw.casino.game.GameDetails;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class CasinoServerHandler extends SimpleChannelInboundHandler<Message>
{
    private CasinoManager casinoManager;

    private static final ConcurrentMap<UUID, Channel> DEALER_CHANNEL_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, Channel> PLAYER_CHANNEL_CACHE = new ConcurrentHashMap<>();

    public CasinoServerHandler(CasinoManager casinoManager)
    {
        this.casinoManager = casinoManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message request)
            throws Exception
    {
        Message response = null;
        if (request instanceof GameListRequest)
        {
            GameListRequest gameListRequest = (GameListRequest) request;
            List<GameDetails> gameDetailsList = casinoManager.getGameDetailsList();

            // Store the ChannelHandlerContext for player
            PLAYER_CHANNEL_CACHE.putIfAbsent(gameListRequest.getPlayerId(), ctx.channel());

            response = new 
                    GameListResponse(gameListRequest.getPlayerId(), gameDetailsList);
            
            ctx.write(response);
        }
        else if (request instanceof GameDataRequest)
        {
            GameDataRequest gameDataRequest = (GameDataRequest) request;

            casinoManager.registerDealer(gameDataRequest.getDealerId());

            // Store the ChannelHandlerContext for dealer
            DEALER_CHANNEL_CACHE.putIfAbsent(gameDataRequest.getDealerId(), ctx.channel());

            List<DealerGameDetails> gameData = casinoManager.getGameData();
            response = new GameDataResponse(gameDataRequest.getDealerId(), gameData);
            
            ctx.write(response);
        }
        else if (request instanceof GameRequest)
        {
            GameRequest gameRequest = (GameRequest) request;
            String name = gameRequest.getGameName();
            UUID assignedDealer = casinoManager.assignDealerForGame(name);
            Channel dealerContext = DEALER_CHANNEL_CACHE.get(assignedDealer);
            
            //response = new GameExecuteEvent(assignedDealer, gameRequest.getPlayerDetails(), name);
            
            // Forward to Dealer
            ChannelFuture future = dealerContext.writeAndFlush(gameRequest);
            future.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture arg0) throws Exception
                {
                }});
        }
        else if (request instanceof GameWaitResponse)
        {
            GameWaitResponse waitEvent = (GameWaitResponse) request;
            UUID playerId = waitEvent.getPlayerId();
            Channel playerContext = PLAYER_CHANNEL_CACHE.get(playerId);
            
            //response = new GameWaitResponse(playerId);
            
            // Forward to Player
            ChannelFuture future = playerContext.writeAndFlush(request);
            future.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture arg0) throws Exception
                {
                    //playerContext.flush();
                    
                }});           
        }
        else if (request instanceof GameRejectResponse)
        {
            GameRejectResponse waitEvent = (GameRejectResponse) request;
            UUID playerId = waitEvent.getPlayerId();
            Channel playerContext = PLAYER_CHANNEL_CACHE.get(playerId);
            
            //response = new GameRejectResponse(playerId);
            
            // Forward to Player
            ChannelFuture future = playerContext.writeAndFlush(waitEvent);
            future.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture arg0) throws Exception
                {
                    //playerContext.flush();
                    
                }});       
        }

        

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}
