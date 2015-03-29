package com.gmail.dajinchu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by Da-Jin on 3/23/2015.
 */
//TODO wtf is this class needed for!?
public class Snapshot {
    Array<ObjectData> ships = new Array<ObjectData>();
    Array<ObjectData> suns = new Array<ObjectData>();
    public StringBuilder ret;

    public Snapshot(HostModel model){

        ret = new StringBuilder();
        ret.append(model.allShips.size+" ");
        ret.append(model.allSuns.size+" ");

        for(Ship ship:model.allShips.values()){
            ret.append(ship.pos.x + " " + ship.pos.y + " "+ship.size+" "+ship.spritekey+" ");
            ret.append(ship.id+" ");
        }
        for(Sun sun:model.allSuns){
            ret.append(sun.pos.x + " " + sun.pos.y + " "+sun.size+" "+sun.spritekey+" ");
            ret.append(sun.progress+" ");
            ret.append(sun.maxupgrade+" ");
        }
    }

    public Snapshot(String in){
        Array<String> stringdata = new Array<String>(in.split(" "));
        Iterator<String> data = stringdata.iterator();

        int numships = Integer.parseInt(data.next());
        int numsuns = Integer.parseInt(data.next());

        ObjectData tmp;

        //TODO unify this crap by adding a type parameter that InGameScreen/Model knows about too
        for(int i=0; i<numships; i++){
            tmp = new ObjectData(new Vector2(Float.parseFloat(data.next()),Float.parseFloat(data.next())),Integer.parseInt(data.next()),Integer.parseInt(data.next()));
            tmp.id = Integer.parseInt(data.next());
            ships.add(tmp);
        }
        for(int i=0; i<numsuns; i++){
            tmp = new ObjectData(new Vector2(Float.parseFloat(data.next()),Float.parseFloat(data.next())),Integer.parseInt(data.next()),Integer.parseInt(data.next()));
            tmp.progress = Integer.parseInt(data.next());
            tmp.maxupgrade = Integer.parseInt(data.next());
            suns.add(tmp);
        }
    }

}
