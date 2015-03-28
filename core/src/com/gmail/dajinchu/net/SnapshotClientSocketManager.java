package com.gmail.dajinchu.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Created by Da-Jin on 3/26/2015.
 */
public class SnapshotClientSocketManager extends SocketManager {

    public SnapshotClientSocketManager(BufferedReader reader, BufferedWriter writer) {
        super(reader, writer);
    }

    @Override
    public void decodeMessage(String msg) {
        //This should be receiving snapshots

    }
}
