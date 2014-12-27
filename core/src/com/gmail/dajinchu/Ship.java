package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.Serializable;


/**
 * Created by Da-Jin on 12/5/2014.
 */

public class Ship implements Serializable, Entity {
    private final Body body;
    boolean arrived = false, wanderArrived = true;//wanderArrived needs to be true when not arrived to trigger finding a new wander when arrived at Player destx
    Vector2 wanderdest = new Vector2();//Eventually give this back to give player control over individual ships
    Vector2 pos = new Vector2();
    Vector2 desired = new Vector2();
    Vector2 steer = new Vector2();
    //int color;

    ShipTile my_tile;

    boolean destroyed = false;

    //Temp stuff, local variables here to save memory
    double minx,maxx,miny,maxy,wanderxoffset;
    int newgridx,newgridy;
    Ship target;

    int id;

    Player my_owner;
    Model model;
    volatile private double  dist, speed, steerMagnitude, ratio;

    static int newGrid = 0, loopcount = 0, dead = 0;

    public Ship(Player owner, int id, Model model, Body b){
        my_owner = owner;
        this.model = model;
        this.id = id;
        my_owner.my_ships.add(id);
        this.body = b;
    }


    public void frame(){
        pos = body.getPosition();//TODO check if I can do this only once, and the vector will update?
        arrive();

        //TODO efficiency here

        //Gdx.app.log("SHIP", newgridx + " " + my_tile.x+" "+newgridy+" "+my_tile.y);
        loopcount++;

        //Have we arrived and needing a new wanderdest?
        if(wanderdest.dst(pos)<InGameScreen.ENGAGEMENT_RANGE){
            calcDestWithWander(my_owner.destx, my_owner.desty);
        }

    }

    public void die(){
        destroyed = true;
        my_tile.ships.removeValue(id, false);//TODO anywhere else to do this so we can stay pure flag?
        dead++;
    }

/*    public void killFrame(){
        target = getTarget();
        if(target != null){
            target.die();
            this.die();
        }
    }
*/
    public void calcDestWithWander(int originx, int originy){
        Gdx.app.log("Ship", "Calcdestwithwander");
        //Range after each operation, x=dest_radius: (interval notation)
        //                                  [0,1]  ->         [0,x] ->        [0,2x]->  [-x,x]
        wanderxoffset = (int) (model.random.nextDouble()*InGameScreen.DEST_RADIUS*2-InGameScreen.DEST_RADIUS);
        maxy = Math.sqrt((InGameScreen.DEST_RADIUS*InGameScreen.DEST_RADIUS)-(wanderxoffset*wanderxoffset));
        wanderdest.y = (int)(model.random.nextDouble()*maxy*2-maxy+originy);
        wanderdest.x = (int) (wanderxoffset+originx);
    }

    public void arrive(){
        Vector2 currentVel = body.getLinearVelocity();
        desired.set(wanderdest).sub(pos);

        dist = desired.len();
        desired.nor();

        //m is speed to multiply
        if(dist<InGameScreen.DEST_RADIUS){
            //Closer we get, slower we get, its a proportion
            speed = InGameScreen.TERMINAL_VELOCITY * (dist /InGameScreen.DEST_RADIUS);
            desired.scl((float) speed);
        } else{
            //Otherwise fast as possible
            desired.scl((float) InGameScreen.TERMINAL_VELOCITY);
        }
        //Gdx.app.log("ship", speed+" "+dist);
        steer.set(desired).sub(currentVel);
        steer.limit((float) InGameScreen.MAX_FORCE);
        body.applyForceToCenter(steer, true);
    }

    /*public Ship getTarget(){
        //pre-calculation bounds for efficiency
        Ship ship;
        for(ShipTile shipTile : my_tile.neighbors){

            for(int id :shipTile.ships){
                ship = model.getShip(id);
                //Dont need to target our teammates, and getShip will return null if destroyed
                if(ship.my_owner == my_owner||ship==null) {
                    continue;
                }
                if(Math.abs(x-ship.x)+Math.abs(y-ship.y)<2){
                    return ship;
                }
            }
        }/*
        minx=x-InGameScreen.ENGAGEMENT_RANGE;
        miny=y-InGameScreen.ENGAGEMENT_RANGE;
        maxx=x+InGameScreen.ENGAGEMENT_RANGE;
        maxy=y+InGameScreen.ENGAGEMENT_RANGE;

        for(Player enemy: inGame.players){
            if(enemy != my_owner){
                for(Ship ship : enemy.my_ships){
                    if(minx<ship.x&&ship.x<maxx&&miny<ship.y&&ship.y<maxy){
                        if(Math.pow(ship.x-x,2)+Math.pow(ship.y-y,2)<InGameScreen.ENGAGEMENT_RANGE){
                            return ship;
                        }
                    }
                }
            }
        }
        return null;
    }*/
}
