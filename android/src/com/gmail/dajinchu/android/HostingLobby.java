package com.gmail.dajinchu.android;

import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gmail.dajinchu.MainGame;
import com.gmail.dajinchu.Model;
import com.gmail.dajinchu.net.SocketServerManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class HostingLobby implements Screen{

    private final MainGame mainGame;
    private final ShapeRenderer shapeRenderer;
    private ServerSocket serverSocket;
    String TAG = "HostingLobby";
    private Socket client;
    private JmDNS jmdns;
    private ServiceInfo serviceInfo;
    private Model model;
    private SpriteBatch spriteBatch;
    private NinePatch label;
    private Stage stage;
    private Table table;

    public HostingLobby(MainGame mainGame, JmDNS jmdns){
        this.mainGame = mainGame;
        this.jmdns = jmdns;
        shapeRenderer = new ShapeRenderer();
        new ASyncConnect().execute();
    }

    class ASyncConnect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                serviceInfo = ServiceInfo.create("_test2._tcp.local.", "AndroidTest", 0, "plain test service from android");
                //notifyUser("This IP: " + deviceIpAddress);
                jmdns.registerService(serviceInfo);
                if (serverSocket == null) {
                    serverSocket = new ServerSocket(13079);//Random hardcoded port
                    Log.i(TAG, "Server socket opened");
                }
                client = serverSocket.accept();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                // Write output
                sendInitalSetup(writer);
                sendStart(writer, br);

                //writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Gdx.app.log(TAG,client.getLocalPort()+"");
            return null;//TODO make this like client
        }
    }

    public void sendStart(final BufferedWriter writer, final BufferedReader reader){
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

/*        spriteBatch.begin();
        table.drawDebug(shapeRenderer);
        spriteBatch.end();*/
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        TextureAtlas atlas = new TextureAtlas("game.pack");
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        skin.addRegions(atlas);

        stage = new Stage(new ExtendViewport(640,480));
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);

        VerticalGroup playerList = new VerticalGroup();
        stage.addActor(new ScrollPane(playerList));

        //GO Button
        Stack stack = new Stack();
        table.add(stack).expandY().bottom().fillX();
        label = skin.getPatch("button");
        Image image = new Image(label);
        stack.add(image);
        stack.add(new Label("GO", skin));
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
        stage.dispose();
        shapeRenderer.dispose();
    }
}
