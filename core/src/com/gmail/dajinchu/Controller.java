package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gmail.dajinchu.net.Command;
import com.gmail.dajinchu.net.CreateFutureSetDestCommand;
import com.gmail.dajinchu.net.MessageObserver;
import com.gmail.dajinchu.net.ReadyToPlayCommand;
import com.gmail.dajinchu.net.SocketManager;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Controller implements GestureDetector.GestureListener, MessageObserver {
    //Controller handles input from player, and other players through sockets

    private final OrthographicCamera cam;
    public Model model;
    Vector3 touch = new Vector3();
    Vector2 delta = new Vector2();
    float previousDistance, previousInitial;

    private final SocketManager socketManager;
    private Vector2 previousPointer1, previousInitial2 = new Vector2(), previousInitial1 = new Vector2(), previousPointer2;

    public Controller(Model model, OrthographicCamera camera, SocketManager socketManager){
        this.model = model;
        this.cam = camera;
        this.socketManager = socketManager;
        socketManager.setMessageReceived(this);
        clamp();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if(model.state == Model.GameState.STARTING) {
            socketManager.start();
            setPlayerReady(model.me.playerNumber);
        }
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        touch.set(x, y, 0);
        cam.unproject(touch);
        socketManager.sendMsg(new CreateFutureSetDestCommand(model.worldFrame+100,model.me.playerNumber,(int)touch.x,(int)touch.y));
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    public void pan(float deltaX, float deltaY) {
        cam.translate(deltaX * cam.zoom, -deltaY * cam.zoom);
        cam.update();
        clamp();
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    public void zom(float initialDistance, float distance) {
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
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        try{
            Gdx.app.log("Pinch before",initialPointer1+" "+initialPointer2+" "+ previousPointer1+" "+previousPointer2+" "+pointer1+" "+pointer2+" "+delta.x+" "+delta.y);
        }catch (NullPointerException e){}
        if(!previousInitial1.equals(initialPointer1)&&!previousInitial2.equals(initialPointer2)){
            //Starting a new gesture
            //Just make previous initial to avoid jumpiness from the previous gesture's values carrying over
            previousPointer1 = initialPointer1;
            previousPointer2 = initialPointer2;
        }//*/

        zom(initialPointer1.cpy().dst(initialPointer2), pointer1.cpy().dst(pointer2));
        delta = previousPointer1.cpy().add(previousPointer2).scl(.5f).sub(pointer1.cpy().add(pointer2).scl(.5f));
        pan(delta.x,delta.y);
        Gdx.app.log("Pinch after", initialPointer1+" "+initialPointer2+" "+ previousPointer1+" "+previousPointer2+" "+pointer1+" "+pointer2+" "+delta.x+" "+delta.y);

        previousInitial1 = initialPointer1.cpy();
        previousInitial2 = initialPointer2.cpy();
        previousPointer1 = pointer1.cpy();
        previousPointer2 = pointer2.cpy();
        return true;
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

    public void setPlayerReady(int playerid){
        socketManager.sendMsg(new ReadyToPlayCommand(playerid));
    }

    @Override
    public void update(Command msg) {
        Gdx.app.log("Controller", msg.serialize());
        msg.execute(this);
    }
}
