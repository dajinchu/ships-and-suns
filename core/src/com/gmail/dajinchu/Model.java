package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gmail.dajinchu.net.MessageObserver;
import com.gmail.dajinchu.net.SocketManager;

/**
 * Created by Da-Jin on 3/26/2015.
 */
//TODO make abstract and merge the commanalities between spectate and host>
public interface Model extends MessageObserver {
    public enum GameState{STARTING,PLAYING,PAUSED};

    public void step(float timestep);
    public com.badlogic.gdx.utils.IntMap<? extends ObjectData> getShips();
    public Array<? extends ObjectData> getSuns();
    public int me();

    public GameState state();

    public Vector2 mapSize();
    public int worldFrame();
    public SocketManager socket();
}
