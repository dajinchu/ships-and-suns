package com.gmail.dajinchu.net;

/**
 * Created by Da-Jin on 1/1/2015.
 */
public interface SocketManager {

    void start();

    void sendMsg(Command msg);
    void setMessageReceived(MessageObserver msgrec);
    void notifyObserver(Command msg);
    String getName();
}
