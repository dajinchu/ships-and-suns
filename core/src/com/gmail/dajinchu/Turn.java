package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 2/9/2015.
 */
public class Turn {
    private final int frame;
    private boolean[] playersDone = new boolean[2];
    private FutureAction[] actions = new FutureAction[2];//For now, one action per player per frame. How the hell could there be 2 from one player?

    public Turn(int frame){
        this.frame = frame;
    }
    public void addAction(FutureAction action){
        actions[action.getPlayerId()] = action;
    }
    public void setPlayerDone(int playerid){
        InGameScreen.file.writeString("Turn "+frame+"is setting player "+playerid+" to 'done'\n", true);
        playersDone[playerid] = true;
    }
    public boolean isDone(){
        return playersDone[0]&&playersDone[1];
    }
    public void execute(Model model){
        for(int i = 0; i < 2; i++){
            if(actions[i]!=null){
                actions[i].execute(model);
            }
        }
    }
}
