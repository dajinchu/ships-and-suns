package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 1/5/2015.
 */
public interface Message {

    public String serialize();
    public void deserialize(String in);
}
