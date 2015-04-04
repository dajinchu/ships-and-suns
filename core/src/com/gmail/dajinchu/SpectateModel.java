package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
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

    //We must stay in between prev and next for the frame
    Snapshot prev, next;
    IntMap<Snapshot> futurebuffer = new IntMap<Snapshot>();
    //Begin with null Snapshot
    Snapshot now = new Snapshot("0 0 0");

    //tmp vars
    private ObjectData nowship;
    private Vector2 pos;

    public SpectateModel(Vector2 mapSize, int player_id, SocketManager socketManager){
        socketManager.setMessageReceived(this);
        me = player_id;
        this.mapsize = mapSize;
        sm = socketManager;
    }
    @Override
    public void step(float timestep) {
        if(state!=GameState.PLAYING){
            return;
        }
        worldFrame++;

        InGameScreen.interpFile.writeString("stepping "+worldFrame+"\n",true);
        //As snaps get sent in, they go into futurebuffer
        //every frame, we try to see if there is an new update for this frame
        //if so, we move on to the next snap to be the "prev"
        //Also it will not need to interpolate this frame, it has the exact info already
        if(futurebuffer.containsKey(worldFrame)){
            InGameScreen.interpFile.writeString("new next prev "+worldFrame+"\n",true);
            if(next != null){
                prev=next;
            }
            next=futurebuffer.remove(worldFrame);
            if(prev!=null) {
                now.ships = prev.ships;
                now.suns = prev.suns;
            }
        }else{
            //No new updates, must continue to interpolate what we have now.
            InGameScreen.interpFile.writeString("Interpolating "+worldFrame+"\n",true);
            if(next!=null && prev != null){
                //Interpolate
                for(ObjectData prevship:prev.ships.values()){
                    if(!next.ships.containsKey(prevship.id)){
                        //The ship will die. Let's just kill it now eh?
                        now.ships.remove(prevship.id);
                        continue;
                    }
                    //Ship exists still. we can do this
                    ObjectData nextship = next.ships.get(prevship.id);
                    float alpha = (worldFrame-prev.frame)/(next.frame-prev.frame);

                    //Actual Interpolation:
                    pos = prevship.pos.lerp(nextship.pos,alpha);

                    //If the ship is in now, then we can just modify it's values
                    if(now.ships.containsKey(prevship.id)){
                        nowship = now.ships.get(prevship.id);
                        nowship.pos = pos;
                        nowship.size = nextship.size;
                    }else{
                        //Create new one
                        nowship = new ObjectData(pos,nextship.size,prevship.spritekey);
                        nowship.id=prevship.id;
                        now.ships.put(prevship.id,nowship);
                    }
                }
                //Suns should probably interpolate their progress, but it is less critical,
                //This might be satisfactory for now
                //now.suns = prev.suns;
            }
        }



    }

    @Override
    public IntMap<? extends ObjectData> getShips() {
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
        Snapshot tmp = new Snapshot(msg);
        InGameScreen.interpFile.writeString("received frame "+tmp.frame+"\n",true);
        futurebuffer.put(tmp.frame, tmp);
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
