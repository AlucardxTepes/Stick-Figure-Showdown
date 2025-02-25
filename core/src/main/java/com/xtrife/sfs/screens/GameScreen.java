package com.xtrife.sfs.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.xtrife.sfs.Main;
import com.xtrife.sfs.resources.Assets;
import com.xtrife.sfs.resources.GlobalVariables;

/**
 * Created by 9S on 2/24/2025 - 10:28 PM.
 */
public class GameScreen implements Screen, InputProcessor {

    private final Main game;
    private final ExtendViewport viewport;

    // background/ring
    private Texture backgroundTexture;
    private Texture frontRopeTexture;

    // ring boundaries
    private static final float RING_MIN_X = 18f;
    private static final float RING_MAX_X = 144f;
    private static final float RING_MIN_Y = 11f;
    private static final float RING_MAX_Y = 50f;
    private static final float RING_SLOPE = 3.16f;

    // fighters
    private static final float PLAYER_START_POSITION_X = 60f;
    private static final float OPPONENT_START_POSITION_X = 100f;
    private static final float FIGHTER_START_POSITION_Y = 35f;


    public GameScreen(Main game) {
        this.game = game;

        // set up the viewport
        viewport = new ExtendViewport(GlobalVariables.WORLD_WIDTH, GlobalVariables.MIN_WORLD_HEIGHT,
            GlobalVariables.WORLD_WIDTH, 0);

        // create the game area
        createGameArea();

        // ready fighters
        game.player.getReady(PLAYER_START_POSITION_X, FIGHTER_START_POSITION_Y);
        game.opponent.getReady(OPPONENT_START_POSITION_X, FIGHTER_START_POSITION_Y);
    }

    private void createGameArea() {
        // get the ring textures from the asset manager
        backgroundTexture = game.assets.manager.get(Assets.BACKGROUND_TEXTURE);
        frontRopeTexture = game.assets.manager.get(Assets.FRONT_ROPES_TEXTURE);
    }

    @Override
    public void show() {
        // have this GameScreen handle inputs
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // update the game
        update(delta);

        // set the sprite batch to use our camera
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        // begin drawing
        game.batch.begin();

        // draw the background at half its image size
        game.batch.draw(backgroundTexture, 0, 0, backgroundTexture.getWidth() * GlobalVariables.WORLD_SCALE, backgroundTexture.getHeight() * GlobalVariables.WORLD_SCALE);

        // draw the fighters
        renderFighters();

        // draw the front ropes layer after righters so that it is rendered over them
        game.batch.draw(frontRopeTexture, 0, 0, frontRopeTexture.getWidth() * GlobalVariables.WORLD_SCALE, frontRopeTexture.getHeight() * GlobalVariables.WORLD_SCALE);

        // end drawing
        game.batch.end();

    }

    private void renderFighters() {
        // use the y coordinates to determine which fighter sprite is in front
        if (game.player.getPosition().y > game.opponent.getPosition().y) {
            game.player.render(game.batch);  // draw player first
            game.opponent.render(game.batch);
        } else {
            game.opponent.render(game.batch);  // draw opponent first
            game.player.render(game.batch);
        }

    }

    private void update(float delta) {
        game.player.update(delta); // delta is the elapsed time since last screen render
        game.opponent.update(delta);

        // make sure fighters are facing each other
        if (game.player.getPosition().x <= game.opponent.getPosition().x) {
            game.player.faceRight();
            game.opponent.faceLeft();
        } else {
            game.player.faceLeft();
            game.opponent.faceRight();
        }

        // keep fighters within ring boundaries
        keepWithinRingBounds(game.player.getPosition());
        keepWithinRingBounds(game.opponent.getPosition());
    }

    private void keepWithinRingBounds(Vector2 position) {
        if (position.y < RING_MIN_Y ) {
            position.y = RING_MIN_Y;
        } else if (position.y > RING_MAX_Y) {
            position.y = RING_MAX_Y;
        }
        if (position.x < position.y / RING_SLOPE + RING_MIN_X ) {
            position.x = position.y / RING_SLOPE +  RING_MIN_X;
        } else if (position.x > position.y / -RING_SLOPE + RING_MAX_X ) {
            position.x = position.y / -RING_SLOPE + RING_MAX_X;
        }
    }

    @Override
    public void resize(int width, int height) {
        // this method is also called the first time the game 8window is opened
        // update the viewport with the new screen size
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        // check for player movement key
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            game.player.moveLeft();
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            game.player.moveRight();
        }
        // separate if for vertical movement to allow players to move both horiz or vert simultaneously
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            game.player.moveUp();
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            game.player.moveDown();
        }

        return true; // means we have handled the key input here
    }

    @Override
    public boolean keyUp(int keycode) {
        // stop movement on key release
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            game.player.stopMovingLeft();
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            game.player.stopMovingRight();
        }
        // separate if for vertical movement to allow players to move both horiz or vert simultaneously
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            game.player.stopMovingUp();
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            game.player.stopMovingDown();
        }

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
