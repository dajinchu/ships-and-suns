package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
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

    private static final int MAXCAP = 100;

    enum STATE{EMPTY, CAPTURING, CAPTURED, DECAPTURING};
    STATE state = STATE.EMPTY;

    public static final int size = 60;

    public Sun(int x, int y, Model model){
        model.createCircleBody(x,y,size/2, BodyDef.BodyType.StaticBody,true).setUserData(this);

        model.allSuns.add(this);

        this.pos = new Vector2(x,y);
        this.model = model;

    }

    public Sun(int x, int y, Player occupant,int initialPopulation, Model model){
        this(x,y,model);

        this.state = STATE.CAPTURED;
        this.occupant = occupant;
        this.progress = MAXCAP;
        if(initialPopulation>0){
            produceShip(initialPopulation);
        }
    }
    public void pulse(){
        //Gdx.app.log("Sun"," "+state);
        if(state == STATE.DECAPTURING || state == STATE.CAPTURED){
            produceShip(1);
        }
    }
    public void produceShip(int mass){
        new Ship(occupant, model, mass, (int)pos.x, (int)pos.y);
    }
    public void consumeShip(Ship ship){
        Gdx.app.log("Sun", state+" "+ship.dumpInfo()+" progress="+progress);
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
                }else{
                    decapture(ship);
                }break;
            case CAPTURED:
                if(ship.my_owner!=occupant){
                    decapture(ship);
                    state=STATE.DECAPTURING;
                }break;
        }
    }
    private void capture(Ship ship){
        progress+=ship.mass;
        if(progress>MAXCAP){
            //If the ship overflows, setMass to refund the extra
            ship.setMass(progress - MAXCAP);
            progress=MAXCAP;
        }else{
            model.killShip(ship);
        }
        if(progress==MAXCAP){
            state=STATE.CAPTURED;
        }
    }
    private void decapture(Ship ship) {
        progress -= ship.mass;
        if (progress < 0) {
            //Took too much off, refund!
            ship.setMass(-progress);
            progress = 0;
        } else {
            model.killShip(ship);
        }
        if(progress==0){
            state=STATE.EMPTY;
        }
    }
}
