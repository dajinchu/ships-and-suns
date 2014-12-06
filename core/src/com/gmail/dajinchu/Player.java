package com.gmail.dajinchu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class Player implements Serializable {
    ArrayList<Ship> my_ships = new ArrayList<Ship>();//ships under this Player's control
    ArrayList<Ship> remove_ships = new ArrayList<Ship>();//ships to be removed
    int playerNumber;//For identification across devices, each number corresponds to a color
    int destx=100,desty=100;
    Texture texture;

    //Graphics
    static Texture[] textureMap = new Texture[]{new Texture("red.png"),new Texture("blue.png")};//number->color link


    String TAG = "Player";

    public Player(int playerNumber){
        this.playerNumber = playerNumber;
        texture = textureMap[playerNumber];
    }
    public void drawShips(SpriteBatch batch){
        for(Ship ship : my_ships){
            //Log.i("PLAYER", bmp+" "+ship.x+" "+ship.y+" "+mPaint);
            batch.draw(texture, (int) ship.x, (int) ship.y);
        }
    }
    public void frame(){
        //Log.i(TAG, my_ships.size()+"");
        for(Ship ship : my_ships){
            ship.frame();
        }
        for(Ship ship: remove_ships){
            my_ships.remove(ship);
        }
        remove_ships.clear();
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
