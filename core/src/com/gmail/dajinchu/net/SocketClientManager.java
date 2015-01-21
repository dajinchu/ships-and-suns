package com.gmail.dajinchu.net;

import com.badlogic.gdx.Gdx;
import com.gmail.dajinchu.InGameScreen;
import com.gmail.dajinchu.Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Da-Jin on 1/1/2015.
 */
//Manages a Socket connections, making it easier to send and receive messages(Command)
//Client gets one each to talk to server, but ServerManager gets one for every client connected.
public class SocketClientManager implements SocketManager {

    private BufferedReader reader;
    private Writer writer;

    private BlockingQueue<Command> sendingQueue = new LinkedBlockingQueue<Command>();

    MessageObserver observer;
    private String readline;

    String TAG = "SocketClientManager";

    public SocketClientManager(BufferedReader reader, BufferedWriter writer){
        this.writer = writer;
        this.reader = reader;
    }
    @Override
    public void start(){
        new Thread(new SocketSend()).start();
        new Thread(new SocketReceive()).start();
    }
    @Override
    public void sendMsg(Command msg) {
        try {
            InGameScreen.file.writeString("Sending '"+msg.serialize()+"'. Frame "+ Model.worldFrame+"\n", true);
            Gdx.app.log(TAG, msg.serialize());
            sendingQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void setMessageReceived(MessageObserver msgrec) {
        observer = msgrec;
    }
    @Override
    public void notifyObserver(Command msg) {
        InGameScreen.file.writeString("Received '"+msg.serialize()+"'. Frame "+ Model.worldFrame+"\n", true);
        if(observer==null)return;
        observer.update(msg);
    }

    @Override
    public String getName() {
        return "client";
    }

    class SocketSend implements Runnable{
        @Override
        public void run() {
            while(true){
                Command msg;
                while((msg = sendingQueue.poll()) != null){
                    Gdx.app.log(TAG, "Sending a thing! ");
                    try {
                        writer.write(msg.serialize()+"\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    class SocketReceive implements Runnable{
        @Override
        public void run() {
            while(true){
                //Gdx.app.log("Receive", "Checking for more on ufferedREader");
                try{
                    if((readline = reader.readLine())!=null){
                        notifyObserver(Command.deserialize(readline));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
