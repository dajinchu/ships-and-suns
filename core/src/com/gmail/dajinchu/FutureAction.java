package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 1/5/2015.
 */
//FutureActions are held by HostModel and will be executed at an exact worldFrame for proper sync.
public interface FutureAction {
    public void execute(HostModel model);
    public int getScheduledFrame();
    public int getPlayerId();
}
