package com.gmail.dajinchu.android;

import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.TimeUtils;
import com.gmail.dajinchu.MainGame;
import com.gmail.dajinchu.Model;

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
    private ServerSocket serverSocket;
    String TAG = "HostingLobby";
    private Socket client;
    private JmDNS jmdns;
    private ServiceInfo serviceInfo;
    private Model model;

    public HostingLobby(MainGame mainGame, JmDNS jmdns){
        this.mainGame = mainGame;
        this.jmdns = jmdns;
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
            writer.flush();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    mainGame.startGame(model, reader, writer);
                }
            });
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

    }
}
