package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class InGameScreen implements Screen, GestureDetector.GestureListener {

    //Screems
    MainGame game;

    //Classes that help make stuff work
    OrthographicCamera cam;
    SpriteBatch spriteBatch;
    ShapeRenderer shapeRenderer;
    static Random random;

    //Ships and Suns stuff
    Player[] players;
    Player me;
    ShipTile[][] grid;
    LinkedList<Ship> allShips =  new LinkedList<Ship>();//To save resources, preallocate some space

    //Ships and Suns CONSTANTS
    /*static final int WIDTH = 400;//TODO make this *map* w/h, annotate theses constants
    static final int HEIGHT = 400;*/
    static final int SHIP_NUM = 1000;//Ships per player
    static final double DEST_RADIUS = 50;
    static final double ENGAGEMENT_RANGE = 50;
    static final double TERMINAL_VELOCITY = 2;
    static final double MAX_FORCE = .1;

    //Cam
    int height, width;

    //Test
    float retarget = 0;

    int gridHeight, gridWidth;
    private Ship ship;
    private long start;
    private int drawShips;
    private int allShipRemove=0;

    public InGameScreen(MainGame game){
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        Gdx.input.setInputProcessor(new GestureDetector(this));

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        cam = new OrthographicCamera(width,height);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        gridHeight=(int) Math.ceil(height/ENGAGEMENT_RANGE);
        gridWidth=(int) Math.ceil(width/ENGAGEMENT_RANGE);
        grid = new ShipTile[gridHeight][gridWidth];
        for(int y=0; y < gridHeight; y++){
            for(int x = 0; x < gridWidth; x++){
                grid[y][x] = new ShipTile(x,y);
                Gdx.app.log("Making grid",x+" "+y);
            }
        }
        for(int y=0; y < gridHeight; y++){
            for(int x = 0; x < gridWidth; x++){
                grid[y][x].fillNeighbors(this);
            }
        }

        initWithSeed(TimeUtils.millis());
    }

    //Game Mechanic Functions
    public void initWithSeed(long randomSeed){
        //When received seed for random from server, take appropriate action
        random = new Random(randomSeed);
        players = new Player[2];//TODO TEMP, have send of num of playas
        players[0] = new Player(0);
        players[1] = new Player(1);
        me = players[0];
        int x,y;
        for(Player player : players){
            for(int i=0; i<SHIP_NUM; i++){
                x=random.nextInt(width);
                y=random.nextInt(height);
                spawnShip(player,x,y);
            }
        }
        players[0].setDest((int)random.nextDouble()*width, (int)random.nextDouble()*height);
        players[1].setDest((int)random.nextDouble()*width, (int)random.nextDouble()*height);
    }
    public void update(float delta){

        //Move em
        for(Ship ship : allShips){
            if(ship.destroyed){
                //allShips.remove(ship);
                //allShipRemove++;
                continue;
            }
            ship.frame();
        }
/*
        //After all moved, calc killing
        for(Ship ship : allShips){
            if(ship.destroyed){
                //NEEDED?
                allShipRemove++;
                allShips.remove(ship);
                continue;
            }
            ship.killFrame();
        }
        //Gdx.app.log("Time for killFrames", String.valueOf(TimeUtils.timeSinceMillis(start)));
        /*for(Iterator iterator = allShips.iterator(); iterator.hasNext();){
            ship = (Ship) iterator.next();
            if(ship.destroyed) {
                iterator.remove();
            }else{
                ship.frame();
            }
        }*/
        //Calculate kills
        for(Iterator iterator = allShips.iterator(); iterator.hasNext();){
            ship = (Ship) iterator.next();
            if(ship.destroyed) {
                iterator.remove();
                continue;
            }
            ship.killFrame();
        }

        /*
        for(Player player : players){
            player.killFrame();
        }*/
        retarget += delta;
        //System.out.println(retarget);
        if(retarget>5){
            //Gdx.app.log("SHIP", Ship.newGrid+" "+Ship.loopcount);
            players[1].setDest(random.nextInt(width), random.nextInt(height));
            retarget=0;
        }
    }
    public void spawnShip(Player owner, int x, int y){
        //May need to modify this eventually, Ship adds itself to grid and owner
        allShips.add(new Ship(x,y,owner,this));
    }


    @Override
    public void render(float delta) {
        start = TimeUtils.millis();
        cam.update();
        spriteBatch.setProjectionMatrix(cam.combined);
        shapeRenderer.setProjectionMatrix(cam.combined);

        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();

        drawShips = 0;

        //Draw all ships
        for(Ship ship: allShips){
            drawShips++;
            //spriteBatch.draw(player.texture,0,0);
            spriteBatch.draw(ship.my_owner.texture, (int) ship.x-ship.my_owner.textureXShift, (int) ship.y-ship.my_owner.textureYShift);
        }
        spriteBatch.end();
        //Draw destination circles
        if(players!=null) {
            for (Player player : players) {
                shapeRenderer.begin();
                shapeRenderer.circle(player.destx,player.desty, (float) DEST_RADIUS);
                shapeRenderer.end();
            }
        }
        //Gdx.app.log("Draw ships", drawShips+" "+Ship.dead+" "+allShipRemove);

        //Draw collision detection optimization grid borders
        shapeRenderer.begin();
        for(int h = 0; h < gridHeight; h++){
            for(int w = 0; w < gridWidth; w++){
                shapeRenderer.rect((float)(w*ENGAGEMENT_RANGE),(float)(h*ENGAGEMENT_RANGE),(float)(ENGAGEMENT_RANGE),(float)(ENGAGEMENT_RANGE));
                //Gdx.app.log("Rect",(float)(w*ENGAGEMENT_RANGE)+" "+(float)(h*ENGAGEMENT_RANGE)+" "+(float)((w+1)*ENGAGEMENT_RANGE)+" "+(float)((h+1)*ENGAGEMENT_RANGE));
            }
        }
        shapeRenderer.end();

        update(delta);
    }

    @Override
    public void resize(int w, int h) {
        this.width = Gdx.graphics.getWidth();
        this.height = Gdx.graphics.getHeight();
        cam = new OrthographicCamera(width,height);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    //Gestures
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        me.setDest(Gdx.input.getX(),height-Gdx.input.getY());
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        cam.translate(-deltaX,deltaY);
        cam.update();
        Gdx.app.log("GESTURES","PAN");
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
