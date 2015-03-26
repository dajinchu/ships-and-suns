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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.IntMap;
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

    //Box2D
    private final World world;
    private final Box2DDebugRenderer debugRenderer;

    //View
    //static Texture[] textureMap = new Texture[]{new Texture(Gdx.files.internal("red.png")),new Texture(Gdx.files.internal("blue.png"))};//number->color link
    static Texture blue_earth = new Texture("blue_earth.png");
    static Texture red_earth = new Texture("red_earth.png");
    static Texture grey_earth = new Texture("grey_earth.png");
    static Texture redship = new Texture("ship.png");
    Texture tempTexture;

    public static FileHandle file, checksumFile, deternismFile;

    //For average 60 fps system to step model
    private float frameTime;
    private float accumulator;

    public InGameScreen(Game game, final Model model, final SocketManager socketManager) {
        Gdx.app.log("Ingame", "GAME  screen STARTED");
        this.game = game;
        this.model = model;
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        this.world = model.world;

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        cam = new OrthographicCamera(width, height);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        debugRenderer = new Box2DDebugRenderer();

        start = TimeUtils.millis();

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
        if(model.state != Model.GameState.PLAYING)return;
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
        for(Sun sun : model.allSuns){
            switch (sun.state){
                case EMPTY: case CAPTURING: tempTexture = grey_earth; break;
                case CAPTURED:case DECAPTURING: case UPGRADING:
                    if(sun.occupant.playerNumber==0)tempTexture = red_earth;
                    if(sun.occupant.playerNumber==1)tempTexture = blue_earth;
                    break;
            }
            spriteBatch.draw(tempTexture,(int) sun.pos.x-sun.size/2, (int) sun.pos.y-sun.size/2, sun.size, sun.size);
        }
        //Draw all ships
        for (IntMap.Entry<Ship> entry : model.allShips.entries()) {
            ship = entry.value;
            drawShips++;
            spriteBatch.setColor(ship.my_owner.color);
            spriteBatch.draw(redship, ship.pos.x - ship.radius,
                    ship.pos.y - ship.radius, ship.radius*2, ship.radius*2);
        }
        spriteBatch.end();

        shapeRenderer.begin();
        shapeRenderer.setColor(model.me.color);
        if(controller.setDestState!= Controller.SETDESTSTATE.NOT){
            shapeRenderer.circle(controller.setDestSelectCenter.x,controller.setDestSelectCenter.y,controller.setDestRadius);
        }
        shapeRenderer.rect(0,0,model.mapWidth,model.mapHeight);
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
