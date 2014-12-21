package com.gmail.dajinchu.android;

import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.gmail.dajinchu.MainGame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class HostingLobby implements Screen{

    private ServerSocket serverSocket;
    String TAG = "HostingLobby";
    private Socket client;
    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    public HostingLobby(MainGame mainGame, JmDNS jmdns){
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
                    serverSocket = new ServerSocket(49151);//Random hardcoded port
                    Log.i(TAG, "Server socket opened");
                }
                client = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Gdx.app.log(TAG,client.getLocalPort()+"");
            return null;//TODO make this like client
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