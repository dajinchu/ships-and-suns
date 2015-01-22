package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
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

    //Screems
    MainGame game;

    //Classes that help make stuff work
    OrthographicCamera cam;
    SpriteBatch spriteBatch;
    ShapeRenderer shapeRenderer;

    //Ships and Suns stuff
    Model model;

    //Ships and Suns CONSTANTS
    static final int MAPWIDTH = 400;//TODO make this *map* w/h, annotate theses constants
    static final int MAPHEIGHT = 400;
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
    static Texture[] textureMap = new Texture[]{new Texture(Gdx.files.internal("red.png")),new Texture(Gdx.files.internal("blue.png"))};//number->color link
    static Texture blue_earth = new Texture("blue_earth.png");
    static Texture red_earth = new Texture("red_earth.png");
    static Texture grey_earth = new Texture("grey_earth.png");

    Texture tempTexture;

    public static FileHandle file;

    public InGameScreen(MainGame game, final Model model, final SocketManager socketManager) {
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
        final Controller controller = new Controller(model, cam, socketManager);
        Gdx.input.setInputProcessor(new GestureDetector(controller));

        file = Gdx.files.external(new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date()) + " print networking" + ".txt");
        file.writeString("This is a " + socketManager.getName() + " log file\n", true);



    }
    //Game Mechanic Functions

    public void update(float delta) {
        model.update(delta);
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

        //Draw all ships
        for (IntMap.Entry<Ship> entry : model.allShips.entries()) {
            ship = entry.value;
            drawShips++;
            spriteBatch.draw(textureMap[ship.my_owner.playerNumber], (int) ship.pos.x - textureMap[ship.my_owner.playerNumber].getWidth()/2,
                    (int) ship.pos.y - textureMap[ship.my_owner.playerNumber].getHeight()/2);
        }
        for(Sun sun : model.allSuns){
            switch (sun.state){
                case EMPTY: case CAPTURING: tempTexture = grey_earth; break;
                case CAPTURED: case DECAPTURING:
                    if(sun.occupant.playerNumber==0)tempTexture = red_earth;
                    if(sun.occupant.playerNumber==1)tempTexture = blue_earth;
                    break;
            }
            spriteBatch.draw(tempTexture,(int) sun.pos.x-Sun.size/2, (int) sun.pos.y-Sun.size/2, Sun.size, Sun.size);
        }
        spriteBatch.end();

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
