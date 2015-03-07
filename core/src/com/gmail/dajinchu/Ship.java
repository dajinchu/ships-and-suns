package com.gmail.dajinchu;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.io.Serializable;

/**
 * Created by Da-Jin on 12/5/2014.
 */


public class Ship implements Serializable, Entity {


    static int collisions=0;


    Body body;
    boolean arrived = false, wanderArrived = true;//wanderArrived needs to be true when not arrived to trigger finding a new wander when arrived at Player destx
    private Vector2 wanderdest = new Vector2();//TODO doing now: Eventually give this back to give player control over individual ships
    Vector2 dest = new Vector2();
    Vector2 pos = new Vector2();
    private Vector2 desired = new Vector2();
    private Vector2 steer = new Vector2();
    //int color;

    int platoonNumber=0;

    //Hosting sun isn't technically necessary,
    //it just saves Ship from having to do a large number of pythag to every Sun to see if it hit one
    Sun hostingSun;

    boolean destroyed = false;

    //Temp stuff, local variables here to save memory
    double minx,maxx,miny,maxy,wanderxoffset;
    int newgridx,newgridy;
    static Ship temp;

    int id;

    Player my_owner;
    Model model;
    volatile private double  dist, speed, steerMagnitude, ratio;

    static int newGrid = 0, loopcount = 0, dead = 0, outOfBounds = 0;
    private int frame;

    public int mass = 1;
    public int radius;
    public final static int MAXMASS = 10;


    public static void collide(Ship ship1, Ship ship2){
        collisions++;
        //Gdx.app.log("Ship", "Colliding "+ship1.dumpInfo()+" and "+ship2.dumpInfo());

        //Ship1 is bigger id
        if(ship1.id<ship2.id) {
            temp = ship1;
            ship1 = ship2;
            ship2 = temp;
        }

        if(ship1.my_owner != ship2.my_owner){
            //Enemy ships colliding
            Gdx.app.log("Ship",ship1.id+" and "+ship2.id+" are not friendly");
            if(ship1.mass>ship2.mass){
                //Ship1 will have remainder
                ship1.setMass(ship1.mass-ship2.mass);
                ship1.model.killShip(ship2);//TODO make Model singleton
            }else if(ship2.mass>ship1.mass){
                ship2.setMass(ship2.mass-ship1.mass);
                ship2.model.killShip(ship1);
            }else if(ship2.mass==ship2.mass){
                ship1.model.killShip(ship1);
                ship2.model.killShip(ship2);
            }
        }else{
            Gdx.app.log("Ship",ship1.id+" and "+ship2.id+" are friendly");
            //Friendly ships colliding
            int combinedMass=ship1.mass+ship2.mass;
            //If they are too big to make one ship, just average them, and give larger id extra if odd
            if(combinedMass>MAXMASS){
                ship1.setMass((int) Math.ceil(combinedMass / 2f));
                ship2.setMass((int) Math.floor(combinedMass / 2f));
            }
            //If the two ships are small enough to combine and not exceed max...
            //Check to see who is larger so that the larger one sets the position
            else if(ship1.mass>ship2.mass){
                ship1.setMass(combinedMass);
                ship1.model.killShip(ship2);//TODO make Model singleton
            }else if(ship2.mass>ship1.mass){
                ship2.setMass(combinedMass);
                ship2.model.killShip(ship1);
            }else {
                //else just use ship1
                ship1.setMass(combinedMass);
                ship1.model.killShip(ship2);
            }
            //}
        }
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
        dest = new Vector2(body.getPosition());
        calcDestWithWander();

        //Sort of register the ship
        model.allShips.put(model.shipIdCount, this);
        model.shipIdCount++;
    }

    public void setDest(Vector2 newdest, int platoon){
        platoonNumber = platoon;
        dest = newdest;
        calcDestWithWander();
    }

    public void captureSun(Sun sun){
        hostingSun = sun;
    }

    public boolean tryingToCapture(){
        //Ship has not even contacted a sun! Can't cap if we aren't there.
        if(hostingSun==null)return false;
        //Only cap the sun if platoon is still trying to, this also means the player wants ship here,
        // it's not just random wander
        // ALSO ship has have dest inside sun, so it doesn't cap while flying by
        return !my_owner.platoonFinished.get(platoonNumber)&&dest.dst(hostingSun.pos)<=hostingSun.size/2;
    }
    public void completeObjective(){
        my_owner.platoonFinished.set(platoonNumber, true);
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
            if(tryingToCapture()) {
                hostingSun.consumeShip(this);
            }

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
        wanderdest.y = (int)(model.random.nextDouble()*maxy*2-maxy+dest.y);
        wanderdest.x = (int) (wanderxoffset+dest.x);
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
        body.setTransform(pos.add(steer),0);
        //body.applyLinearImpulse(steer, body.getWorldCenter(), true);
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

    public int setMass(int mass){
        this.mass = mass;
        radius = (int) (4*Math.sqrt(mass));
        Gdx.app.log("Ship"+id,"Setting mass to "+mass+" radius to "+radius);
        if(model.allShips.containsKey(id)){
            body.getFixtureList().get(0).getShape().setRadius(radius);
        }else{
            Gdx.app.log("Ship"+id,"WTF HAPPENED");
        }
        return 0;//TODO, should return overflow from a MAX ship size
    }

    public String dumpInfo(){
        return String.format("%d,%f,%f,%f,%d", mass,pos.y,wanderdest.x,wanderdest.y,my_owner.playerNumber);
    }
}
