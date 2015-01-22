package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

/**
 * Created by Da-Jin on 12/30/2014.
 */
public class Sun {
    Player occupant;
    Vector2 pos;
    Model model;

    private boolean occupied;

    int progress=0;

    enum STATE{EMPTY, CAPTURING, CAPTURED, DECAPTURING};
    STATE state = STATE.EMPTY;

    public static final int size = 60;

    public Sun(int x, int y, Player occupant,int initialPopulation, Model model){
        model.createCircleBody(x,y,size/2, BodyDef.BodyType.StaticBody,true).setUserData(this);

        model.allSuns.add(this);

        this.pos = new Vector2(x,y);
        this.occupant = occupant;
        this.model = model;
        produceShip();
    }
    public void pulse(){
        //Gdx.app.log("Sun"," "+state);
        if(state == STATE.DECAPTURING || state == STATE.CAPTURED){
            produceShip();
        }
    }
    public void produceShip(){
        new Ship(occupant, model, (int)pos.x, (int)pos.y).calcDestWithWander((int)occupant.dest.getWorldCenter().x,(int)occupant.dest.getWorldCenter().y);
    }
    public void consumeShip(Ship ship){
        //TODO safety ship==null check?
        switch (state){
            case EMPTY:
                occupant = ship.my_owner;
                state=STATE.CAPTURING;
                capture(ship);
                break;
            case CAPTURING: case DECAPTURING:
                if(ship.my_owner==occupant){
                    capture(ship);
                    if(progress>=50){
                        state=STATE.CAPTURED;
                    }
                }else{
                    decapture(ship);
                    if(progress<=0){
                        state=STATE.EMPTY;
                    }
                }break;
            case CAPTURED:
                if(ship.my_owner!=occupant){
                    decapture(ship);
                    state=STATE.DECAPTURING;
                }break;
        }
    }
    private void capture(Ship ship){
        progress++;
        model.killShip(ship);
    }
    private void decapture(Ship ship){
        progress--;
        model.killShip(ship);
    }
}
