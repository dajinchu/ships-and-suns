package com.gmail.dajinchu.desktop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Da-Jin on 1/22/2015.
 */
public class Quick extends Game {
    private Vector2 initialPointer1 = new Vector2(4,4);
    private Vector2 initialPointer2 = new Vector2(4,1);
    private Vector2 pointer1 = new Vector2(4,5);
    private Vector2 pointer2 = new Vector2(4,2);

    @Override
    public void create() {

        Vector2 delta = initialPointer1.add(initialPointer2).scl(.5f).sub(pointer1.add(pointer2).scl(.5f));
        System.out.println(delta);
    }
}
