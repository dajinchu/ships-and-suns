package com.gmail.dajinchu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;

import java.util.Iterator;
import java.util.Random;

/**
 * Created by Da-Jin on 12/20/2014.
 */
public class Model {
    Player[] players;
    Player me;
    private ShipTile[][] grid;
    IntMap<Ship> allShips =  new IntMap<Ship>();
    long seed;
    Random random;


    int gridHeight, gridWidth, mapHeight, mapWidth;

    int shipIdCount = 0;

    //Memory saving fields, unfortunate to expand score this way, but not much choice
    private Ship tempship;


    public Model(int mapWidth, int mapHeight){
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void setSeed(long seed){
        this.seed = seed;
        random = new Random(seed);
    }

    public void makeGrid(int tileSize){
        gridHeight=(int) Math.ceil(mapHeight/tileSize);
        gridWidth=(int) Math.ceil(mapWidth/tileSize);
        grid = new ShipTile[gridHeight][gridWidth];
        for(int y=0; y < gridHeight; y++){
            for(int x = 0; x < gridWidth; x++){
                grid[y][x] = new ShipTile(x,y);
                Gdx.app.log("Making grid",x+" "+y);
            }
        }
        for(int y=0; y < gridHeight; y++){
            for(int x = 0; x < gridWidth; x++){
                grid[y][x].fillNeighbors(this);
            }
        }
    }
    public void initShipDistro(int numPlayers, int shipsPerPlayer){
        players = new Player[numPlayers];
        int x,y;
        for(int p = 0; p < numPlayers; p++){//use the "Player player: players" syntax?
            players[p] = new Player(p,this);
            for(int i = 0; i < shipsPerPlayer; i++){
                x=random.nextInt(mapWidth);
                y=random.nextInt(mapHeight);
                spawnShip(players[p],x,y);
            }
            players[p].setDest(random.nextInt(mapWidth), random.nextInt(mapHeight));
        }
        me = players[0];
    }
    public void update(float delta){
        for(IntMap.Entry<Ship> ship : allShips.entries()){
            if(ship.value.destroyed){
                continue;
            }
            ship.value.frame();
        }
        for(Iterator iterator = allShips.values();iterator.hasNext();){
            tempship = (Ship) iterator.next();
            if(tempship.destroyed) {
                iterator.remove();
                continue;
            }
            tempship.killFrame();
        }
    }

    public void spawnShip(Player player, int x, int y){
        allShips.put(shipIdCount,new Ship(x,y,player,shipIdCount,this));
        shipIdCount++;
    }

    //Getter-Setter
    public ShipTile getShipTile(int x, int y){
        //Finally end all that y/x confusion
        return grid[y][x];
    }

    public Ship getShip(int id){
        return allShips.get(id);
    }
}
