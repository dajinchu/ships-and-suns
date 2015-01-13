package com.gmail.dajinchu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

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

    public void startGame(Model model, SocketManager socketManager){
        Gdx.app.log("MainGame","Starting Game");
        igScreen = new InGameScreen(this, model, socketManager);
        setScreen(igScreen);
        conScreen.dispose();
    }
}
