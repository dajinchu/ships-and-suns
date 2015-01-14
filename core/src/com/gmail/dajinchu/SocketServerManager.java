package com.gmail.dajinchu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Da-Jin on 1/9/2015.
 */
//A bit confusing here, instead of actually sending a Msg or notifying observer like in client,
//it takes sendMsg directly into it's processing queue, and simulates receiving when it sends
//to the clients, because it is a client too.
//Should eventually manage lock-step and such, but for now is basically an echo server
//Implements SocketManager because to the game, usage is exactly the same as SocketClientManager
//Implements MessageObserver to subscribe to the SocketClientManagers that it uses to listen
//  to client connections
public class SocketServerManager implements SocketManager, MessageObserver {
    private SocketClientManager clientManager;
    private BufferedWriter writer;
    private BufferedReader reader;

    BlockingQueue<Message> processingQueue = new LinkedBlockingQueue<Message>();

    MessageObserver observer;

    public SocketServerManager(Socket client){
        clientManager = new SocketClientManager(client);
        //Subscribe this to listen on each socket connection to each client
        clientManager.setMessageReceived(this);
    }

    @Override
    public void sendMsg(Message msg) {
        //sendMsg gets called by game, and this gets treated like any other client
        update(msg);
    }

    @Override
    public void setMessageReceived(MessageObserver msgrec) {
        observer = msgrec;
    }

    @Override
    public void notifyObserver(Message msg) {
        observer.update(msg);
    }

    public void process(){

    }

    @Override
    public void update(Message msg) {
        //When clients send messages over, including the client on server's device
        processingQueue.add(msg);
        clientManager.sendMsg(msg);
        notifyObserver(msg);
    }
}