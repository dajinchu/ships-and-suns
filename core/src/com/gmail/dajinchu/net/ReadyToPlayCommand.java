package com.gmail.dajinchu.net;

import com.gmail.dajinchu.Controller;

/**
 * Created by Da-Jin on 1/20/2015.
 */
public class ReadyToPlayCommand extends Command{

    public int playerid;

    public ReadyToPlayCommand(int playerid){
        this.playerid = playerid;
    }
    @Override
    public String serialize() {
        return String.format("1,%s", playerid);
    }

    @Override
    public void execute(Controller controller) {
        controller.model.setPlayerReady(playerid);
    }
}
