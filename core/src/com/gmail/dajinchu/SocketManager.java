package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 1/1/2015.
 */
public interface SocketManager {

    void sendMsg(Command msg);
    void setMessageReceived(MessageObserver msgrec);
    void notifyObserver(Command msg);
    String getName();
}
