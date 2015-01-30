package com.gmail.dajinchu.android;

import android.os.AsyncTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.TimeUtils;
import com.gmail.dajinchu.MainGame;
import com.gmail.dajinchu.Model;
import com.gmail.dajinchu.net.SocketServerManager;
import com.splunk.mint.Mint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class HostingLobby implements Screen{

    private final MainGame mainGame;
    private String name;
    private ServerSocket serverSocket;
    String TAG = "HostingLobby";
    private Socket client;
    private JmDNS jmdns;
    private ServiceInfo serviceInfo;
    private Model model;

    private SpriteBatch spriteBatch = new SpriteBatch();
    //private final ShapeRenderer shapeRenderer;

    //Scene2d set-up
    private Skin skin;
    //private Stage stage;

    //UI
    /*private final VerticalGroup playerList;
    private Table table;
    private final ImageButton go;*/

    public HostingLobby(MainGame mainGame, JmDNS jmdns, String name){
        this.mainGame = mainGame;
        this.jmdns = jmdns;
        this.name = name;
        new ASyncConnect().execute();

/*        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        TextureAtlas atlas = new TextureAtlas("game.pack");
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        skin.addRegions(atlas);

        stage = new Stage(new ExtendViewport(640,480));
        Gdx.input.setInputProcessor(stage);

        //Player List
        playerList = new VerticalGroup();
        playerList.setDebug(true);
        playerList.space(10);
        playerList.addActor(new Label("Participants", skin));
        playerList.fill();
        addParticipant("Da-Jin Chu");
        addParticipant("Bob");
        ScrollPane players = new ScrollPane(playerList);
        players.setDebug(true);
        //GO Button, adding clickListener later, AFTER we have connected with someone
        go = new ImageButton(skin.getDrawable("play"),skin.getDrawable("play_down"));
        //Map selector
        Drawable mapimg = skin.getDrawable("map1");
        Image map = new Image(mapimg);
        //Right Pane:
        Table rightPane = new Table();
        rightPane.setDebug(true);
        rightPane.add(new Label("<",skin));
        rightPane.add(map).height(300).width(300).pad(10);
        rightPane.add(new Label(">", skin));
        rightPane.row();
        rightPane.add(go).colspan(3).width(200).height(75).expandY().right();

        TextField nameText = new TextField("", skin);
        Label addressLabel = new Label("Address:", skin);
        TextField addressText = new TextField("", skin);

        table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);
        table.add(players).expand().top().fillX().pad(10);
        table.add(rightPane).fill();*/
    }

    class ASyncConnect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                serviceInfo = ServiceInfo.create("_ships._tcp.local.", name, 13079, "plain test service from android");
                //notifyUser("This IP: " + deviceIpAddress);
                jmdns.registerService(serviceInfo);
                if (serverSocket == null) {
                    serverSocket = new ServerSocket(13079);//Random hardcoded port
                    Mint.leaveBreadcrumb("Server socket opened");
                }
                Mint.leaveBreadcrumb("Server socket="+serverSocket.toString());
                client = serverSocket.accept();
                /*final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                final BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                // Write output
                //sendInitalSetup(writer);
                //AFTER sending inital setup info, we can "activate" the go button to have actions
                /*go.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y){
                        sendStart(br,writer);
                    }
                });*/
            } catch (IOException e) {
                e.printStackTrace();
            }
            Gdx.app.log(TAG,client.getLocalPort()+"");
            return null;//TODO make this like client
        }
    }

    public void sendStart(final BufferedReader reader, final BufferedWriter writer){
        try {
            writer.write("Start\n");
            Gdx.app.log("HostingLobby", "sent start");
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    mainGame.startGame(model, new SocketServerManager(reader, writer));
                }
            });
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInitalSetup(BufferedWriter writer){
        try {
            long seed = TimeUtils.millis();
            int client_player_id = 1;
            int my_player_id = 0;

            writer.write(seed + "\n" + client_player_id+"\n");
            writer.flush();
            model = Model.defaultModel(seed, my_player_id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addParticipant(String name){
        Stack participant = new Stack();
        participant.add(new Image(skin.getPatch("button")));
        participant.add(new Label(name, skin));
        //playerList.addActor(participant);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(255,255,255,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //stage.act(Gdx.graphics.getDeltaTime());
        //stage.draw();
    }

    @Override
    public void resize(int width, int height) {
//        stage.getViewport().update(width, height, true);
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
        //stage.dispose();
        //shapeRenderer.dispose();
    }
}
