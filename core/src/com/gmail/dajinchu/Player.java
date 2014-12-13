package com.gmail.dajinchu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.DelayedRemovalArray;

import java.io.Serializable;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class Player implements Serializable {
    DelayedRemovalArray<Ship> my_ships = new DelayedRemovalArray<Ship>(false,InGameScreen.SHIP_NUM);//ships under this Player's control
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
    public void drawShips(SpriteBatch batch){
        my_ships.begin();
        for(Ship ship : my_ships){
            if(ship.destroyed){
                my_ships.removeValue(ship, true);
                break;
            }
            //It draws bottomleft corner at given coords, so we give it coords shifted down and left
            batch.draw(texture, (int) ship.x-textureXShift, (int) ship.y-textureYShift);
        }
        my_ships.end();
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
