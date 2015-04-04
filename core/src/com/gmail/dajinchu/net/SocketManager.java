package com.gmail.dajinchu.net;

import com.badlogic.gdx.Gdx;
import com.gmail.dajinchu.InGameScreen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Da-Jin on 1/1/2015.
 */
public class SocketManager {

    private BufferedReader reader;
    private Writer writer;

    private BlockingQueue<String> sendingQueue = new LinkedBlockingQueue<String>();
    private String readline;


    String TAG = "SocketManager";
    MessageObserver observer;

    public SocketManager(BufferedReader reader, BufferedWriter writer){
        this.writer = writer;
        this.reader = reader;
    }
    public void start(){
        new Thread(new SocketSend()).start();
        new Thread(new SocketReceive()).start();
    }

    public void decodeMessage(String msg) {
        observer.update(msg);
    }

    public void setMessageReceived(MessageObserver msgrec) {
        observer = msgrec;
    }

    public void sendCmd(Command cmd){
        InGameScreen.file.writeString("Sending cmd: '"+cmd.serialize()+"'", true);
        sendMsg(cmd.serialize());
    }

    void sendMsg(String msg){
        try {
            //InGameScreen.file.writeString("Sending '"+msg+"'. Frame "+ +"Time "+ TimeUtils.timeSinceMillis(InGameScreen.start)+"\n", true);
            sendingQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class SocketSend implements Runnable{
        @Override
        public void run() {
            while(true){
                String msg;
                while((msg = sendingQueue.poll()) != null){
                    Gdx.app.log(TAG, "Sending a thing! ");
                    try {
                        writer.write(msg+"\n");
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
                        decodeMessage(readline);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getName(){
        return TAG;
    }
}
