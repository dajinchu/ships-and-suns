package com.gmail.dajinchu.android;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;


/**
 * Created by Da-Jin on 12/14/2014.
 */
public class LANConnect implements Screen {

    WifiManager.MulticastLock lock;
    Handler handler = new Handler();


    private String type = "_test2._tcp.local.";
    private static final String HOSTNAME = "dajinlol";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    private ServiceInfo serviceInfo;
    private AndroidLauncher activity;

    //UI
    private Stage stage;
    private Table table;
    //For debug
    private ShapeRenderer shapeRenderer;
    private Skin uiSkin;

    public LANConnect(AndroidLauncher androidLauncher){
        activity = androidLauncher;

    }

    class SetUp extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //http://stackoverflow.com/questions/18255329/how-to-make-jmdns-work-on-a-large-network
            //https://github.com/twitwi/AndroidDnssdDemo
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) activity.getSystemService(android.content.Context.WIFI_SERVICE);
            final InetAddress deviceIpAddress = getDeviceIpAddress(wifi);
            lock = wifi.createMulticastLock("mylockthereturn");
            lock.setReferenceCounted(true);
            lock.acquire();
            try {
                jmdns = JmDNS.create(deviceIpAddress, HOSTNAME);
                jmdns.addServiceListener(type, listener = new ServiceListener() {

                    @Override
                    public void serviceResolved(ServiceEvent ev) {
                        addConnect("Service resolved: " + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort()+"IP: "+ Arrays.toString(ev.getInfo().getHostAddresses()),
                                ev.getInfo().getHostAddresses()[0], ev.getInfo().getPort());
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
                serviceInfo = ServiceInfo.create("_test2._tcp.local.", "AndroidTest", 0, "plain test service from android");
                notifyUser("This IP: " + deviceIpAddress);
                jmdns.registerService(serviceInfo);
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

        table.drawDebug(shapeRenderer);

    }

    public void notifyUser(String msg){
        table.add(msg);
    }

    public void addConnect(String name, final String ip, final int port){
        TextButton textButton = new TextButton(name,uiSkin.get(TextButton.TextButtonStyle.class));
        table.add(textButton);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println(ip+" "+port);
            }
        });

    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        table = new Table(uiSkin);
        table.setFillParent(true);
        stage.addActor(table);

        //Label label = new Label("HI", new Label.LabelStyle());
        table.add("HI");

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
        stage.dispose();
        shapeRenderer.dispose();
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
        }
        lock.release();
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
