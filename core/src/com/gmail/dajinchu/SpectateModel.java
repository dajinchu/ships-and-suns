package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gmail.dajinchu.net.Command;
import com.gmail.dajinchu.net.SocketManager;

/**
 * Created by Da-Jin on 3/26/2015.
 */
//Interpolates snapshots. Fits into InGameScreen same as HostModel.
public class SpectateModel implements Model{

    private GameState state = GameState.STARTING;

    public SpectateModel(Vector2 mapSize, SocketManager socketManager){
        socketManager.setMessageReceived(this);
    }
    @Override
    public void step(float timestep) {

    }

    @Override
    public Array<? extends ObjectData> getShips() {
        return null;
    }

    @Override
    public ObjectData[] getSuns() {
        return null;
    }

    @Override
    public int me() {
        return 0;
    }

    @Override
    public GameState state(){
        return state;
    }

    @Override
    public Vector2 mapSize() {
        return null;
    }

    @Override
    public int worldFrame() {
        return 0;
    }

    @Override
    public void update(Command msg) {

    }
}
