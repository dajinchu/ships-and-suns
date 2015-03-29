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
import com.gmail.dajinchu.net.Command;
import com.gmail.dajinchu.net.SocketManager;
import com.gmail.dajinchu.net.SocketServerManager;

import java.util.Random;
import java.util.Scanner;

/**
 * Created by Da-Jin on 12/20/2014.
 */
//Model with Box2d and does collisions. Should be on Host device.
public class HostModel implements Model{
    private final SocketServerManager socketManager;
    Player[] players;
    Player me;
    IntMap<Ship> allShips =  new IntMap<Ship>();
    Array<Sun> allSuns = new Array<Sun>();
    long seed;
    Random random;
    private Object aData, bData;

    public GameState state = GameState.STARTING;

    Vector2 mapSize = new Vector2();

    int shipIdCount = 0, randomcalls = 0;

    //Memory saving fields, unfortunate to expand scope this way, but not much choice
    private Ship tempship;

    World world;
    private float accumulator = 0;
    Array<Body> bodies = new Array<Body>();
    private Ship disShip;
    Array<ContactUserData> contacts = new Array<ContactUserData>();

    public static int worldFrame = 0;

    StringBuilder delete = new StringBuilder();

    //Checksums
    double XY, totalmass, massID;

    public HostModel(Vector2 mapSize, World world, SocketServerManager socketManager){
        this.mapSize = mapSize;
        this.world=world;
        world.setContactListener(new ShipContactListener());
        this.socketManager = socketManager;
        socketManager.setMessageReceived(this);
    }

    public void setSeed(long seed){
        this.seed = seed;
        random = new Random(seed);
    }/*I don't think this is needed, it just randomly spawned suns
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
    }*/
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
                x=random.nextInt((int) mapSize.x);
                y=random.nextInt((int) mapSize.y);
                new Ship(players[p],this,x,y);
            }
            //players[p].setDest(200,200);
        }
    }
    public void step(float timestep){
        Gdx.app.log("HostModel", "world stepping");
        world.step(1/60f, 6, 2);
        //Gdx.app.log("HostModel", "updating");

        socketManager.sendSnap(new Snapshot(this));

        if(worldFrame%60==0) {
            for (Sun sun : allSuns) {
                sun.pulse();
            }
        }

        XY=totalmass=massID=0;
        for(Ship ship: allShips.values()){
            XY+=ship.pos.x+ship.pos.y;
            totalmass+=ship.mass;
            massID+=ship.mass*ship.id;
        }

        InGameScreen.checksumFile.writeString("Frame "+worldFrame+
                "\nXY-Checksum: "+XY+
                "\ntotalmass-Checksum: "+totalmass+
                "\nmass*ID-Checksum: "+massID+"\n"+Ship.collisions+" ship collisions\n",true);


        world.getBodies(bodies);
        /*if(worldFrame>100) {
            turnBuffer.executeFrame(this, worldFrame);
        }*/

        for (IntMap.Entry<Ship> entry : allShips.entries()) {
            tempship = entry.value;
            tempship.frame();
        }
        delete.setLength(0);
        InGameScreen.deternismFile.writeString("New frame"+worldFrame+"\n",true);
        for(ContactUserData contact : contacts){
            aData = contact.a;
            bData = contact.b;
            if(aData instanceof Ship && bData instanceof Ship){
                InGameScreen.deternismFile.writeString("Contact "+((Ship) aData).id+", "+((Ship) bData).id+"\n", true);
                Gdx.app.log("Contact Cycle", ((Ship) aData).id+", "+((Ship) bData).id+" checking number "+contacts.indexOf(contact, true)+"/"+contacts.size);
                if(!allShips.containsKey(((Ship) aData).id)||!allShips.containsKey(((Ship) bData).id))continue;
                Ship.collide((Ship)aData,(Ship)bData);
            }
            //Ship to Sun contact
            else if(aData instanceof Sun && bData instanceof Ship){
                InGameScreen.deternismFile.writeString("Contact "+((Ship) bData).id+", sun\n", true);
                ((Ship) bData).captureSun((Sun) aData);
            }else if(aData instanceof Ship && bData instanceof Sun){
                InGameScreen.deternismFile.writeString("Contact "+((Ship) aData).id+", sun\n", true);
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
    }

    @Override
    public Array<? extends ObjectData> getShips() {
        return allShips.values().toArray();
    }

    @Override
    public Array<? extends ObjectData> getSuns() {
        return allSuns;
    }

    @Override
    public int me() {
        return me.playerNumber;
    }

    @Override
    public GameState state() {
        return state;
    }

    @Override
    public Vector2 mapSize() {
        return mapSize;
    }

    @Override
    public int worldFrame() {
        return worldFrame;
    }

    @Override
    public SocketManager socket() {
        return socketManager;
    }

    @Override
    public void update(String msg) {
        InGameScreen.file.writeString("RECEIVED:"+msg, true);
        Command.deserialize(msg).execute(this);
    }

    //Getter-Setter
    public Ship getShip(int id){
        return allShips.get(id);
    }
    public void addFutureAction(FutureAction action){
        action.execute(this);
        /*InGameScreen.file.writeString("\ngot future action set to happen at " +action.getScheduledFrame()+". Current frame is "+worldFrame+"\n", true);
        if(action.getScheduledFrame() < worldFrame){
            Gdx.app.log("HostModel","Tried to add future action, but its already in the past!..NOOOOO");
            return;
        }
        turnBuffer.addAction(action);*/
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
        InGameScreen.file.writeString("Setting GameState to PLAYING" + "Time " + TimeUtils.timeSinceMillis(InGameScreen.start) + "\n", true);
    }
    public double checkSum() {
        double sum = 0;
        for (Ship ship : allShips.values()) {
            sum+=(ship.pos.x+ship.pos.y)*ship.mass*ship.id;
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
            //Gdx.app.log("HostModel", "contact");
            //Preventing double deletion, as well as contacts where one ship is already dead from a previous contact
            if(contact.getFixtureA()==null||contact.getFixtureB()==null)return;
            aData = contact.getFixtureA().getBody().getUserData();
            bData = contact.getFixtureB().getBody().getUserData();
            contacts.add(new ContactUserData(aData,bData));
            //Gdx.app.log("HostModel", "contact proccessed");
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

        public static HostModel defaultHostModel(long seed, int player_id, SocketServerManager socketManager){
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

            HostModel model = new HostModel(new Vector2(width, height), world, socketManager);
            model.setSeed(seed);
            model.initPlayerDistro(2,player_id);



            for(int i = 0; i < unoccupied; i++){
                new Sun(map.nextInt(),map.nextInt(),model);
            }
            for(int i = 0; i < occupied; i++){
                new Sun(map.nextInt(),map.nextInt(),model.players[map.nextInt()],map.nextInt(),model);
            }
            return model;
        }
    }
}
