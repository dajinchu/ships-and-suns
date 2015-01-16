package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 1/5/2015.
 */
public class SetDestAction implements FutureAction, Message {

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


    @Override
    public String serialize() {
        return String.format("%s,%s,%s,%s", frame,playerid,x,y);
    }

    public static SetDestAction deserialize(String[] args) {
        return new SetDestAction(Integer.parseInt(args[0]),Integer.parseInt(args[1]),
                Integer.parseInt(args[2]),Integer.parseInt(args[3]));
    }
}
