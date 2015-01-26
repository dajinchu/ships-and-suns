package com.gmail.dajinchu.desktop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Created by Da-Jin on 1/22/2015.
 */
public class Quick extends Game {
    private Vector2 initialPointer1 = new Vector2(4,4);
    private Vector2 initialPointer2 = new Vector2(4,1);
    private Vector2 pointer1 = new Vector2(4,5);
    private Vector2 pointer2 = new Vector2(4,2);
    private Stage stage;
    private Table table;
    private NinePatch label;
    private Drawable map;
    private VerticalGroup playerList;
    private Skin skin;

    @Override
    public void create() {
        /*TexturePacker.Settings setting = new TexturePacker.Settings();
        setting.fast = true;
        TexturePacker.process(setting, "../PackImages","../android/assets","game");*/

        TextureAtlas atlas = new TextureAtlas("game.pack");
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        skin.addRegions(atlas);

        stage = new Stage(new ExtendViewport(640,480));
        Gdx.input.setInputProcessor(stage);

        playerList = new VerticalGroup();
        playerList.setDebug(true);
        playerList.space(10);
        playerList.addActor(new Label("Participants", skin));
        playerList.fill();
        addParticipant("Da-Jin Chu");
        addParticipant("Bob");
        ScrollPane players = new ScrollPane(playerList);
        players.setDebug(true);

        //GO Button
        ImageButton go = new ImageButton(skin.getDrawable("play"),skin.getDrawable("play_down"));
        //Map selector
        Drawable mapimg = skin.getDrawable("map1");
        Image map = new Image(mapimg);
        //Right Pane:
        Table rightPane = new Table();
        rightPane.setDebug(true);
        rightPane.add(new Label("<",skin));
        rightPane.add(map).height(300).width(300).pad(10);
        rightPane.add(new Label(">", skin));
        rightPane.row();
        rightPane.add(go).colspan(3).width(200).height(75).expandY().right();

        TextField nameText = new TextField("", skin);
        Label addressLabel = new Label("Address:", skin);
        TextField addressText = new TextField("", skin);

        table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);
        table.add(players).expand().top().fillX().pad(10);
        table.add(rightPane).fill();

    }

    public void addParticipant(String name){
        Stack participant = new Stack();
        participant.add(new Image(skin.getPatch("button")));
        participant.add(new Label(name, skin));
        playerList.addActor(participant);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(){
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }
}
