package com.gmail.dajinchu;

import com.badlogic.gdx.utils.IntMap;

/**
 * Created by Da-Jin on 2/13/2015.
 */
//Turn Buffer. Eventually may figure out slowing and speeding up timestep to keep buffer consistent
public class TurnBuffer {
    private IntMap<Turn> buffer = new IntMap<Turn>();
    private Turn temp;

    public TurnBuffer(){

    }
    private Turn getTurn(int frame){
        if(!buffer.containsKey(frame)){
            //Make new Turn if not already existing
            buffer.put(frame, new Turn(frame));
        }
        return buffer.get(frame);
    }
    public void addAction(FutureAction action){
        getTurn(action.getScheduledFrame()).addAction(action);
    }
    public void setDoneSending(int frame, int player){
        getTurn(frame).setPlayerDone(player);
    }
    public void executeFrame(Model model, int frame){
        temp = getTurn(frame);
        while (!temp.isDone()){
            InGameScreen.file.writeString("waiting on turn "+frame+"\n",true); //TODO Add exception for first 100 frames?
        }
        temp.execute(model);
        buffer.remove(frame);
    }
}
