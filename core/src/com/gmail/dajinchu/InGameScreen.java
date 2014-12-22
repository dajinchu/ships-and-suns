package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
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
    public static final int SHIP_NUM = 1000;//Ships per player
    static final double DEST_RADIUS = 50;
    public static final double ENGAGEMENT_RANGE = 50;
    static final double TERMINAL_VELOCITY = 2;
    static final double MAX_FORCE = .1;

    //Cam
    int height, width;

    //Test
    float retarget = 0;

    private Ship ship;
    private long start;
    private int drawShips;
    private int allShipRemove = 0;

    public InGameScreen(MainGame game, Model model) {
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


        //Controller
        Gdx.input.setInputProcessor(new GestureDetector(new Controller(model, cam)));
    }

    //Game Mechanic Functions

    public void update(float delta) {
        model.update(delta);

        /*
        for(Player player : players){
            player.killFrame();
        }*/
        retarget += delta;
        //System.out.println(retarget);
        if (retarget > 5) {
            //Gdx.app.log("SHIP", Ship.newGrid+" "+Ship.loopcount);
            model.players[1].setDest(model.random.nextInt(MAPWIDTH), model.random.nextInt(MAPHEIGHT));
            retarget = 0;
        }
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
        spriteBatch.draw(model.me.texture,0,0);
        for (IntMap.Entry<Ship> entry : model.allShips.entries()) {
            ship = entry.value;
            drawShips++;
            spriteBatch.draw(ship.my_owner.texture, (int) ship.x - ship.my_owner.textureXShift, (int) ship.y - ship.my_owner.textureYShift);
        }
        spriteBatch.end();
        //Draw destination circles
        if (model.players != null) {
            for (Player player : model.players) {
                shapeRenderer.begin();
                shapeRenderer.circle(player.destx, player.desty, (float) DEST_RADIUS);
                shapeRenderer.end();
            }
        }
        //Gdx.app.log("Draw ships", drawShips+" "+Ship.dead+" "+allShipRemove);

        //Draw collision detection optimization grid borders
        shapeRenderer.begin();
        for (int h = 0; h < model.gridHeight; h++) {
            for (int w = 0; w < model.gridWidth; w++) {
                shapeRenderer.rect((float) (w * ENGAGEMENT_RANGE), (float) (h * ENGAGEMENT_RANGE), (float) (ENGAGEMENT_RANGE), (float) (ENGAGEMENT_RANGE));
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
