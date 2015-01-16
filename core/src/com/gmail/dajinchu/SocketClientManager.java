package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Da-Jin on 1/1/2015.
 */
public class SocketClientManager implements SocketManager {

    private BufferedReader reader;
    private Writer writer;

    private BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<Message>();

    MessageObserver observer;
    private String readline;

    String TAG = "SocketClientManager";

    public SocketClientManager(Socket socket){
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new SocketSend()).start();
        new Thread(new SocketReceive()).start();
    }
    @Override
    public void sendMsg(Message msg) {
        try {
            InGameScreen.file.writeString("Sending msg to server. Frame "+Model.worldFrame+"\n", true);
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
    public void notifyObserver(Message msg) {
        InGameScreen.file.writeString("Received msg from server. Frame "+Model.worldFrame+"\n", true);
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
                Message msg;
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
                        notifyObserver(SetDestAction.deserialize(readline));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
