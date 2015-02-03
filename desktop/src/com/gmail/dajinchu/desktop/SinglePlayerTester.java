package com.gmail.dajinchu.desktop;

import com.badlogic.gdx.Game;
import com.gmail.dajinchu.InGameScreen;
import com.gmail.dajinchu.Model;
import com.gmail.dajinchu.net.SinglePlayerSocketManager;

/**
 * Created by Da-Jin on 2/2/2015.
 */
public class SinglePlayerTester extends Game {
    @Override
    public void create() {
        setScreen(new InGameScreen(this, Model.defaultModel(50,0),new SinglePlayerSocketManager()));
    }
}
