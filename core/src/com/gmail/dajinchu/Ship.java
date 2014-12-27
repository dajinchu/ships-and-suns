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
    int wanderdestx, wanderdesty;//Eventually give this back to give player control over individual ships
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
    volatile private double desiredx, desiredy, dist, speed, steeringx,steeringy, steerMagnitude, ratio;

    static int newGrid = 0, loopcount = 0, dead = 0;
    Vector2 pos;

    public Ship(Player owner, int id, Model model, Body b){
        my_owner = owner;
        this.model = model;
        this.id = id;
        my_owner.my_ships.add(id);
        this.body = b;
    }


    public void frame(){
        pos = body.getLocalCenter();//TODO check if I can do this only once, and the vector will update?
        arrive();

        //TODO efficiency here

        //Gdx.app.log("SHIP", newgridx + " " + my_tile.x+" "+newgridy+" "+my_tile.y);
        loopcount++;

        //Have we arrived and needing a new wanderdest?
        //Dont need fancy pthagorean, just reach threshold, pythag hardly applies this close
        if(Math.abs(wanderdestx-pos.x)+Math.abs(wanderdesty-pos.y)<InGameScreen.ENGAGEMENT_RANGE){
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
        //Range after each operation, x=dest_radius: (interval notation)
        //                                  [0,1]  ->         [0,x] ->        [0,2x]->  [-x,x]
        wanderxoffset = (int) (model.random.nextDouble()*InGameScreen.DEST_RADIUS*2-InGameScreen.DEST_RADIUS);
        maxy = Math.sqrt((InGameScreen.DEST_RADIUS*InGameScreen.DEST_RADIUS)-(wanderxoffset*wanderxoffset));
        wanderdesty = (int)(model.random.nextDouble()*maxy*2-maxy+originy);
        wanderdestx = (int) (wanderxoffset+originx);
    }

    public void arrive(){
        Vector2 currentVel = body.getLinearVelocity();
        float xVel = currentVel.x;
        float yVel = currentVel.y;
        desiredx = wanderdestx-pos.x;
        desiredy = wanderdesty-pos.y;

        dist = Math.sqrt(Math.pow(desiredx,2)+Math.pow(desiredy,2));//Magnitude of desired

        //m is speed to multiply
        if(dist<InGameScreen.DEST_RADIUS){
            //Closer we get, slower we get, its a proportion
            speed = InGameScreen.TERMINAL_VELOCITY * (dist /InGameScreen.DEST_RADIUS);
        } else{
            //Otherwise fast as possible
            speed = InGameScreen.TERMINAL_VELOCITY;
        }

        desiredx*=speed;
        desiredy*=speed;

        steeringx = desiredx-xVel;
        steeringy = desiredy-yVel;

        steerMagnitude = Math.sqrt(steeringx*steeringx+steeringy*steeringy);
        if(steerMagnitude>InGameScreen.MAX_FORCE){
            ratio = InGameScreen.MAX_FORCE/steerMagnitude;
            steeringx*=ratio;
            steeringy*=ratio;
        }

        xVel += steeringx;
        yVel += steeringy;

        speed =Math.sqrt(xVel*xVel+yVel*yVel);
        if(speed>InGameScreen.TERMINAL_VELOCITY){
            ratio = InGameScreen.TERMINAL_VELOCITY/speed;;
            xVel*=ratio;
            yVel*=ratio;
        }
        body.applyForceToCenter(2000, (float) steeringx, true);
        Gdx.app.log("SHIP",desiredx+" "+ dist+" "+ speed+ " "+steeringx+" "+steerMagnitude+" "+xVel);
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
