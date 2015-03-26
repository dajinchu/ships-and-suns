package com.gmail.dajinchu;

import com.badlogic.gdx.utils.IntMap;

/**
 * Created by Da-Jin on 2/13/2015.
 */
//Turn Buffer. Eventually may figure out slowing and speeding up timestep to keep buffer consistent
public class TurnBuffer {
    private volatile IntMap<Turn> buffer = new IntMap<Turn>();
    private Turn temp;

    public TurnBuffer(){

    }
    private Turn getTurn(int frame){
        if(!buffer.containsKey(frame)){
            //Make new Turn if not already existing
            InGameScreen.file.writeString("frame "+frame+" was created", true);
            //Sometimes the InGameScreen thread and the Socket thread
            //make the same frame at the same time and make two different turns for the same frame
            buffer.put(frame, new Turn(frame));
        }
        return buffer.get(frame);
    }
    public void addAction(FutureAction action){
        getTurn(action.getScheduledFrame()).addAction(action);
    }
    public void executeFrame(Model model, int frame){
        temp = getTurn(frame);
        temp.execute(model);
        buffer.remove(frame);
    }
}
