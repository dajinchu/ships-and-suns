package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

/**
 * Created by Da-Jin on 12/30/2014.
 */
//Like ship, it is class for the Box2D Sun body. ONLY FOR HOST
public class Sun extends ObjectData{
    Player occupant;
    HostModel model;

    private boolean occupied;

    //Instead of progress just going up to, say MAXCAP*2 as an upgrade, it goes up each time.
    //This makes it so stealing a sun is always the same difficulty regardless of upgrade
    int progress=0;
    int level = 1;
    enum STATE{EMPTY, CAPTURING, CAPTURED, DECAPTURING, UPGRADING};
    STATE state = STATE.EMPTY;

    private static final int MAXCAP = 100;
    public static final int FIRSTRADIUS = 30;

    public Sun(int x, int y, HostModel model){
        //This is constructor for unoccupied suns
        model.createCircleBody(x,y,size/2, BodyDef.BodyType.StaticBody,true).setUserData(this);

        model.allSuns.add(this);

        this.pos = new Vector2(x,y);
        this.size = FIRSTRADIUS*2;
        this.model = model;
        spritekey=-1;//-1 is for not captured
        maxupgrade = 3;
    }

    public Sun(int x, int y, Player occupant,int initialPopulation, HostModel model){
        this(x,y,model);

        this.state = STATE.CAPTURED;
        this.occupant = occupant;
        this.progress = MAXCAP;
        if(initialPopulation>0){
            produceShip(initialPopulation);
        }
        setColorKey();
    }
    public void pulse(){
        //Gdx.app.log("Sun"," "+state);
        if(state == STATE.DECAPTURING || state == STATE.CAPTURED || state == STATE.UPGRADING){
            produceShip(level);
        }
    }
    public void produceShip(int mass){
        new Ship(occupant, model, mass, (int)pos.x, (int)pos.y);
    }
    public void consumeShip(Ship ship){
        InGameScreen.deternismFile.writeString("Sun "+ state+" "+ship.dumpInfo()+" progress="+progress+"\n",true);
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
                if(ship.my_owner==occupant){
                    state = STATE.UPGRADING;
                    progress=0;//Sort of hijack the capture system by setting progress to 0, this MIGHT cause issues, not sure
                    capture(ship);
                }else{
                    state=STATE.DECAPTURING;
                    decapture(ship);
                }break;
            case UPGRADING:
                if(ship.my_owner==occupant){
                    capture(ship);//It will set state to captured if done. That is OK, we can use it
                    if(state==STATE.CAPTURED){
                        upgrade();
                        Gdx.app.log("Sun", "Captured");
                    }
                }else{
                    state=STATE.DECAPTURING;
                    progress = MAXCAP;
                    decapture(ship);
                }break;
        }
        setColorKey();
        InGameScreen.deternismFile.writeString("Sun "+ state+" "+ship.dumpInfo()+" progress="+progress+"\n",true);
    }

    private void setColorKey() {
        //Set key for color
        switch (state) {
            case EMPTY:
            case CAPTURING:
                spritekey = -1;
                break;
            case CAPTURED:
            case DECAPTURING:
            case UPGRADING:
                if (occupant.playerNumber == 0) spritekey=0;
                if (occupant.playerNumber == 1) spritekey=1;
                break;
        }
    }

    private void upgrade(){
        level++;
        size+=5;
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
            //Good job ship! set platoon to done
            ship.completeObjective();
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
            //Good job ship! set platoon to done
            ship.completeObjective();
        }
    }
}
