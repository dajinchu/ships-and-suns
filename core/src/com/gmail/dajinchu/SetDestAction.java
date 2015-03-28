package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Da-Jin on 1/18/2015.
 */
//FutureAction that sets destination of a player in model.
public class SetDestAction implements FutureAction {

    private final int frame;
    private final int playerid;
    private final Vector2 dest;
    private final Vector2 effectedCenter;
    private final float effectedRadiusSq;

    //cpu-saving TMP
    Ship tmp;

    public SetDestAction(int frame, int player, Vector2 dest, Vector2 effectedCenter, float effectedRadius){
        this.frame = frame;
        this.playerid = player;
        this.dest = dest;
        this.effectedCenter = effectedCenter;
        this.effectedRadiusSq = effectedRadius*effectedRadius;
    }
    @Override
    public void execute(HostModel model) {
        int platoon = model.players[playerid].newPlatoon();
        for(int id : model.players[playerid].my_ships){
            tmp = model.allShips.get(id);
            if(tmp.pos.dst2(effectedCenter)<effectedRadiusSq){
                tmp.setDest(dest,platoon);
            }
        }
    }

    @Override
    public int getScheduledFrame() {
        return frame;
    }

    @Override
    public int getPlayerId() {
        return playerid;
    }
}
