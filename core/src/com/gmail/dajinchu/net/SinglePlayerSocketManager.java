package com.gmail.dajinchu.net;

/**
 * Created by Da-Jin on 2/2/2015.
 */
//Basically a fake SocketManager for single player, just bounces stuff back
public class SinglePlayerSocketManager implements SocketManager{

    MessageObserver observer;

    public SinglePlayerSocketManager(){

    }

    @Override
    public void start() {
        //Bypass the ready to play sync system, there's not screen switching and it's single player!
        notifyObserver(new ReadyToPlayCommand(0));
        notifyObserver(new ReadyToPlayCommand(1));
    }

    @Override
    public void sendMsg(Command msg) {
        //Just bounce it back
        notifyObserver(Command.deserialize(msg.serialize()));
    }

    @Override
    public void setMessageReceived(MessageObserver msgrec) {
        observer = msgrec;
    }

    @Override
    public void notifyObserver(Command msg) {
        observer.update(msg);
    }

    @Override
    public String getName() {
        return "single-player";
    }
}
