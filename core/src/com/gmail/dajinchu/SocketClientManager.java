package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Da-Jin on 1/1/2015.
 */
public class SocketClientManager implements SocketManager {

    private final BufferedReader reader;
    private final Writer writer;

    private BlockingQueue<String> sendingQueue = new LinkedBlockingQueue<String>();

    MessageObserver observer;
    private String readline;

    String TAG = "SocketClientManager";

    public SocketClientManager(Writer writer, BufferedReader reader){
        this.writer = writer;
        this.reader = reader;
        new Thread(new SocketSend()).start();
        new Thread(new SocketReceive()).start();
    }
    @Override
    public void sendMsg(String msg) {
        try {
            Gdx.app.log(TAG, msg);
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
    public void notifyObserver() {
        if(observer==null)return;
        observer.update(readline);
    }
    class SocketSend implements Runnable{
        @Override
        public void run() {
            while(true){
                String str;
                while((str = sendingQueue.poll()) != null){
                    Gdx.app.log(TAG, "Sending a thing! ");
                    try {
                        writer.write(str+"\n");
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
                        notifyObserver();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
