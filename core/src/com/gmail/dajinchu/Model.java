package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;
import java.util.Scanner;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Model {
    Player[] players;
    Player me;
    IntMap<Ship> allShips =  new IntMap<Ship>();
    Array<Sun> allSuns = new Array<Sun>();
    long seed;
    Random random;
    private Object aData, bData;

    public enum GameState{STARTING,PLAYING,PAUSED};

    public GameState state = GameState.STARTING;

    int gridHeight, gridWidth, mapHeight, mapWidth;

    int shipIdCount = 0, randomcalls = 0;

    //Memory saving fields, unfortunate to expand scope this way, but not much choice
    private Ship tempship;

    World world;
    private float accumulator = 0;
    Array<Body> bodies = new Array<Body>();
    private Ship disShip;
    Array<ContactUserData> contacts = new Array<ContactUserData>();

    float spawnAccumulator=0;
    public static int worldFrame = 0;

    StringBuilder delete = new StringBuilder();

    public TurnBuffer turnBuffer = new TurnBuffer();

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
    public void initSunDistro(){
        for(int s = 0; s< 6; s++){
            new Sun(random.nextInt(mapWidth),random.nextInt(mapHeight),this);
        }
        for(int s = 0; s< 1; s++){
            new Sun(random.nextInt(mapWidth),random.nextInt(mapHeight),players[1],500,this);
        }
        for(int s = 0; s< 4; s++){
            new Sun(random.nextInt(mapWidth),random.nextInt(mapHeight),players[0],0,this);
        }
    }
    public void initPlayerDistro(int numPlayers, int player_id){
        //Make the Player array
        players = new Player[numPlayers];
        //Instantiate the players
        for(int p =0; p<numPlayers; p++){
            players[p] = new Player(p,this);
        }
        //Define which one is "me," the person on this device
        me = players[player_id];
    }
    public void initShipDistro(int shipsPerPlayer){
        int x,y;
        for(int p = 0; p < players.length; p++){//use the "Player player: players" syntax?
            for(int i = 0; i < shipsPerPlayer; i++){
                x=random.nextInt(mapWidth);
                y=random.nextInt(mapHeight);
                new Ship(players[p],this,x,y);
            }
            //players[p].setDest(200,200);
        }
    }
    public void step(float timestep){
        Gdx.app.log("Model", "world stepping");
        world.step(1/60f, 6, 2);
        //Gdx.app.log("Model", "updating");

        world.getBodies(bodies);
        if(worldFrame>100) {
            turnBuffer.executeFrame(this, worldFrame);
        }

        for (IntMap.Entry<Ship> entry : allShips.entries()) {
            tempship = entry.value;
            tempship.frame();
        }
        delete.setLength(0);
        for(ContactUserData contact : contacts){
            aData = contact.a;
            bData = contact.b;
            if(aData instanceof Ship && bData instanceof Ship){
                Gdx.app.log("Contact Cycle", ((Ship) aData).id+", "+((Ship) bData).id+" checking number "+contacts.indexOf(contact, true)+"/"+contacts.size);
                if(!allShips.containsKey(((Ship) aData).id)||!allShips.containsKey(((Ship) bData).id))continue;
                Ship.collide((Ship)aData,(Ship)bData);
            }
            //Ship to Sun contact
            else if(aData instanceof Sun && bData instanceof Ship){
                ((Ship) bData).captureSun((Sun) aData);
            }else if(aData instanceof Ship && bData instanceof Sun){
                ((Ship) aData).captureSun((Sun) bData);
            }
        }
        contacts.clear();
        //if(worldFrame%30==0) {
        /*for (IntMap.Entry<Ship> entry : allShips.entries()) {
                file.writeString(entry.value.pos.x + "," + entry.value.pos.y+"\n",true);
            }*/
        //}
        worldFrame++;

        spawnAccumulator+=timestep;
        while(spawnAccumulator>=1){
            for(Sun sun : allSuns){
                sun.pulse();
            }
            spawnAccumulator-=1;
        }
    }

    //Getter-Setter
    public Ship getShip(int id){
        return allShips.get(id);
    }
    public void addFutureAction(FutureAction action){
        InGameScreen.file.writeString("\ngot future action set to happen at " +action.getScheduledFrame()+". Current frame is "+worldFrame+"\n", true);
        if(action.getScheduledFrame() < worldFrame){
            Gdx.app.log("Model","Tried to add future action, but its already in the past!..NOOOOO");
            return;
        }
        turnBuffer.addAction(action);
    }
    public void setPlayerReady(int playerid){
        InGameScreen.file.writeString("player "+playerid+"is now ready\n", true);
        players[playerid].readyToPlay = true;

        //See if everyone is ready to start
        for(int i = 0; i < players.length; i++){
            if(!players[i].readyToPlay){
                InGameScreen.file.writeString("player "+i+"is not ready\n", true);
                return;
            }
        }
        state = GameState.PLAYING;
        InGameScreen.file.writeString("Setting GameState to PLAYING"+ "Time "+ TimeUtils.timeSinceMillis(InGameScreen.start)+"\n", true);
    }
    public int checkSum() {
        int sum = 0;
        for (Ship ship : allShips.values()) {
            sum+=ship.pos.x+ship.pos.y;
        }
        return sum;
    }

    public Body createCircleBody(int x, int y, float radius, BodyDef.BodyType type, boolean isSensor){
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = type;
        // Set our body's starting position in the world
        bodyDef.position.set(x,y);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        FixtureDef fixtureDef = FixtureDefFactory.getCircleDef((int)radius);
        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setSensor(isSensor);
        return body;
    }

    public void killShip(Ship ship){
        if(bodies.contains(ship.body,true)){
            ship.body.setUserData(null);
            world.destroyBody(ship.body);
            ship.body = null;
        }
        ship.my_owner.my_ships.removeValue(ship.id,false);
        allShips.remove(ship.id);
        delete.append(ship.id+",");
    }
    public class ShipContactListener implements ContactListener {
        private Object aData;
        private Object bData;

        @Override
        public void beginContact(Contact contact) {
            //Gdx.app.log("Model", "contact");
            //Preventing double deletion, as well as contacts where one ship is already dead from a previous contact
            if(contact.getFixtureA()==null||contact.getFixtureB()==null)return;
            aData = contact.getFixtureA().getBody().getUserData();
            bData = contact.getFixtureB().getBody().getUserData();
            contacts.add(new ContactUserData(aData,bData));
            //Gdx.app.log("Model", "contact proccessed");
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

    public static class ModelFactory{

        public static Model defaultModel(long seed, int player_id){
            Gdx.app.log("Client", "Seed: " + seed);
            Gdx.app.log("Client", "Player ID: " + player_id);

            Scanner map = new Scanner(Gdx.files.internal("map.txt").read()).useDelimiter(" ");

            int width = map.nextInt();
            int height = map.nextInt();
            int unoccupied = map.nextInt();
            int occupied = map.nextInt();

            //Box2D
            Box2D.init();
            World world = new World(new Vector2(0,0), true);
            world.setContinuousPhysics(true);

            Model model = new Model(width, height, world);
            model.setSeed(seed);
            model.initPlayerDistro(2,player_id);
            model.initShipDistro(100);
/*
            for(int i = 0; i < unoccupied; i++){
                new Sun(map.nextInt(),map.nextInt(),model);
            }
            for(int i = 0; i < occupied; i++){
                new Sun(map.nextInt(),map.nextInt(),model.players[map.nextInt()],map.nextInt(),model);
            }*/
            return model;
        }
    }
}
