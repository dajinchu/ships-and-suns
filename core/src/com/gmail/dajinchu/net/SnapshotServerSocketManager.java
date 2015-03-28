package com.gmail.dajinchu.net;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Created by Da-Jin on 3/24/2015.
 */
public class SnapshotServerSocketManager extends SocketManager {


    public SnapshotServerSocketManager(BufferedReader reader, BufferedWriter writer) {
        super(reader, writer);
        TAG="Server"+TAG;
    }

    @Override
    public void decodeMessage(String msg) {
        //No client to server commands yet
        Gdx.app.log(TAG, "Received something idk why. msg:"+msg);
    }
}
