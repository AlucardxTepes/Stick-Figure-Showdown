package com.xtrife.sfs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.xtrife.sfs.resources.Assets;
import com.xtrife.sfs.screens.GameScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {
    public SpriteBatch batch;
    public Assets assets;

    // screens
    public GameScreen gameScreen;


    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();

        // load all assets
        assets.load(); // assets are loaded asynchronously
        assets.manager.finishLoading(); // blocks until all assets are done loading

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
    }
}
