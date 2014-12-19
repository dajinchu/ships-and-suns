package com.gmail.dajinchu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class MainGame extends Game {

    InGameScreen igScreen;
    Screen conScreen;

    public MainGame(Screen conScreen){
        this.conScreen = conScreen;
    }

	@Override
	public void create () {
        igScreen = new InGameScreen(this);
        setScreen(conScreen);
	}

    @Override
    public void dispose(){
        igScreen.dispose();
    }
}
