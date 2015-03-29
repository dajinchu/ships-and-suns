package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gmail.dajinchu.net.SocketManager;

import java.util.Scanner;

/**
 * Created by Da-Jin on 3/26/2015.
 */
//Interpolates snapshots. Fits into InGameScreen same as HostModel.
public class SpectateModel implements Model{

    private final Vector2 mapsize;
    private GameState state = GameState.STARTING;
    int worldFrame=0;

    int me;

    SocketManager sm;

    //Begin with null Snapshot
    Snapshot now = new Snapshot("0 0");

    public SpectateModel(Vector2 mapSize, int player_id, SocketManager socketManager){
        socketManager.setMessageReceived(this);
        me = player_id;
        this.mapsize = mapSize;
        sm = socketManager;
    }
    @Override
    public void step(float timestep) {
        worldFrame++;
    }

    @Override
    public Array<? extends ObjectData> getShips() {
        return now.ships;
    }

    @Override
    public Array<? extends ObjectData> getSuns() {
        return now.suns;//TODO convert this to getShips style possibly
    }

    @Override
    public int me() {
        return me;
    }

    @Override
    public GameState state(){
        return state;
    }

    @Override
    public Vector2 mapSize() {
        return mapsize;
    }

    @Override
    public int worldFrame() {
        return worldFrame;
    }

    @Override
    public SocketManager socket() {
        return sm;
    }

    @Override
    public void update(String msg) {
        state = GameState.PLAYING;
        now=new Snapshot(msg);
    }


    public static class ModelFactory {

        public static SpectateModel defaultSpectateModel(int player_id, SocketManager socketManager) {
            Scanner map = new Scanner(Gdx.files.internal("map.txt").read()).useDelimiter(" ");

            int width = map.nextInt();
            int height = map.nextInt();

            return new SpectateModel(new Vector2(width,height),player_id,socketManager);
        }
    }
}
