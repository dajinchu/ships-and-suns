package com.gmail.dajinchu.android;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.gmail.dajinchu.ConnectScreen;
import com.gmail.dajinchu.Model;
import com.splunk.mint.Mint;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;


/**
 * Created by Da-Jin on 12/14/2014.
 */
//LANConnect will instantiate JmDNS and give user option to host,join,and rename
public class LANConnect extends ConnectScreen {

    private Preferences prefs;

    private static final String HOSTNAME = "dajinlol";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    private AndroidLauncher activity;

    //UI
    private Stage stage;
    private Table table;
    //For debug
    private ShapeRenderer shapeRenderer;
    private Skin uiSkin;
    private Model model;
    private Socket socket;
    private TextField name;
    private WifiManager wifi;

    public LANConnect(AndroidLauncher androidLauncher){
        activity = androidLauncher;

    }

    class SetUp extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //http://stackoverflow.com/questions/18255329/how-to-make-jmdns-work-on-a-large-network
            //https://github.com/twitwi/AndroidDnssdDemo
            wifi = (android.net.wifi.WifiManager) activity.getSystemService(android.content.Context.WIFI_SERVICE);
            final InetAddress deviceIpAddress = getDeviceIpAddress(wifi);
            try {
                jmdns = JmDNS.create(deviceIpAddress, HOSTNAME);

                /*serviceInfo = ServiceInfo.create("_test2._tcp.local.", "AndroidTest", 0, "plain test service from android");
                notifyUser("This IP: " + deviceIpAddress);
                jmdns.registerService(serviceInfo);*/
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
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


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        prefs = Gdx.app.getPreferences("playerPrefs");

        stage = new Stage(new ExtendViewport(640,480));
        Gdx.input.setInputProcessor(stage);

        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        table = new Table(uiSkin);
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);

        //Host and Join Buttons
        TextButton host = new TextButton("HOST", uiSkin.get(TextButton.TextButtonStyle.class));
        TextButton join = new TextButton("JOIN", uiSkin.get(TextButton.TextButtonStyle.class));
        host.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Mint.logEvent("Starting HostingLobby");
                mainGame.setScreen(new HostingLobby(mainGame, jmdns, name.getText()));
            }
        });
        join.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Mint.logEvent("Starting JoinSearch");
                mainGame.setScreen(new JoinSearch(mainGame, jmdns, wifi, name.getText()));
            }
        });
        //Name field
        name = new TextField(prefs.getString("name","Mr.E-Man"), uiSkin);
        name.setTextFieldListener(new TextField.TextFieldListener() {
            //Save name for player's convenience
            @Override
            public void keyTyped(TextField textField, char c) {
                prefs.putString("name", textField.getText());
                prefs.flush();
            }
        });

        //Add to table
        table.add(name).colspan(2).top();
        table.row();
        table.add(host).width(200).height(200).pad(100);
        table.add(join).width(200).height(200).pad(100);

        shapeRenderer = new ShapeRenderer();
        new SetUp().execute();

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
        Gdx.app.log("LANConnect","Dispoingd");
        stage.dispose();
        shapeRenderer.dispose();
        uiSkin.dispose();
    }

    private InetAddress getDeviceIpAddress(WifiManager wifi) {
        InetAddress result = null;
        try {
            // default to Android localhost
            result = InetAddress.getByName("10.0.0.2");

            // figure out our wifi address, otherwise bail
            WifiInfo wifiinfo = wifi.getConnectionInfo();
            int intaddr = wifiinfo.getIpAddress();
            byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff), (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff) };
            result = InetAddress.getByAddress(byteaddr);
        } catch (UnknownHostException ex) {
            Log.w("DnssdDiscoveryActivity", String.format("getDeviceIpAddress Error: %s", ex.getMessage()));
        }

        return result;
    }
}
