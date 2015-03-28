package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 2/9/2015.
 */
public class Turn {
    private final int frame;
    private FutureAction[] actions = new FutureAction[2];//For now, one action per player per frame. How the hell could there be 2 from one player?

    public Turn(int frame){
        this.frame = frame;
    }
    public void addAction(FutureAction action){
        actions[action.getPlayerId()] = action;
    }
    public void execute(HostModel model){
        for(int i = 0; i < 2; i++){
            if(actions[i]!=null){
                actions[i].execute(model);
            }
        }
    }
}
