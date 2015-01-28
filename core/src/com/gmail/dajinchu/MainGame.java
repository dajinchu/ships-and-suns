package com.gmail.dajinchu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import com.gmail.dajinchu.net.SocketManager;

public class MainGame extends Game {

    InGameScreen igScreen;
    ConnectScreen conScreen;

    public MainGame(ConnectScreen conScreen){
        this.conScreen = conScreen;
    }

	@Override
	public void create () {
        Gdx.input.setCatchBackKey(true);
        conScreen.mainGame = this;
        setScreen(conScreen);
	}

    @Override
    public void render(){
        super.render();
        if(Gdx.input.isKeyJustPressed(Input.Keys.BACK)){
            Gdx.app.log("MainGame","Pressed back button");
        }
    }

    @Override
    public void dispose(){
        igScreen.dispose();
    }

    public void startGame(Model model, SocketManager socketManager){
        Gdx.app.log("MainGame","Starting Game");
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = TimeUtils.millis();
                conScreen.dispose();
                Gdx.app.log("MaingGame", "Dispose conScreen took " + TimeUtils.timeSinceMillis(start));
            }
        }).start();
        igScreen = new InGameScreen(this, model, socketManager);
        setScreen(igScreen);
    }
}
