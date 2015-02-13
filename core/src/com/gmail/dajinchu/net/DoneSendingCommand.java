package com.gmail.dajinchu.net;

import com.badlogic.gdx.Gdx;
import com.gmail.dajinchu.Controller;

/**
 * Created by Da-Jin on 2/9/2015.
 */
public class DoneSendingCommand extends Command{

    private final int frame;
    private final int player;

    public DoneSendingCommand(int frame, int player){
        this.frame = frame;
        this.player = player;
    }
    @Override
    public String serialize() {
        return String.format("2,%d,%d",frame,player);
    }

    @Override
    public void execute(Controller controller) {
        Gdx.app.log("Done setting Command","setting player "+player+" to done on frame "+frame);
        controller.model.turnBuffer.setDoneSending(frame,player);
    }
}
