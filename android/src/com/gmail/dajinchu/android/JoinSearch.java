package com.gmail.dajinchu.android;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gmail.dajinchu.MainGame;
import com.gmail.dajinchu.Model;
import com.gmail.dajinchu.net.SocketClientManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Created by Da-Jin on 1/27/2015.
 */
public class JoinSearch implements Screen{

    private final MainGame mainGame;

    private JmDNS jmdns;
    private WifiManager.MulticastLock lock;
    private final WifiManager wifi;
    private ServiceListener listener;
    private Socket socket;
    private final String type = "_ships._tcp.local.";
    private String name;

    //UI
    Stage stage;
    Table table;
    Skin skin;
    private Model model;


    public JoinSearch(MainGame mainGame, JmDNS jmDNS, WifiManager wifi, String name){
        this.mainGame = mainGame;
        this.jmdns = jmDNS;
        this.wifi = wifi;
        this.name = name;

        stage = new Stage(new ExtendViewport(640,480));
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);

        table.add(new Label("Nearby Games:", skin));


        new Search().execute();
    }

    class Search extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            lock = wifi.createMulticastLock("mylockthereturn");
            lock.setReferenceCounted(false);
            lock.acquire();
            jmdns.addServiceListener(type, listener = new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent ev) {
                    addConnect(ev.getName()+"'s game", ev.getInfo().getHostAddresses()[0], ev.getInfo().getPort());
                }

                @Override
                public void serviceRemoved(ServiceEvent ev) {
                    notifyUser("Service removed: " + ev.getName());
                }

                @Override
                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again (after the first search)
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });
            return null;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void notifyUser(String msg){
        table.add(msg);
    }

    public void addConnect(String name, final String ip, final int port){
        TextButton textButton = new TextButton(name,skin.get(TextButton.TextButtonStyle.class));
        table.add(textButton);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new ASyncConnect(ip,port).execute();
            }
        });

    }

    @Override
    public void resize(int width, int height) {

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
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            jmdns = null;
        }
        lock.release();
    }

    //Connect to IP
    class ASyncConnect extends AsyncTask<Void,Void,Void> {

        String host;
        int port;

        public ASyncConnect(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Gdx.app.log("CLient",port+"");
                socket = new Socket(host, 13079);//Random hardcoded port
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Tell server name
                writer.write(name+"\n");
                writer.flush();
                readInitialSetup(br);
                waitForStart(br,writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }

    public void readInitialSetup(BufferedReader reader){
        try {
            long seed = Long.parseLong(reader.readLine());
            int player_id = Integer.parseInt(reader.readLine());

            model = Model.ModelFactory.defaultModel(seed, player_id);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForStart(final BufferedReader reader, final BufferedWriter writer) {
        Gdx.app.log("Client", "Waiting "+reader.toString());
        try {
            if (reader.readLine().equals("Start")) {
                Gdx.app.log("Client", "waiting for start");
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {

                        mainGame.startGame(model, new SocketClientManager(reader,writer));
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
