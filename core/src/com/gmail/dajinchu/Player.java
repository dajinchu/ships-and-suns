package com.gmail.dajinchu;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Da-Jin on 12/5/2014.
 */
public class Player{
    private final Model model;
    Array<Integer> my_ships = new Array<Integer>();//ships under this Player's control
    int playerNumber;//For identification across devices, each number corresponds to a color
    int destx=300,desty=300;


    String TAG = "Player";

    public Player(int playerNumber, Model model){
        this.model = model;
        this.playerNumber = playerNumber;
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
        for(int id : my_ships){
            //System.out.print(id);
            if(model.getShip(id)==null){
                continue;
            }
            model.getShip(id).calcDestWithWander(destx, desty);
        }
    }

}
