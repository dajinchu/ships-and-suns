package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import java.util.Random;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Model {
    Player[] players;
    Player me;
    IntMap<Ship> allShips =  new IntMap<Ship>();
    long seed;
    Random random;


    int gridHeight, gridWidth, mapHeight, mapWidth;

    int shipIdCount = 0;

    //Memory saving fields, unfortunate to expand score this way, but not much choice
    private Ship tempship;

    World world;
    private float accumulator = 0;
    Array<Body> bodies = new Array<Body>();
    private Ship disShip;
    Body dest;
    Array<Ship> scheduleForDelete = new Array<Ship>();

    public Model(int mapWidth, int mapHeight, World world){
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.world=world;
        world.setContactListener(new ShipContactListener());
    }

    public void setSeed(long seed){
        this.seed = seed;
        random = new Random(seed);
    }

    public void initShipDistro(int numPlayers, int shipsPerPlayer){
        players = new Player[numPlayers];
        int x,y;
        for(int p = 0; p < numPlayers; p++){//use the "Player player: players" syntax?
            players[p] = new Player(p,this);
            for(int i = 0; i < shipsPerPlayer; i++){
                x=random.nextInt(mapWidth);
                y=random.nextInt(mapHeight);
                spawnShip(players[p],x,y);
            }
            dest = createCircleBody(0, 0, (float) InGameScreen.DEST_RADIUS, BodyDef.BodyType.StaticBody, true);
            players[p].setDest(random.nextInt(mapWidth), random.nextInt(mapHeight));
        }
        me = players[0];
    }
    public void update(float delta){
        float frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;
        while (accumulator >= 1/60f) {
            world.step(1/60f, 6, 2);
            accumulator -= 1/60f;
        }

        world.getBodies(bodies);

        for (Body b : bodies) {
            // Get the body's user data - in this example, our user
            // data is an instance of the Entity class
            Entity e = (Entity) b.getUserData();

            if (e != null) {
                e.frame();
            }
        }
        for(Ship ship:scheduleForDelete){
            if(bodies.contains(ship.body,true)){
                world.destroyBody(ship.body);
            }
            allShips.remove(ship.id);
        }

        scheduleForDelete.clear();
        //Gdx.app.log("Model", String.valueOf(scheduleForDelete.size));
/*
        for(IntMap.Entry<Ship> ship : allShips.entries()){
            if(ship.value.destroyed){
                continue;
            }
            ship.value.frame();
        }
        for(Iterator iterator = allShips.values();iterator.hasNext();){
            tempship = (Ship) iterator.next();
            if(tempship.destroyed) {
                iterator.remove();
                continue;
            }
            tempship.killFrame();
        }*/
    }

    public void spawnShip(Player player, int x, int y){
        // First we create a body definition
        Body body = createCircleBody(x,y,4, BodyDef.BodyType.DynamicBody, true);

        //Add Ship userData to do the moving around stuff
        disShip = new Ship(player,shipIdCount,this,body);
        body.setUserData(disShip);
        allShips.put(shipIdCount, disShip);
        shipIdCount++;
    }

    //Getter-Setter
    public Ship getShip(int id){
        return allShips.get(id);
    }
    public Body createCircleBody(int x, int y, float radius, BodyDef.BodyType type, boolean isSensor){
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = type;
        // Set our body's starting position in the world
        bodyDef.position.set(x,y);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(radius);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setSensor(isSensor);

        // Remember to dispose of any shapes after you're done with them!
        // BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();
        return body;
    }

    public void killShip(Ship ship){
        scheduleForDelete.add(ship);
        allShips.remove(ship.id);
    }
    public class ShipContactListener implements ContactListener {
        private Object aData;
        private Object bData;

        @Override
        public void beginContact(Contact contact) {
            aData = contact.getFixtureA().getBody().getUserData();
            bData = contact.getFixtureB().getBody().getUserData();
            //Preventing double deletion, as well as contacts where one fixture is already dead from a previous contact
            if (scheduleForDelete.contains((Ship) aData, true)||scheduleForDelete.contains((Ship) bData,true))return;
            if(aData instanceof Ship && bData instanceof Ship){
                Gdx.app.log("ContactListener", "Ship "+((Ship) aData).id+" and "+((Ship) bData).id+" are getting deleted");
                killShip((Ship) aData);
                killShip((Ship) bData);
            }
        }

        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }
}
