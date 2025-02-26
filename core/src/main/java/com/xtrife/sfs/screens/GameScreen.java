package com.xtrife.sfs.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.xtrife.sfs.Main;
import com.xtrife.sfs.objects.Fighter;
import com.xtrife.sfs.resources.Assets;
import com.xtrife.sfs.resources.GlobalVariables;

import java.util.Locale;

/**
 * Created by 9S on 2/24/2025 - 10:28 PM.
 */
public class GameScreen implements Screen, InputProcessor {

    private final Main game;
    private final ExtendViewport viewport;

    // game
    private enum GameState { RUNNING, PAUSED, GAME_OVER }
    private GameState gameState;
    private GlobalVariables.Difficulty difficulty = GlobalVariables.Difficulty.EASY;


    // rounds
    private enum RoundState { STARTING, IN_PROGRESS, ENDING }
    private RoundState roundState;
    private float roundStateTime;
    private static final float START_ROUND_DELAY = 2f; // 2 sec delay
    private static final float END_ROUND_DELAY = 2f;
    private int currentRound;
    private static final int MAX_ROUNDS = 3;
    private int roundsWon = 0, roundsLost = 0;
    private static final float MAX_ROUND_TIME = 99.99f;
    private float roundTimer = MAX_ROUND_TIME;
    private static final float CRITICAL_ROUND_TIME = 10f;
    private static final Color CRITICAL_ROUND_TIME_COLOR = Color.RED;


    // fonts
    BitmapFont smallFont, mediumFont, largeFont;
    private static final Color DEFAULT_FONT_COLOR = Color.WHITE;

    // HUD
    private static final Color HEALTH_BAR_COLOR = Color.RED;
    private static final Color HEALTH_BAR_BACKGROUND_COLOR = GlobalVariables.GOLD;

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
    private static final float FIGHTER_CONTACT_DISTANCE_X = 17f;
    private static final float FIGHTER_CONTACT_DISTANCE_Y = 3f;


    public GameScreen(Main game) {
        this.game = game;

        // set up the viewport
        viewport = new ExtendViewport(GlobalVariables.WORLD_WIDTH, GlobalVariables.MIN_WORLD_HEIGHT,
            GlobalVariables.WORLD_WIDTH, 0);

        // create the game area
        createGameArea();

        // set up the fonts
        setupFonts();

        startGame();
    }

    private void createGameArea() {
        // get the ring textures from the asset manager
        backgroundTexture = game.assets.manager.get(Assets.BACKGROUND_TEXTURE);
        frontRopeTexture = game.assets.manager.get(Assets.FRONT_ROPES_TEXTURE);
    }

    private void setupFonts() {
        smallFont = game.assets.manager.get(Assets.SMALL_FONT);
        smallFont.getData().setScale(GlobalVariables.WORLD_SCALE);
        smallFont.setColor(DEFAULT_FONT_COLOR);
        smallFont.setUseIntegerPositions(false);

        mediumFont = game.assets.manager.get(Assets.MEDIUM_FONT);
        mediumFont.getData().setScale(GlobalVariables.WORLD_SCALE);
        mediumFont.setColor(DEFAULT_FONT_COLOR);
        mediumFont.setUseIntegerPositions(false);

        largeFont = game.assets.manager.get(Assets.LARGE_FONT);
        largeFont.getData().setScale(GlobalVariables.WORLD_SCALE);
        largeFont.setColor(DEFAULT_FONT_COLOR);
        largeFont.setUseIntegerPositions(false);
    }

    @Override
    public void show() {
        // have this GameScreen handle inputs
        Gdx.input.setInputProcessor(this);

        // start the game
    }

    private void startGame() {
        gameState = GameState.RUNNING;
        roundsWon = roundsLost = 0;

        // start round 1
        currentRound = 1;
        startRound();
    }

    private void startRound() {
        // ready fighters
        game.player.getReady(PLAYER_START_POSITION_X, FIGHTER_START_POSITION_Y);
        game.opponent.getReady(OPPONENT_START_POSITION_X, FIGHTER_START_POSITION_Y);
        // start the round
        roundState = RoundState.STARTING;
        roundStateTime = 0f;
        roundTimer = MAX_ROUND_TIME;
    }

    private void endRound() {
        roundState = RoundState.ENDING;
        roundStateTime = 0f;
    }

    private void winRound() {
        // player wins the round
        game.player.win();
        game.opponent.lose();
        roundsWon++;
        endRound();
    }

