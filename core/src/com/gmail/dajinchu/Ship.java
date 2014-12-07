package com.gmail.dajinchu;

import java.io.Serializable;

/**
 * Created by Da-Jin on 12/5/2014.
 */

public class Ship implements Serializable {
    double x, y;
    double xVel=0,yVel=0;
    boolean arrived = false, wanderArrived = true;//wanderArrived needs to be true when not arrived to trigger finding a new wander when arrived at Player destx
    int wanderdestx, wanderdesty;//Eventually give this back to give player control over individual ships
    //int color;

    //Temp stuff, local variables here to save memory
    double minx,maxx,miny,maxy,wanderxoffset;

    Player my_owner;
    InGameScreen inGame;
    volatile private double desiredx, desiredy, dist, speed, steeringx,steeringy, steerMagnitude, ratio;

    public Ship(int x, int y, Player owner, InGameScreen inGame){
        this.x = x;
        this.y = y;
        my_owner = owner;
        this.inGame = inGame;
    }



    public void frame(){
        arrive();
        //Have we arrived and needing a new wanderdest?
        //Dont need fancy pthagorean, just reach threshold, pythag hardly applies this close
        if(Math.abs(wanderdestx-x)+Math.abs(wanderdesty-y)<2){
            calcDestWithWander(my_owner.destx,my_owner.desty);
        }
        Ship target = getTarget();
        if(target != null){
            //Not iterating through enemies ships right now, so just remove!
            target.my_owner.my_ships.remove(target);//TODO maybbe just iterate through a clone and remove from real?
            //Have to flag for removal, since my_owner is iterating through his my_ships
            my_owner.remove_ships.add(this);
        }
    }

    public void calcDestWithWander(int originx, int originy){
        //Range after each operation, x=dest_radius: (interval notation)
        //                                  [0,1]  ->         [0,x] ->        [0,2x]->  [-x,x]
        wanderxoffset = (int) (InGameScreen.random.nextDouble()*InGameScreen.DEST_RADIUS*2-InGameScreen.DEST_RADIUS);
        maxy = Math.sqrt((InGameScreen.DEST_RADIUS*InGameScreen.DEST_RADIUS)-(wanderxoffset*wanderxoffset));
        wanderdesty = (int)(InGameScreen.random.nextDouble()*maxy*2-maxy+originy);
        wanderdestx = (int) (wanderxoffset+originx);
    }

    public void arrive(){
        desiredx = wanderdestx-x;
        desiredy = wanderdesty-y;

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

        x += xVel;
        y += yVel;
        //Log.i("SHIP",desiredx+" "+ dist+" "+ speed+ " "+steeringx+" "+steerMagnitude+" "+xVel);
    }

    public Ship getTarget(){
        //pre-calculation bounds for efficiency
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
    }
}
