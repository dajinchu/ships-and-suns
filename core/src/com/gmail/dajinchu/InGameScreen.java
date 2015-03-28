package com.gmail.dajinchu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.TimeUtils;
import com.gmail.dajinchu.net.SocketManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class InGameScreen implements Screen {

    private final Controller controller;
    //Screems
    Game game;

    //Classes that help make stuff work
    OrthographicCamera cam;
    SpriteBatch spriteBatch;
    ShapeRenderer shapeRenderer;

    //Ships and Suns stuff
    Model model;

    //Ships and Suns CONSTANTS
    static final int SHIP_NUM = 500 ;//Ships per player
    static final double DEST_RADIUS = 30;
    static final double ENGAGEMENT_RANGE = 50;
    static final double TERMINAL_VELOCITY = 20;
    static final double MAX_FORCE = 5;

    //Cam
    int height, width;

    //Test
    float retarget = 0;

    private Ship ship;
    public static long start;
    private int drawShips;
    private int allShipRemove = 0;

    //View
    private Color[] colormap = new Color[]{new Color(Color.RED), new Color(Color.BLUE)};
    static Texture blue_earth = new Texture("blue_earth.png");
    static Texture red_earth = new Texture("red_earth.png");
    static Texture grey_earth = new Texture("grey_earth.png");
    static Texture redship = new Texture("ship.png");
    Texture tempTexture;

    public static FileHandle file, checksumFile, deternismFile;

    //For average 60 fps system to step model
    private float frameTime;
    private float accumulator;

    public InGameScreen(Game game, final Model model) {
        Gdx.app.log("Ingame", "GAME  screen STARTED");
        this.game = game;
        this.model = model;
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        cam = new OrthographicCamera(width, height);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        start = TimeUtils.millis();

        SocketManager socketManager = model.socket();

        //Controller
        controller = new Controller(model, cam, socketManager);
        Gdx.input.setInputProcessor(new GestureDetector(controller));

        file = Gdx.files.external(new SimpleDateFormat("'Ships and Suns/'MM-dd-yyyy'/interpolation network 'hh-mm a'.txt'").format(new Date()));
        checksumFile = Gdx.files.external(new SimpleDateFormat("'Ships and Suns/'MM-dd-yyyy'/interpolation checksums 'hh-mm a'.txt'").format(new Date()));
        deternismFile = Gdx.files.external(new SimpleDateFormat("'Ships and Suns/'MM-dd-yyyy'/interpolation determinism 'hh-mm a'.txt'").format(new Date()));
        deternismFile.writeString("This is a " + socketManager.getName() + " log file\n", true);
        file.writeString("This is a " + socketManager.getName() + " log file\n", true);
        checksumFile.writeString("This is a " + socketManager.getName() + " log file\n", true);



    }
    //Game Mechanic Functions

    public void update(float delta) {
        if(model.state() != Model.GameState.PLAYING)return;
        frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;
        while (accumulator >= 1 / 60f) {
            model.step(1/60f);
            //Make a Turn instance
            accumulator -= 1/60f;
        }
    }

    @Override
    public void render(float delta) {
        cam.update();
        spriteBatch.setProjectionMatrix(cam.combined);
        shapeRenderer.setProjectionMatrix(cam.combined);

        Gdx.gl.glClearColor(1f, 1f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        spriteBatch.begin();

        drawShips = 0;

        //Gdx.app.log("InGameScreen","Drawing");

        spriteBatch.setColor(Color.WHITE);
        for(ObjectData sun : model.getSuns()){
            switch (sun.spritekey){
                case -1: tempTexture = grey_earth; break;
                case 0: tempTexture = red_earth; break;
                case 1: tempTexture = blue_earth; break;
            }
            spriteBatch.draw(tempTexture,(int) sun.pos.x-sun.size/2, (int) sun.pos.y-sun.size/2, sun.size, sun.size);
        }
        //Draw all ships
        for (ObjectData ship:model.getShips()) {
            drawShips++;
            spriteBatch.setColor(colormap[ship.spritekey]);
            spriteBatch.draw(redship, ship.pos.x - ship.size,
                    ship.pos.y - ship.size, ship.size*2, ship.size*2);
        }
        spriteBatch.end();

        shapeRenderer.begin();
        shapeRenderer.setColor(colormap[model.me()]);
        if(controller.setDestState!= Controller.SETDESTSTATE.NOT){
            shapeRenderer.circle(controller.setDestSelectCenter.x,controller.setDestSelectCenter.y,controller.setDestRadius);
        }
        shapeRenderer.rect(0,0,model.mapSize().x,model.mapSize().y);
        shapeRenderer.end();

        //debugRenderer.render(world, cam.combined);

        update(delta);
    }

    @Override
    public void resize(int w, int h) {
        this.width = Gdx.graphics.getWidth();
        this.height = Gdx.graphics.getHeight();
        cam.setToOrtho(false,width,height);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
    }

    @Override
    public void show() {
        controller.start();
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
        FixtureDefFactory.dispose();
    }
}
