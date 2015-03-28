package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gmail.dajinchu.net.MessageObserver;

/**
 * Created by Da-Jin on 3/26/2015.
 */
//TODO make abstract and merge the commanalities between spectate and host>
public interface Model extends MessageObserver {
    public enum GameState{STARTING,PLAYING,PAUSED};

    public void step(float timestep);
    public Array<? extends ObjectData> getShips();
    public ObjectData[] getSuns();
    public int me();

    public GameState state();

    public Vector2 mapSize();
    public int worldFrame();
}
