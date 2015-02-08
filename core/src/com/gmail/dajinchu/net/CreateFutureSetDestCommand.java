package com.gmail.dajinchu.net;

import com.badlogic.gdx.math.Vector2;
import com.gmail.dajinchu.Controller;
import com.gmail.dajinchu.SetDestAction;

/**
 * Created by Da-Jin on 1/5/2015.
 */
//Command to add a new SetDestAction to Model when executed
public class CreateFutureSetDestCommand extends Command {

    private final int frame;
    private final int playerid;
    private final Vector2 dest;
    private final Vector2 effectedCenter;
    private final float effectedRadius;


    public CreateFutureSetDestCommand(int frame, int player, Vector2 dest, Vector2 effectedCenter, float effectedRadius){
        this.frame = frame;
        this.playerid = player;
        this.dest = dest;
        this.effectedCenter = effectedCenter;
        this.effectedRadius = effectedRadius;
    }

    @Override
    public String serialize() {
        return String.format("0,%s,%s,%s,%s,%s,%s,%s", frame,playerid,dest.x,dest.y,effectedCenter.x,effectedCenter.y,effectedRadius);
    }

    @Override
    public void execute(Controller controller) {
        controller.model.addFutureAction(new SetDestAction(frame,playerid,dest,effectedCenter,effectedRadius));
    }
}
