package com.gmail.dajinchu.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gmail.dajinchu.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Da-Jin on 1/5/2015.
 */
//Commands are to be sent serialized across sockets.
public abstract class Command {

    public abstract String serialize();
    public abstract void execute(Controller controller);

    public static Command deserialize(String in){
        Gdx.app.log("Command", "deserializing "+in);
        List<String> args = new ArrayList<String>(Arrays.asList(in.split(",")));
        int msg_type = Integer.parseInt(args.remove(0));
        switch (msg_type){
            case 0: return SetDest(args);
            case 1: return Ready(args);
            case 2: return DoneSending(args);
        }
        //If returning null like this, SOMETHING IS BAD
        return null;
    }

    private static CreateFutureSetDestCommand SetDest(List<String> args){
        return new CreateFutureSetDestCommand(Integer.parseInt(args.get(0)),Integer.parseInt(args.get(1)),
                new Vector2(Float.parseFloat(args.get(2)),Float.parseFloat(args.get(3))),
                new Vector2(Float.parseFloat(args.get(4)),Float.parseFloat(args.get(5))),
                Float.parseFloat(args.get(6)));
    }
    private static ReadyToPlayCommand Ready(List<String> args){
        return new ReadyToPlayCommand(Integer.parseInt(args.get(0)));
    }
    private static DoneSendingCommand DoneSending(List<String> args){
        return new DoneSendingCommand(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)));
    }
}
