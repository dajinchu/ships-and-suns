package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Controller implements GestureDetector.GestureListener {
    //Controller handles input from player, and other players through sockets

    private final OrthographicCamera cam;
    Model model;
    Vector3 touch = new Vector3();
    float previousDistance, previousInitial;

    BufferedWriter writer;

    public Controller(Model model, OrthographicCamera camera, BufferedReader reader, BufferedWriter writer){
        this.model = model;
        this.cam = camera;
        new Thread(new SocketReceive(reader)).start();
        this.writer = writer;
        clamp();
    }

    public void setPlayerDest(int playerId, int x, int y){
        model.players[playerId].setDest(x,y);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        touch.set(x,y,0);
        cam.unproject(touch);

        setPlayerDest(model.me.playerNumber,(int)touch.x,(int)touch.y);
        new Thread(new SocketSend(model.me.playerNumber+","+(int)touch.x+","+(int)touch.y)).start();
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        cam.translate(-deltaX * cam.zoom, deltaY * cam.zoom);
        cam.update();
        clamp();
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if(previousInitial!=initialDistance){
            //Starting a new gesture
            //Just make previousDistance initial to avoid jumpiness from the previous gesture's values carrying over
            previousDistance=initialDistance;
        }
        cam.zoom*=(previousDistance/distance);
        clamp();
        Gdx.app.log("Zoom", initialDistance+" "+distance);
        //Set previous, as this frame has ended
        previousDistance = distance;
        previousInitial = initialDistance;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        Gdx.app.log("Pinch", initialPointer1+" "+initialPointer2+" "+pointer1+" "+pointer2);
        return false;
    }

    public void clamp(){
        float effectiveViewportWidth = cam.viewportWidth * cam.zoom;
        float effectiveViewportHeight = cam.viewportHeight * cam.zoom;

        cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, model.mapWidth / cam.viewportWidth);
        cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, model.mapWidth - effectiveViewportWidth / 2f);
        cam.position.y = MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, model.mapHeight - effectiveViewportHeight / 2f);
        cam.update();
        //Gdx.app.log("GESTURES",cam.viewportWidth+" "+cam.zoom);
    }

    class SocketSend implements Runnable{
        String msg;

        public SocketSend(String msg){
            this.msg = msg;
        }
        @Override
        public void run() {
            try {
                writer.write(msg+"\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class SocketReceive implements Runnable{
        BufferedReader reader;

        public SocketReceive(BufferedReader reader){
            this.reader = reader;
        }
        @Override
        public void run() {
            String line;
            String[] line_split;
            while(true){
                //Gdx.app.log("Receive", "Checking for more on ufferedREader");
                try{
                    if((line = reader.readLine())!=null){
                        line_split = line.split(",");
                        setPlayerDest(Integer.parseInt(line_split[0]),Integer.parseInt(line_split[1]),Integer.parseInt(line_split[2]));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
