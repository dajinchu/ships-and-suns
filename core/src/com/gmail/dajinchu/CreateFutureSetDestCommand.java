package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 1/5/2015.
 */
//Command to add a new SetDestAction to Model when executed
public class CreateFutureSetDestCommand extends Command {

    private final int x,y;
    private final int frame;
    private final int playerid;


    public CreateFutureSetDestCommand(int frame, int player, int x, int y){
        this.frame = frame;
        this.playerid = player;
        this.x = x;
        this.y = y;
    }

    @Override
    public String serialize() {
        return String.format("0,%s,%s,%s,%s", frame,playerid,x,y);
    }

    @Override
    public void execute(Controller controller) {
        controller.model.addFutureAction(new SetDestAction(frame,playerid,x,y));
    }
}
