package com.gmail.dajinchu;

import com.badlogic.gdx.graphics.Texture;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class Player implements Serializable {
    LinkedList<Ship> my_ships = new LinkedList<Ship>();//ships under this Player's control
    int playerNumber;//For identification across devices, each number corresponds to a color
    int destx=300,desty=300;
    Texture texture;


    //Graphics
    static Texture[] textureMap = new Texture[]{new Texture("red.png"),new Texture("blue.png")};//number->color link
    int textureXShift, textureYShift;

    String TAG = "Player";

    public Player(int playerNumber){
        this.playerNumber = playerNumber;
        texture = textureMap[playerNumber];
        textureXShift = texture.getWidth()/2;
        textureYShift = texture.getHeight()/2;
    }

    //TODO maybe you'll need this?
    /*private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        in.defaultReadObject();
        mPaint = new Paint();
        Log.i("Player", "Getting de-serialized!");
    }*/

    public void setDest(int destx, int desty){
        this.destx = destx;
        this.desty = desty;
        for(Ship ship : my_ships){
            ship.calcDestWithWander(destx,desty);
        }
    }

}
