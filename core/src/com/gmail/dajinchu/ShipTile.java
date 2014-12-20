package com.gmail.dajinchu;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Da-Jin on 12/8/2014.
 */
public class ShipTile {
    final int x, y;
    Array<Integer> ships = new Array<Integer>();
    Array<ShipTile> neighbors = new Array<ShipTile>();

    public ShipTile(int x, int y){
        super();
        this.x = x;
        this.y = y;
    }

    public void fillNeighbors(Model model){

        neighbors.add(model.getShipTile(x,y));
        if(y+1<model.gridHeight){
            neighbors.add(model.getShipTile(x,y + 1));
            if(x+1<model.gridWidth){
                neighbors.add(model.getShipTile(x + 1,y + 1));
            }
            if(x-1>0){
                neighbors.add(model.getShipTile(x - 1,y + 1));
            }
        }
        if(y-1>0){
            neighbors.add(model.getShipTile(x,y - 1));
            if(x+1<model.gridWidth){
                neighbors.add(model.getShipTile(x + 1,y - 1));
            }
            if(x-1>0){
                neighbors.add(model.getShipTile(x - 1,y - 1));
            }
        }
        if(x+1<model.gridWidth) {
            neighbors.add(model.getShipTile(x + 1,y));
        }
        if(x-1>0){
            neighbors.add(model.getShipTile(x - 1,y));
        }
    }
}