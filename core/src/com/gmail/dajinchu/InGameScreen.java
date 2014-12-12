package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class InGameScreen implements Screen {

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

    //Ships and Suns CONSTANTS
    /*static final int WIDTH = 400;//TODO make this *map* w/h, annotate theses constants
    static final int HEIGHT = 400;*/
    static final int SHIP_NUM = 1000;//Ships per player
    static final double DEST_RADIUS = 50;
    static final double ENGAGEMENT_RANGE = 10;
    static final double TERMINAL_VELOCITY = 2;
    static final double MAX_FORCE = .1;

    //Cam
    int height, width;

    //Test
    float retarget = 0;

    ShipTile[][] grid;
    int gridHeight, gridWidth;

    public InGameScreen(MainGame game){
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

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
        players = new Player[2];//TODO TEMP, have sen3d of num of playas
        players[0] = new Player(0);
        players[1] = new Player(1);
        me = players[0];
        int x,y;
        for(Player player : players){
            for(int i=0; i<SHIP_NUM; i++){
                x=random.nextInt(width);
                y=random.nextInt(height);
                player.my_ships.add(new Ship(x,y,player, this));
            }
        }
        players[0].setDest((int)random.nextDouble()*width, (int)random.nextDouble()*height);
        players[1].setDest((int)random.nextDouble()*width, (int)random.nextDouble()*height);
    }

    @Override
    public void render(float delta) {
        cam.update();
        spriteBatch.setProjectionMatrix(cam.combined);
        shapeRenderer.setProjectionMatrix(cam.combined);

        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(players!=null) {
            for (Player player : players) {
                spriteBatch.begin();
                //spriteBatch.draw(player.texture,0,0);
                player.drawShips(spriteBatch);
                spriteBatch.end();

                shapeRenderer.begin();
                shapeRenderer.circle(player.destx,player.desty, (float) DEST_RADIUS);
                shapeRenderer.end();
            }
        }

        if(Gdx.input.justTouched()){
            me.setDest(Gdx.input.getX(),height-Gdx.input.getY());
            System.out.println(Gdx.input.getX());
        }

        update(delta);
    }

    public void update(float delta){

        //Move em
        for(Player player : players){
            player.frame();
        }/*
        for(Player player : players){
            player.killFrame();
        }*/
        retarget += delta;
        //System.out.println(retarget);
        if(retarget>5){
            players[1].setDest(random.nextInt(width), random.nextInt(height));
            retarget=0;
        }
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
}
