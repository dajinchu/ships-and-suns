package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Da-Jin on 3/26/2015.
 */
public class ObjectData{
    Vector2 pos;
    int size;
    int spritekey;
    public ObjectData(Vector2 pos, int size, int spritekey){
        this.pos = pos;
        this.size = size;
        this.spritekey = spritekey;
    }

    public ObjectData() {//For Ship and Sun classes to call super
    }
}