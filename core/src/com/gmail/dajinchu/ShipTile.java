package com.gmail.dajinchu;

import java.util.ArrayList;

/**
 * Created by Da-Jin on 12/8/2014.
 */
public class ShipTile {
    final int x, y;
    ArrayList<Ship> ships = new ArrayList<Ship>();
    ArrayList<ShipTile> neighbors = new ArrayList<ShipTile>();

    public ShipTile(int x, int y){
        super();
        this.x = x;
        this.y = y;
    }

    public void fillNeighbors(InGameScreen gameScreen){

        neighbors.add(gameScreen.grid[y][x]);
        if(y+1<gameScreen.gridHeight){
            neighbors.add(gameScreen.grid[y + 1][x]);
            if(x+1<gameScreen.gridWidth){
                neighbors.add(gameScreen.grid[y + 1][x + 1]);
            }
            if(x-1>0){
                neighbors.add(gameScreen.grid[y + 1][x - 1]);
            }
        }
        if(y-1>0){
            neighbors.add(gameScreen.grid[y - 1][x]);
            if(x+1<gameScreen.gridWidth){
                neighbors.add(gameScreen.grid[y - 1][x + 1]);
            }
            if(x-1>0){
                neighbors.add(gameScreen.grid[y - 1][x - 1]);
            }
        }
        if(x+1<gameScreen.gridWidth) {
            neighbors.add(gameScreen.grid[y][x + 1]);
        }
        if(x-1>0){
            neighbors.add(gameScreen.grid[y][x - 1]);
        }
    }
}
