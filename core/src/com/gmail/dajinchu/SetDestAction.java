package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 1/18/2015.
 */
//FutureAction that sets destination of a player in model.
public class SetDestAction implements FutureAction {

    private final int x,y;
    private final int frame;
    private final int playerid;


    public SetDestAction(int frame, int player, int x, int y){
        this.frame = frame;
        this.playerid = player;
        this.x = x;
        this.y = y;
    }
    @Override
    public void execute(Model model) {
        model.players[playerid].setDest(x,y);
    }

    @Override
    public int getScheduledFrame() {
        return frame;
    }
}
