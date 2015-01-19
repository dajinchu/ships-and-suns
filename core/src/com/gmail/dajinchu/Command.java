package com.gmail.dajinchu;

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
        List<String> args = new ArrayList<String>(Arrays.asList(in.split(",")));
        int msg_type = Integer.parseInt(args.remove(0));
        switch (msg_type){
            case 0: return deserialize0(args);
        }
        //If returning null like this, SOMETHING IS BAD
        return null;
    }

    private static CreateFutureSetDestCommand deserialize0(List<String> args){
        return new CreateFutureSetDestCommand(Integer.parseInt(args.get(0)),Integer.parseInt(args.get(1)),
                Integer.parseInt(args.get(2)),Integer.parseInt(args.get(3)));
    }
}
