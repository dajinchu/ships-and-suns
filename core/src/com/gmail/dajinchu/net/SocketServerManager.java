package com.gmail.dajinchu.net;

import com.gmail.dajinchu.HostModel;
import com.gmail.dajinchu.Snapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Created by Da-Jin on 3/27/2015.
 */
public class SocketServerManager extends SocketManager {


    public SocketServerManager(BufferedReader reader, BufferedWriter writer) {
        super(reader, writer);
    }

    @Override
    public void sendCmd(Command msg){
        //To be compatible with client, so controller can call them same way
        msg.execute((HostModel)observer);//TODO fix this terrible casting crap
    }

    public void sendSnap(Snapshot snapshot){
        sendMsg(snapshot.ret.toString());
    }

}
