package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Model {
    FileHandle file;
    Player[] players;
    Player me;
    IntMap<Ship> allShips =  new IntMap<Ship>();
    Array<Sun> allSuns = new Array<Sun>();
    long seed;
    Random random;


    int gridHeight, gridWidth, mapHeight, mapWidth;

    int shipIdCount = 0, randomcalls = 0;

    //Memory saving fields, unfortunate to expand score this way, but not much choice
    private Ship tempship;

    World world;
    private float accumulator = 0;
    Array<Body> bodies = new Array<Body>();
    private Ship disShip;
    Array<Ship> scheduleForDelete = new Array<Ship>();

    float spawnAccumulator=0;
    int worldFrame = 0;

    Queue<FutureAction> actionQueue = new LinkedList<FutureAction>();
    FutureAction nextAction;

    public Model(int mapWidth, int mapHeight, World world){
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.world=world;
        world.setContactListener(new ShipContactListener());
        file = Gdx.files.external("snapshots"+new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date())+".txt");
    }

    public void setSeed(long seed){
        this.seed = seed;
        random = new Random(50);
    }
    public void initSunDistro(){
        //new Sun(100,100,players[0],0,this);
    }

    public void initShipDistro(int numPlayers, int player_id, int shipsPerPlayer){
        players = new Player[numPlayers];
        int x,y;
        for(int p = 0; p < numPlayers; p++){//use the "Player player: players" syntax?
            players[p] = new Player(p,this);
            for(int i = 0; i < shipsPerPlayer; i++){
                x=random.nextInt(mapWidth);
                y=random.nextInt(mapHeight);
                new Ship(players[p],this,x,y);
            }
            players[p].dest = createCircleBody(0, 0, (float) InGameScreen.DEST_RADIUS, BodyDef.BodyType.StaticBody, true);
            players[p].setDest(200,200);
        }
        me = players[player_id];
    }
    public void update(float delta){
        float frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;
        while (accumulator >= 1/60f) {
            world.step(1/60f, 6, 2);
            accumulator -= 1/60f;

            nextAction = actionQueue.peek();
            while(nextAction!=null && nextAction.getScheduledFrame() == worldFrame){
                nextAction.execute(this);
                actionQueue.remove();
                nextAction = actionQueue.peek();
            }
            for (IntMap.Entry<Ship> entry : allShips.entries()) {
                tempship = entry.value;
                tempship.frame();
            }
            try{
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(Ship ship:scheduleForDelete){
                if(bodies.contains(ship.body,true)){
                    world.destroyBody(ship.body);
                }
                allShips.remove(ship.id);
            }

            scheduleForDelete.clear();
            if(worldFrame%30==0) {
                file.writeString("\nFRAME "+worldFrame+"randomcalls: "+randomcalls+"\n",true);
                file.writeString(allShips.get(0).dumpInfo(),true);/*
                for (IntMap.Entry<Ship> entry : allShips.entries()) {
                    file.writeString(entry.value.pos.x + "," + entry.value.pos.y+"\n",true);
                }*/
            }
            worldFrame++;
        }



        spawnAccumulator+=frameTime;
        while(spawnAccumulator>=1){
            for(Sun sun : allSuns){
                sun.pulse();
            }
            spawnAccumulator-=1;
        }
        world.getBodies(bodies);
    }
    public static Model defaultModel(long seed, int player_id){
        Gdx.app.log("Client", "Seed: " + seed);
        Gdx.app.log("Client", "Player ID: " + player_id);


        //Box2D
        Box2D.init();
        World world = new World(new Vector2(0,0), true);
        world.setContinuousPhysics(false);

        Model model = new Model(1000,1000, world);
        model.setSeed(seed);
        model.initShipDistro(2, player_id, InGameScreen.SHIP_NUM);
        model.initSunDistro();
        return model;
    }
    //Getter-Setter
    public Ship getShip(int id){
        return allShips.get(id);
    }
    public void addFutureAction(FutureAction action){
        if(action.getScheduledFrame() < worldFrame){
            Gdx.app.log("Model","Tried to add future action, but its already in the past!..NOOOOO");
            return;
        }
        actionQueue.add(action);
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
            if(aData instanceof Ship && bData instanceof Ship){
                //Preventing friendly fire
                if(((Ship) aData).my_owner==((Ship) bData).my_owner)return;
                //Preventing double deletion, as well as contacts where one fixture is already dead from a previous contact
                if (scheduleForDelete.contains((Ship) aData, true)||scheduleForDelete.contains((Ship) bData,true))return;
                killShip((Ship) aData);
                killShip((Ship) bData);
            }
            //Ship to Sun contact
            if(aData instanceof Sun && bData instanceof Ship){
                ((Sun) aData).consumeShip((Ship) bData);
            }
            if(aData instanceof Ship && bData instanceof Sun){
                ((Sun) bData).consumeShip((Ship) aData);
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
