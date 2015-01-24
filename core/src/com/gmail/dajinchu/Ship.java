package com.gmail.dajinchu;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.io.Serializable;

/**
 * Created by Da-Jin on 12/5/2014.
 */


public class Ship implements Serializable, Entity {
    final Body body;
    boolean arrived = false, wanderArrived = true;//wanderArrived needs to be true when not arrived to trigger finding a new wander when arrived at Player destx
    Vector2 wanderdest = new Vector2();//Eventually give this back to give player control over individual ships
    Vector2 pos = new Vector2();
    Vector2 desired = new Vector2();
    Vector2 steer = new Vector2();
    //int color;

    boolean destroyed = false;

    //Temp stuff, local variables here to save memory
    double minx,maxx,miny,maxy,wanderxoffset;
    int newgridx,newgridy;
    Ship target;

    int id;

    Player my_owner;
    Model model;
    volatile private double  dist, speed, steerMagnitude, ratio;

    static int newGrid = 0, loopcount = 0, dead = 0, outOfBounds = 0;
    private int frame;

    public int mass = 1;
    public int radius;

    public static Ship collide(Ship ship1, Ship ship2){
        if(ship1.my_owner != ship2.my_owner){
            //Enemy ships colliding
            if(ship1.mass>ship2.mass){
                //Ship1 will have remainder
                return new Ship(ship1.my_owner, ship1.model, ship1.mass-ship2.mass, (int)(ship1.pos.x), (int)(ship1.pos.y));
            }
            if(ship2.mass>ship1.mass){
                return new Ship(ship2.my_owner, ship2.model, ship2.mass-ship1.mass, (int)ship2.pos.x, (int)ship2.pos.y);
            }
            if(ship2.mass==ship2.mass){
                return null;
            }
        }else{
            //Friendly ships colliding
            //if (ship1.my_owner.my_ships.size>100){
                //Enough exist
                return new Ship(ship1.my_owner, ship1.model, ship1.mass+ship2.mass, (int)(ship1.pos.x), (int)(ship1.pos.y));
            //}
        }
        return null;
    }

    public Ship(Player owner, Model model, int x, int y){
        this(owner,model,1,x,y);
    }

    public Ship(Player owner, Model model, int mass, int x, int y){//TODO make Model Singleton
        if(mass<=0){
            throw new RuntimeException("The mass of a ship reached "+mass+". How's that even possible!?");
        }
        this.mass = mass;
        radius = (int) (4*Math.sqrt(mass));

        this.body = model.createCircleBody(x,y, radius, BodyDef.BodyType.DynamicBody, true);

        //Add Ship userData to do the moving around stuff
        my_owner = owner;
        this.model = model;
        this.id = model.shipIdCount;
        my_owner.my_ships.add(id);
        body.setUserData(this);

        //Otherwise it will go to 0,0
        calcDestWithWander();
        model.allShips.put(model.shipIdCount, this);
        model.shipIdCount++;
    }

    public void frame(){
        pos = body.getPosition();//TODO check if I can do this only once, and the vector will update?
        arrive();

        frame++;

        //TODO efficiency here

        //Gdx.app.log("SHIP", newgridx + " " + my_tile.x+" "+newgridy+" "+my_tile.y);
        loopcount++;
        //Have we arrived and needing a new wanderdest?
        if(wanderdest.dst(pos)<5){
            calcDestWithWander();
        }

    }

/*    public void killFrame(){
        target = getTarget();
        if(target != null){
            target.die();
            this.die();
        }
    }
*/
    public void calcDestWithWander(){
        //Range after each operation, x=dest_radius: (interval notation)
        //                                  [0,1]  ->         [0,x] ->        [0,2x]->  [-x,x]
        wanderxoffset = (int) (model.random.nextDouble()*InGameScreen.DEST_RADIUS*2-InGameScreen.DEST_RADIUS);
        maxy = Math.sqrt((InGameScreen.DEST_RADIUS*InGameScreen.DEST_RADIUS)-(wanderxoffset*wanderxoffset));
        wanderdest.y = (int)(model.random.nextDouble()*maxy*2-maxy+my_owner.dest.getWorldCenter().y);
        wanderdest.x = (int) (wanderxoffset+my_owner.dest.getWorldCenter().x);
        model.randomcalls++;
        //Gdx.app.log("Ship", "Calcdestwithwander"+originx+" "+originy+" "+wanderdest.x+" "+wanderdest.y);
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
        body.applyLinearImpulse(steer, body.getWorldCenter(), true);
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

    public String dumpInfo(){
        return String.format("%f,%f,%f,%f    %d", pos.x,pos.y,wanderdest.x,wanderdest.y, frame);
    }
}