    private void loseRound() {
        game.player.lose();
        game.opponent.win();
        roundsLost++;
        endRound();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // update the game
        update(delta);

        // set the sprite batch and the shape renderer to use our camera
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        // begin drawing
        game.batch.begin();

        // draw the background at half its image size
        game.batch.draw(backgroundTexture, 0, 0, backgroundTexture.getWidth() * GlobalVariables.WORLD_SCALE, backgroundTexture.getHeight() * GlobalVariables.WORLD_SCALE);

        // draw the fighters
        renderFighters();

        // draw the front ropes layer after righters so that it is rendered over them
        game.batch.draw(frontRopeTexture, 0, 0, frontRopeTexture.getWidth() * GlobalVariables.WORLD_SCALE, frontRopeTexture.getHeight() * GlobalVariables.WORLD_SCALE);

        // draw the HUD
        renderHud();

        // if the round starting, draw the start round text
        if (roundState == RoundState.STARTING) {
            renderStartRoundText();
        }

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

    private void renderHud() {
        float HUDMargin = 1f;

        // Draw the rounds won / lost ratio
        smallFont.draw(game.batch, "WINS: " + roundsWon + " - " + roundsLost, HUDMargin, viewport.getWorldHeight() - HUDMargin);

        // draw difficulty setting
        String text = "DIFFICULTY: ";
        switch (difficulty) {
            case EASY:
                text += "EASY";
                break;
            case MEDIUM:
                text += "MEDIUM";
                break;
            default:
                text += "HARD";
        }
        smallFont.draw(game.batch, text, viewport.getWorldWidth() - HUDMargin,
            viewport.getWorldHeight() - HUDMargin, 0, Align.right, false);

        // setup layout sizes and positioning
        float healthbarPadding = 0.5f;
        float healthbarHeight = smallFont.getCapHeight() + healthbarPadding * 4f;
        float healthbarMaxWidth = 75f;
        float healthbarBackgroundPadding = 0.2f;
        float healthbarBackgroundHeight = healthbarHeight + healthbarPadding;
        float healthbarBackgroundWidth = healthbarMaxWidth + healthbarBackgroundPadding * 2f;
        float healthbarBackgroundMarginTop = 0.8f;
        float healthbarBackgroundPosY = viewport.getWorldHeight() - HUDMargin - smallFont.getCapHeight() -
            healthbarBackgroundMarginTop - healthbarBackgroundHeight;
        float healthbarPosY = healthbarBackgroundPosY + healthbarBackgroundPadding;
        float fighterNamePosY = healthbarPosY + healthbarHeight - healthbarPadding - 0.5f;

        game.batch.end();
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // draw the fighter health bar background rectangles
        game.shapeRenderer.setColor(HEALTH_BAR_BACKGROUND_COLOR);
        game.shapeRenderer.rect(HUDMargin, healthbarBackgroundPosY, healthbarBackgroundWidth, healthbarBackgroundHeight);
        game.shapeRenderer.rect(viewport.getWorldWidth() - HUDMargin - healthbarBackgroundWidth,
            healthbarBackgroundPosY, healthbarBackgroundWidth, healthbarBackgroundHeight);

        // draw health bar rectangles
        game.shapeRenderer.setColor(HEALTH_BAR_COLOR);
        float healthbarWidth = healthbarMaxWidth * game.player.getLife() / Fighter.MAX_LIFE;
        game.shapeRenderer.rect(HUDMargin + healthbarBackgroundPadding, healthbarPosY, healthbarWidth, healthbarHeight);
        healthbarWidth = healthbarMaxWidth * game.opponent.getLife() / Fighter.MAX_LIFE;
        game.shapeRenderer.rect(viewport.getWorldWidth() - HUDMargin - healthbarBackgroundPadding - healthbarWidth,
            healthbarPosY, healthbarWidth, healthbarHeight);

        game.shapeRenderer.end();
        game.batch.begin();

        // draw the fighter names
        smallFont.draw(game.batch, game.player.getName(), HUDMargin + healthbarBackgroundPadding +
            healthbarPadding, fighterNamePosY);
        smallFont.draw(game.batch, game.opponent.getName(), viewport.getWorldWidth() - HUDMargin -
            healthbarBackgroundPadding - healthbarPadding, fighterNamePosY, 0, Align.right, false);

        // draw the round timer
        if (roundTimer < CRITICAL_ROUND_TIME) {
            mediumFont.setColor(CRITICAL_ROUND_TIME_COLOR);
        }
        mediumFont.draw(game.batch, String.format(Locale.getDefault(), "%02d", (int) roundTimer), // pad numbers less than 10 with a leading 0
            viewport.getWorldWidth() / 2f - mediumFont.getSpaceXadvance() * 2.3f, // makes digits not move around due to size diff
            viewport.getWorldHeight() - HUDMargin);
        mediumFont.setColor(DEFAULT_FONT_COLOR); // reset font color in case it was turned red due to crit round timer

    }

    private void renderStartRoundText() {
        String text;
        if (roundStateTime < START_ROUND_DELAY * 0.5f) { // text lasts only half of the start round delay duration
            text = "ROUND " + currentRound;
        } else {
            text = "FIGHT!";
        }
        mediumFont.draw(game.batch, text, viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f,
            0, Align.center, false);
    }

    private void update(float delta) {
        if (roundState == RoundState.STARTING && roundStateTime >= START_ROUND_DELAY) {
            // if start round delay has been reached, start the fight
            roundState = RoundState.IN_PROGRESS;
            roundStateTime = 0f;
        } else if (roundState == RoundState.ENDING && roundStateTime >= END_ROUND_DELAY) {
            // if end round delay has been reached and player has won or lost more than half of the max number of rounds
            // end the game; otherwise start the next round
            if (roundsWon > MAX_ROUNDS / 2 || roundsLost > MAX_ROUNDS / 2) {
                gameState = GameState.GAME_OVER;
            } else {
                currentRound++;
                startRound();
            }
        } else {
            // increment the round state time by delta time
            roundStateTime += delta;
        }


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

        if (roundState == RoundState.IN_PROGRESS) {
            // decrease timer if round in progress
            roundTimer -= delta;
            if (roundTimer <= 0f) {
                // timout win decided by highest HP
                if(game.player.getLife() >= game.opponent.getLife()) {
                    winRound();
                } else {
                    loseRound();
                }
            }
            // round in progress. Check if fighters are within contact distance
            if (areWithinContactDistance(game.player.getPosition(), game.opponent.getPosition())) {
                if (game.player.isAttackActive()) {
                    // if within contact distance AND while attacking, apply hit
                    game.opponent.getHit(Fighter.HIT_STRENGTH);
                    System.out.println("Opponents life: " + game.opponent.getLife());

                    // deactivate player attack after contact
                    game.player.makeContact();

                    // check if opponent has lost
                    if (game.opponent.hasLost()) {
                        winRound();
                    }
                }
            }
        }
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

    private boolean areWithinContactDistance(Vector2 position1, Vector2 position2) {
        // determine if contact is possible
        float xDistance = Math.abs(position1.x - position2.x); // we want positive values only
        float yDistance = Math.abs(position1.y - position2.y);
        return xDistance <= FIGHTER_CONTACT_DISTANCE_X && yDistance <= FIGHTER_CONTACT_DISTANCE_Y;
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
        if (keycode == Input.Keys.ENTER) {
            if (gameState == GameState.RUNNING) {
                // skip round delay
                if (roundState == RoundState.STARTING) {
                    roundStateTime = START_ROUND_DELAY;
                } else if (roundState == RoundState.ENDING) {
                    roundStateTime = END_ROUND_DELAY;
                }
            } else if (gameState == GameState.GAME_OVER) {
                // if game over and key is pressed, restart the game
                startGame();
            }
        } else {
            // Enable fight controls

            if (roundState == RoundState.IN_PROGRESS) {
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
            }


            // attack / block
            if (keycode == Input.Keys.SPACE) {
                game.player.block();
            } else if (keycode == Input.Keys.K) {
                game.player.kick();
            } else if (keycode == Input.Keys.J) {
                game.player.punch();
            }
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

        // stop blocking on key release
        if (keycode == Input.Keys.SPACE) {
            game.player.stopBlocking();
        }


        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // convert from top left pixels into world coordinates
        Vector3 position = new Vector3(screenX, screenY, 0);
        viewport.getCamera().unproject(position, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight()); // unproject affects position var value

        if (gameState == GameState.RUNNING) {
            if (roundState == RoundState.STARTING) {
                // if the round is starting and screen has been clicked/touched, skip start round delay
                roundStateTime = START_ROUND_DELAY;
            } else if (roundState == RoundState.ENDING) {
                roundStateTime = END_ROUND_DELAY; // same for round ending delay
            }
        }


        return true; // let system know our code has handled the key event
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
