package com.xtrife.sfs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.xtrife.sfs.objects.Fighter;
import com.xtrife.sfs.resources.Assets;
import com.xtrife.sfs.screens.GameScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public Assets assets;

    // screens
    public GameScreen gameScreen;

    // fighters
    public Fighter player, opponent;


    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assets = new Assets();

        // load all assets
        assets.load(); // assets are loaded asynchronously
        assets.manager.finishLoading(); // blocks until all assets are done loading

        // init fighters
        player = new Fighter(this, "El Tipo", new Color(1f, 0.2f, 0.2f, 1f));
        opponent = new Fighter(this, "El Otro Tipo", new Color(0.25f, 0.7f, 1f, 1f));

        // init game screen and switch to it
        gameScreen = new GameScreen(this);
        setScreen(gameScreen);

    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        assets.dispose();
        shapeRenderer.dispose();
    }
}
