package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Controller implements GestureDetector.GestureListener {
    //Controller handles input from player, and other players through sockets

    private final OrthographicCamera cam;
    Model model;

    public Controller(Model model, OrthographicCamera camera){
        this.model = model;
        this.cam = camera;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        model.me.setDest(Gdx.input.getX(), (int) (cam.viewportHeight-Gdx.input.getY()));
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        cam.translate(-deltaX,deltaY);
        cam.update();
        Gdx.app.log("GESTURES","PAN");
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
