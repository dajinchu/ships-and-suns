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

    public Sun(int x, int y, Player occupant,int initialPopulation, Model model){
        model.createCircleBody(x,y,20, BodyDef.BodyType.StaticBody,true);

        this.pos = new Vector2(x,y);
        this.occupant = occupant;
        this.model = model;
        produceShip();
    }
    public void produceShip(){
        new Ship(occupant, model, (int)pos.x, (int)pos.y);
    }
}
