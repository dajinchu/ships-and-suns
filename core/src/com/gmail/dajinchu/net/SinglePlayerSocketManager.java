package com.gmail.dajinchu.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Created by Da-Jin on 2/2/2015.
 */
//Basically a fake SocketManager for single player, just bounces stuff back
public class SinglePlayerSocketManager extends SocketManager {

    MessageObserver observer;

    public SinglePlayerSocketManager(BufferedReader reader, BufferedWriter writer) {
        super(reader, writer);
    }

    @Override
    public void start() {
        //Bypass the ready to play sync system, there's not screen switching and it's single player!
    }

    @Override
    public void setMessageReceived(MessageObserver msgrec) {
        observer = msgrec;
    }

    @Override
    public String getName() {
        return "single-player";
    }
}
