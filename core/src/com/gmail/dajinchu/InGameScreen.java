package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;

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
    static final int SHIP_NUM = 1000 ;//Ships per player
    static final double DEST_RADIUS = 50;
    static final double ENGAGEMENT_RANGE = 50;
    static final double TERMINAL_VELOCITY = 20;
    static final double MAX_FORCE = 5;

    //Cam
    int height, width;

    //Test
    float retarget = 0;

    private Ship ship;
    private long start;
    private int drawShips;
    private int allShipRemove = 0;

    //Box2D
    private final World world;
    private final Box2DDebugRenderer debugRenderer;

    public InGameScreen(MainGame game) {
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);


        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        cam = new OrthographicCamera(width, height);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        //Box2D
        Box2D.init();
        world = new World(new Vector2(0,0), true);
        debugRenderer = new Box2DDebugRenderer();

        //Game:
        model = new Model(MAPWIDTH, MAPHEIGHT, world);
        model.setSeed(1234);
        model.initShipDistro(2, SHIP_NUM);

        start = TimeUtils.millis();

        //Controller
        Gdx.input.setInputProcessor(new GestureDetector(new Controller(model, cam)));
    }

    //Game Mechanic Functions

    public void update(float delta) {
        model.update(delta);


        retarget += delta;
        //System.out.println(retarget);
        if (retarget > 5) {
//            Gdx.app.log("SHIP", TimeUtils.timeSinceMillis(start)+" "+model.allShips.get(0).pos.x+" "+model.allShips.get(0).pos.y);
//            model.players[1].setDest(model.random.nextInt(MAPWIDTH), model.random.nextInt(MAPHEIGHT));
            retarget = 0;
        }
    }


    @Override
    public void render(float delta) {
        cam.update();
        spriteBatch.setProjectionMatrix(cam.combined);
        shapeRenderer.setProjectionMatrix(cam.combined);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();

        drawShips = 0;

        //Draw all ships
        for (IntMap.Entry<Ship> entry : model.allShips.entries()) {
            ship = entry.value;
            drawShips++;
            //spriteBatch.draw(player.texture,0,0);
            spriteBatch.draw(ship.my_owner.texture, (int) ship.pos.x - ship.my_owner.textureXShift, (int) ship.pos.y - ship.my_owner.textureYShift);
        }
        spriteBatch.end();
        //Gdx.app.log("Draw ships", drawShips+" "+Ship.dead+" "+allShipRemove);

        shapeRenderer.end();
        debugRenderer.render(world, cam.combined);
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
