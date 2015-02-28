package com.gmail.dajinchu;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import java.util.HashMap;

/**
 * Created by Da-Jin on 2/1/2015.
 */
public final class FixtureDefFactory {

    private static CircleShape circle;
    private static HashMap<Integer, FixtureDef> circleFixtureDefs = new HashMap<Integer, FixtureDef>();

    private FixtureDefFactory(){

    }
    public static FixtureDef getCircleDef(int radius){
        if(circleFixtureDefs.containsKey(radius)){
            //If we already have this fixturedef
            //Gdx.app.log("FixtureDefFactory", "Great, we reused fixtureDef of radius "+radius);
            return circleFixtureDefs.get(radius);
        }
        //We haven't already created a circle of this radius, so make one:
        circle = new CircleShape();
        circle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;

        circleFixtureDefs.put(radius, fixtureDef);
        return fixtureDef;
    }

    public static void dispose(){
        circle.dispose();
    }
}
