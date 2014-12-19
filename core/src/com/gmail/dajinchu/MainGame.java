package com.gmail.dajinchu;

import com.badlogic.gdx.Game;

public class MainGame extends Game {

    InGameScreen igScreen;
    ConnectScreen conScreen;

    public MainGame(ConnectScreen conScreen){
        this.conScreen = conScreen;
    }

	@Override
	public void create () {
        conScreen.mainGame = this;
        setScreen(conScreen);
	}

    @Override
    public void dispose(){
        igScreen.dispose();
    }

    public void startGame(){
        igScreen = new InGameScreen(this);
        setScreen(igScreen);
    }
}
