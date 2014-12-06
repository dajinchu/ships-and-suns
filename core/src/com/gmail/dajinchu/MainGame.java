package com.gmail.dajinchu;

import com.badlogic.gdx.Game;

public class MainGame extends Game {

    InGameScreen igScreen;

	@Override
	public void create () {
        igScreen = new InGameScreen(this);
        setScreen(igScreen);
	}

    @Override
    public void dispose(){
        igScreen.dispose();
    }
}
