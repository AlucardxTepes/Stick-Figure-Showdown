package com.xtrife.sfs.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.xtrife.sfs.Main;
import com.xtrife.sfs.objects.BloodSplatter;
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
    private enum GameState {RUNNING, PAUSED, GAME_OVER}

    private GameState gameState;
    private GlobalVariables.Difficulty difficulty = GlobalVariables.Difficulty.EASY;


    // rounds
    private enum RoundState {STARTING, IN_PROGRESS, ENDING}

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

    // menu buttons
    private Sprite playAgainButtonSprite;
    private Sprite mainMenuButtonSprite;
    private Sprite continueButtonSprite;
    private Sprite pauseButtonSprite;
    private static final float PAUSE_BUTTON_MARGIN = 1.5f;

    // opponent AI
    private float opponentAiTimer;
    private boolean opponentAiMakingcontactDecision;
    private static final float OPPONENT_AI_CONTACT_DECISION_DELAY_EASY = 1f;
    private static final float OPPONENT_AI_CONTACT_DECISION_DELAY_MEDIUM = 0.07f;
    private static final float OPPONENT_AI_CONTACT_DECISION_DELAY_HARD = 0.01f;
    private static final float OPPONENT_AI_BLOCK_CHANCE = 0.4f; // decision chance
    private static final float OPPONENT_AI_ATTACK_CHANCE = 0.8f; // 8 out of 10 times
    private static final float OPPONENT_AI_NON_CONTACT_DECISION_DELAY = 0.5f;
    private boolean opponentAiPursuingPlayer;
    private static final float OPPONENT_AI_PURSUE_CHANCE_EASY = 0.2f; // 20% of time
    private static final float OPPONENT_AI_PURSUE_CHANCE_MEDIUM = 0.5f;
    private static final float OPPONENT_AI_PURSUE_CHANCE_HARD = 1f; // opponent is always going after the player

    // blood
    private boolean showingBlood = true;
    private BloodSplatter[] playerBloodSplatters;
    private BloodSplatter[] opponentBloodSplatters;
    private int currentPlayerBloodSplatterIndex;
    private int currentOpponentBloodSplatterIndex;
    public static final int BLOOD_SPLATTER_AMOUNT = 5;
    private static final float BLOOD_SPLATTER_OFFSET_X = 10f;
    private static final float BLOOD_SPLATTER_OFFSET_Y = 25f;


    public GameScreen(Main game) {
        this.game = game;

        // set up the viewport
        viewport = new ExtendViewport(GlobalVariables.WORLD_WIDTH, GlobalVariables.MIN_WORLD_HEIGHT,
            GlobalVariables.WORLD_WIDTH, 0);

        // create the game area
        createGameArea();

        // set up the fonts
        setupFonts();

        // set up the buttons
        createButtons();

        // create bloodsplatters and pools
        createBlood();
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

    private void createButtons() {
        // get button texture atlas from asset manager
        TextureAtlas buttonTextureAtlas = game.assets.manager.get(Assets.GAMEPLAY_BUTTONS_ATLAS);

        // END OF ROUND MENU
        // create the 'play again' button
        playAgainButtonSprite = new Sprite(buttonTextureAtlas.findRegion("PlayAgainButton")); // this text is inside the atlas file
        playAgainButtonSprite.setSize(playAgainButtonSprite.getWidth() * GlobalVariables.WORLD_SCALE,
            playAgainButtonSprite.getHeight() * GlobalVariables.WORLD_SCALE);
        // create Main menu button
        mainMenuButtonSprite = new Sprite(buttonTextureAtlas.findRegion("MainMenuButton"));
        mainMenuButtonSprite.setSize(mainMenuButtonSprite.getWidth() * GlobalVariables.WORLD_SCALE,
            mainMenuButtonSprite.getHeight() * GlobalVariables.WORLD_SCALE);

        // PAUSE MENU
        // create continue menu button
        continueButtonSprite = new Sprite(buttonTextureAtlas.findRegion("ContinueButton"));
        continueButtonSprite.setSize(continueButtonSprite.getWidth() * GlobalVariables.WORLD_SCALE,
            continueButtonSprite.getHeight() * GlobalVariables.WORLD_SCALE);
        // create pause menu button
        pauseButtonSprite = new Sprite(buttonTextureAtlas.findRegion("PauseButton"));
        pauseButtonSprite.setSize(pauseButtonSprite.getWidth() * GlobalVariables.WORLD_SCALE,
            pauseButtonSprite.getHeight() * GlobalVariables.WORLD_SCALE);
    }

    private void createBlood() {
        // init splatters
        playerBloodSplatters = new BloodSplatter[BLOOD_SPLATTER_AMOUNT];
        opponentBloodSplatters = new BloodSplatter[BLOOD_SPLATTER_AMOUNT];

        for (int i = 0; i < BLOOD_SPLATTER_AMOUNT; i++) {
            playerBloodSplatters[i] = new BloodSplatter(game);
            opponentBloodSplatters[i] = new BloodSplatter(game);
        }

        // set current blood splatter indexes to the start of the arrays
        currentOpponentBloodSplatterIndex = 0;
        currentPlayerBloodSplatterIndex = 0;
    }

    @Override
    public void show() {
        // have this GameScreen handle inputs
        Gdx.input.setInputProcessor(this);

        // start the game
        startGame();
    }

    private void startGame() {
        gameState = GameState.RUNNING;
        roundsWon = roundsLost = 0;

        // start round 1
        currentRound = 1;
        startRound();
    }

    private void pauseGame() {
        gameState = GameState.PAUSED;

        // pause game sounds and music
        game.audioManager.pauseGameSounds();
        game.audioManager.pauseMusic();
    }

    private void resumeGame() {
        gameState = GameState.RUNNING;
        // resume game sounds and music if enabled
        game.audioManager.resumeGameSounds();
        game.audioManager.playMusic();
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

        // play cheer sound
        game.audioManager.playSound(Assets.CHEER_SOUND);

        endRound();
    }

    private void loseRound() {
        game.player.lose();
        game.opponent.win();
        roundsLost++;

        // play boo sound
        game.audioManager.playSound(Assets.BOO_SOUND);

        endRound();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // update the game if not paused
        update(gameState == GameState.RUNNING ? delta : 0f); // 0f freezes the game

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

        // draw pause menu
        renderPauseButtons();

        // handle Round Over
        if (gameState == GameState.GAME_OVER) {
            renderGameOverOverlay();
        } else {
            // if the round starting, draw the start round text
            if (roundState == RoundState.STARTING) {
                renderStartRoundText();
            }

            // if the game is paused, draw pause overlay
            if (gameState == GameState.PAUSED) {
                renderPauseOverlay();
            }
        }

        // end drawing
        game.batch.end();

    }

    private void renderFighters() {
        // use the y coordinates to determine which fighter and bloodsplatter sprite is in front
        if (game.player.getPosition().y > game.opponent.getPosition().y) {
            game.player.render(game.batch);  // draw player first
            renderBloodSplatters(playerBloodSplatters); // render bloodsplatters if enabled
            game.opponent.render(game.batch);
            renderBloodSplatters(opponentBloodSplatters);
        } else {
            game.opponent.render(game.batch);  // draw opponent first
            renderBloodSplatters(opponentBloodSplatters);
            game.player.render(game.batch);
            renderBloodSplatters(playerBloodSplatters);
        }

    }

    private void renderBloodSplatters(BloodSplatter[] bloodSplatters) {
        if (showingBlood) {
            for (BloodSplatter bloodSplatter : bloodSplatters) {
                bloodSplatter.render(game.batch);
            }
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

    private void renderPauseButtons() {
        pauseButtonSprite.setPosition(viewport.getWorldWidth() - PAUSE_BUTTON_MARGIN - pauseButtonSprite.getWidth(),
            PAUSE_BUTTON_MARGIN);
        pauseButtonSprite.draw(game.batch);
    }

    private void renderGameOverOverlay() {
        // darken screen and add transparency
        game.batch.end();

        // enable texture blending so transparency displays textures behind it
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // set dark semi transparent overlay
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, 0.7f);
        game.shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND); // remove blending
        game.batch.begin();

        // calculate layout dimensions placing buttons from bottom to top
        float textMarginBottom = 2f;
        float buttonSpacing = 0.5f;
        float layoutHeight = largeFont.getCapHeight() + textMarginBottom + playAgainButtonSprite.getHeight() +
            buttonSpacing + mainMenuButtonSprite.getHeight();
        float layoutPositionY = viewport.getWorldHeight() / 2f - layoutHeight / 2f;

        // draw the buttons
        mainMenuButtonSprite.setPosition(viewport.getWorldWidth() / 2f - mainMenuButtonSprite.getWidth() / 2f, layoutPositionY);
        mainMenuButtonSprite.draw(game.batch);
        playAgainButtonSprite.setPosition(viewport.getWorldWidth() / 2f - playAgainButtonSprite.getWidth() / 2f,
            layoutPositionY + mainMenuButtonSprite.getHeight() + buttonSpacing);
        playAgainButtonSprite.draw(game.batch);

        // draw the end of fight text
        String text = roundsWon > roundsLost ? "YOU WON!" : "YOU LOST!";
        largeFont.draw(game.batch, text, viewport.getWorldWidth() / 2f,
            playAgainButtonSprite.getY() + playAgainButtonSprite.getHeight() +
                textMarginBottom + largeFont.getCapHeight(), 0, Align.center, false);
    }

    private void renderPauseOverlay() {
        // darken screen and add transparency
        game.batch.end();

        // enable texture blending so transparency displays textures behind it
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // set dark semi transparent overlay
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, 0.7f);
        game.shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND); // remove blending
        game.batch.begin();

        // calculate layout dimensions placing buttons from bottom to top
        float textMarginBottom = 2f;
        float buttonSpacing = 0.5f;
        float layoutHeight = largeFont.getCapHeight() + textMarginBottom + continueButtonSprite.getHeight() +
            buttonSpacing + mainMenuButtonSprite.getHeight();
        float layoutPositionY = viewport.getWorldHeight() / 2f - layoutHeight / 2f;

        // draw the buttons
        mainMenuButtonSprite.setPosition(viewport.getWorldWidth() / 2f - mainMenuButtonSprite.getWidth() / 2f, layoutPositionY);
        mainMenuButtonSprite.draw(game.batch);
        continueButtonSprite.setPosition(viewport.getWorldWidth() / 2f - continueButtonSprite.getWidth() / 2f,
            layoutPositionY + mainMenuButtonSprite.getHeight() + buttonSpacing);
        continueButtonSprite.draw(game.batch);

        // draw the pause menu top text
        largeFont.draw(game.batch, "GAME PAUSED", viewport.getWorldWidth() / 2f,
            continueButtonSprite.getY() + continueButtonSprite.getHeight() +
                textMarginBottom + largeFont.getCapHeight(), 0, Align.center, false);
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

        // update the blood splatters
        for (int i = 0; i < BLOOD_SPLATTER_AMOUNT; i++) {
            playerBloodSplatters[i].update(delta);
            opponentBloodSplatters[i].update(delta);
        }

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
                if (game.player.getLife() >= game.opponent.getLife()) {
                    winRound();
                } else {
                    loseRound();
                }
            }

            // perform opponent AI action
            performOpponentAi(delta);

            // round in progress. Check if fighters are within contact distance
            if (areWithinContactDistance(game.player.getPosition(), game.opponent.getPosition())) {
                if (game.player.isAttackActive()) {
                    // PLAYER HITSCAN LOGIC
                    // if within contact distance AND while attacking, apply hit
                    game.opponent.getHit(Fighter.HIT_STRENGTH);

                    if (game.opponent.isBlocking()) {
                        game.audioManager.playSound(Assets.BLOCK_SOUND);
                    } else {
                        game.audioManager.playSound(Assets.HIT_SOUND);
                        spillBlood(game.opponent);
                    }

                    // deactivate player attack after contact
                    game.player.makeContact();

                    // check if opponent has lost
                    if (game.opponent.hasLost()) {
                        winRound();
                    }
                } else if (game.opponent.isAttackActive()) {
                    // OPPONENT HITSCAN LOGIC
                    // if within contact distance AND while attacking, apply hit
                    game.player.getHit(Fighter.HIT_STRENGTH);

                    if (game.player.isBlocking()) {
                        game.audioManager.playSound(Assets.BLOCK_SOUND);
                    } else {
                        game.audioManager.playSound(Assets.HIT_SOUND);
                        spillBlood(game.player);
                    }

                    // deactivate opponent attack after contact
                    game.opponent.makeContact();

                    // check if player has lost
                    if (game.player.hasLost()) {
                        loseRound();
                    }
                }
            }
        }
    }

    private void spillBlood(Fighter fighter) {
        // use given fighter to use the correct splatter array and current index
        BloodSplatter[] bloodSplatters;
        int currentBloodSplatterIndex;

        if (fighter.equals(game.player)) {
            bloodSplatters = playerBloodSplatters;
            currentBloodSplatterIndex = currentPlayerBloodSplatterIndex;
        } else {
            bloodSplatters = opponentBloodSplatters;
            currentBloodSplatterIndex = currentOpponentBloodSplatterIndex;
        }

        // activate current blood splatter in the array
        bloodSplatters[currentBloodSplatterIndex].activate(
            fighter.getPosition().x + BLOOD_SPLATTER_OFFSET_X,
            fighter.getPosition().y + BLOOD_SPLATTER_OFFSET_Y);

        // increment the correct current blood splatter index or return to the first if at end of the array
        if (fighter.equals(game.player)) {
            if (currentPlayerBloodSplatterIndex < BLOOD_SPLATTER_AMOUNT - 1) {
                currentPlayerBloodSplatterIndex++;
            } else {
                currentPlayerBloodSplatterIndex = 0; // reset back to the first
            }
        } else {
            if (currentOpponentBloodSplatterIndex < BLOOD_SPLATTER_AMOUNT - 1) {
                currentOpponentBloodSplatterIndex++;
            } else {
                currentOpponentBloodSplatterIndex = 0; // reset back to the first
            }
        }
    }

    private void keepWithinRingBounds(Vector2 position) {
        if (position.y < RING_MIN_Y) {
            position.y = RING_MIN_Y;
        } else if (position.y > RING_MAX_Y) {
            position.y = RING_MAX_Y;
        }
        if (position.x < position.y / RING_SLOPE + RING_MIN_X) {
            position.x = position.y / RING_SLOPE + RING_MIN_X;
        } else if (position.x > position.y / -RING_SLOPE + RING_MAX_X) {
            position.x = position.y / -RING_SLOPE + RING_MAX_X;
        }
    }

    private boolean areWithinContactDistance(Vector2 position1, Vector2 position2) {
        // determine if contact is possible
        float xDistance = Math.abs(position1.x - position2.x); // we want positive values only
        float yDistance = Math.abs(position1.y - position2.y);
        return xDistance <= FIGHTER_CONTACT_DISTANCE_X && yDistance <= FIGHTER_CONTACT_DISTANCE_Y;
    }

    private void performOpponentAi(float delta) {
        // check if opponent is making a contact decision (attack, block, etc.)
        if (opponentAiMakingcontactDecision) {
            if (game.opponent.isBlocking()) {
                // stop blocking if not within contact distance, if player isnt attacking or player has attacked and already made contact
                if (!areWithinContactDistance(game.player.getPosition(), game.opponent.getPosition()) ||
                    !game.player.isAttacking() || game.player.hasMadeContact()) {
                    game.opponent.stopBlocking();
                }
            } else if (!game.opponent.isAttacking()) {
                // if opponent isnt currently attacking, check if within contact distance
                if (areWithinContactDistance(game.player.getPosition(), game.opponent.getPosition())) {
                    if (opponentAiTimer <= 0f) {
                        // make an AI contact decision after delay timer is up
                        opponentAiMakeContactDecision();
                    } else {
                        // decrease the opponent AI timer by delta time
                        opponentAiTimer -= delta;
                    }
                } else {
                    // no decision if not within contact distance
                    opponentAiMakingcontactDecision = false;
                }
            }
        } else {
            if (areWithinContactDistance(game.player.getPosition(), game.opponent.getPosition())) {
                // if within contact AND AI not currently making a decision
                opponentAiMakeContactDecision();
            } else {
                if (opponentAiTimer <= 0f) {
                    // pursue player or move to a random direction
                    float pursueChance = difficulty == GlobalVariables.Difficulty.EASY ? OPPONENT_AI_PURSUE_CHANCE_EASY :
                        difficulty == GlobalVariables.Difficulty.MEDIUM ? OPPONENT_AI_PURSUE_CHANCE_MEDIUM :
                            OPPONENT_AI_PURSUE_CHANCE_HARD;
                    if (MathUtils.random() <= pursueChance) {
                        // opponent is pursuing player
                        opponentAiPursuingPlayer = true;
                        // move in the direction of the player
                        opponentAiMoveTowardPlayer();
                    } else {
                        opponentAiPursuingPlayer = false;
                        // move in a random direction
                        opponentAiMoveRandomly();
                    }
                    // set timer to non-contact decision delay
                    opponentAiTimer = OPPONENT_AI_NON_CONTACT_DECISION_DELAY;
                } else {
                    // if opponent is pursuing player, move in direction of the player
                    if (opponentAiPursuingPlayer) {
                        opponentAiMoveTowardPlayer();
                    }
                    opponentAiTimer -= delta; // decrease opponent AI Timer
                }
            }
        }
    }

    private void opponentAiMakeContactDecision() {
        opponentAiMakingcontactDecision = true;

        //check if player is attacking
        if (game.player.isAttacking()) {
            // block or move away if attack hasnt made contact yet
            if (!game.player.hasMadeContact()) {
                if (MathUtils.random() <= OPPONENT_AI_BLOCK_CHANCE) {
                    game.opponent.block();
                } else {
                    opponentAiMoveAwayFromPlayer();
                }
            }
        } else {
            // attack or move away if player isnt attacking
            if (MathUtils.random() <= OPPONENT_AI_ATTACK_CHANCE) {
                if (MathUtils.random(1) == 0) {
                    game.opponent.punch();
                } else { // 50/50 chance to punch or kick
                    game.opponent.kick();
                }
            } else {
                opponentAiMoveAwayFromPlayer();
            }
        }

        // set the opponent AI timer based on difficulty
        switch (difficulty) {
            case EASY:
                opponentAiTimer = OPPONENT_AI_CONTACT_DECISION_DELAY_EASY;
                break;
            case MEDIUM:
                opponentAiTimer = OPPONENT_AI_CONTACT_DECISION_DELAY_MEDIUM;
                break;
            case HARD:
            default:
                opponentAiTimer = OPPONENT_AI_CONTACT_DECISION_DELAY_HARD;
        }
    }

    private void opponentAiMoveTowardPlayer() {
        Vector2 playerPos = game.player.getPosition();
        Vector2 opponentPos = game.opponent.getPosition();

        // check X position
        if (opponentPos.x > playerPos.x + FIGHTER_CONTACT_DISTANCE_X) {
            game.opponent.moveLeft();
        } else if (opponentPos.x < playerPos.x - FIGHTER_CONTACT_DISTANCE_X) {
            game.opponent.moveRight();
        } else {
            game.opponent.stopMovingLeft();
            game.opponent.stopMovingRight();
        }
        // check Y position
        if (opponentPos.y < playerPos.y + FIGHTER_CONTACT_DISTANCE_Y) {
            game.opponent.moveUp();
        } else if (opponentPos.y > playerPos.y - FIGHTER_CONTACT_DISTANCE_Y) {
            game.opponent.moveDown();
        } else {
            game.opponent.stopMovingUp();
            game.opponent.stopMovingDown();
        }
    }

    private void opponentAiMoveRandomly() {
        // random x move
        switch (MathUtils.random(2)) {
            case 0:
                game.opponent.moveLeft();
                break;
            case 1:
                game.opponent.moveRight();
                break;
            case 2:
            default:
                game.opponent.stopMovingLeft();
                game.opponent.stopMovingRight();
        }
        // random y move
        switch (MathUtils.random(2)) {
            case 0:
                game.opponent.moveUp();
                break;
            case 1:
                game.opponent.moveDown();
                break;
            case 2:
            default:
                game.opponent.stopMovingUp();
                game.opponent.stopMovingDown();
        }
    }

    private void opponentAiMoveAwayFromPlayer() {
        Vector2 playerPos = game.player.getPosition();
        Vector2 opponentPos = game.opponent.getPosition();

        if (opponentPos.x > playerPos.x) {
            game.opponent.moveRight();
        } else {
            game.opponent.moveLeft();
        }
        if (opponentPos.y > playerPos.y) {
            game.opponent.moveUp();
        } else {
            game.opponent.moveDown();
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
        // pause game when minimized
        if (gameState == GameState.RUNNING) {
            pauseGame();
        }

        // pause music
        game.audioManager.pauseMusic();
    }

    @Override
    public void resume() {
        game.audioManager.playMusic();
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
        } else if ((gameState == GameState.RUNNING || gameState == GameState.PAUSED) && keycode == Input.Keys.P) {
            // if the game is running or paused and P key is pressed, pause or resume game
            if (gameState == GameState.RUNNING) {
                pauseGame();
            } else {
                resumeGame();
            }
        } else if (keycode == Input.Keys.M) {
            // togle music on or off
            game.audioManager.toggleMusic();
        } else if (keycode == Input.Keys.N) {
            // switch difficulty
            switch (difficulty) {
                case EASY:
                    difficulty = GlobalVariables.Difficulty.MEDIUM;
                    break;
                case MEDIUM:
                    difficulty = GlobalVariables.Difficulty.HARD;
                    break;
                case HARD:
                default:
                    difficulty = GlobalVariables.Difficulty.EASY;
                    break;
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
            if (pauseButtonSprite.getBoundingRectangle().contains(position.x, position.y)) {
                // pause btn was clicked
                pauseGame();
                // play click sound
                game.audioManager.playSound(Assets.CLICK_SOUND);
            } else if (roundState == RoundState.STARTING) {
                // if the round is starting and screen has been clicked/touched, skip start round delay
                roundStateTime = START_ROUND_DELAY;
            } else if (roundState == RoundState.ENDING) {
                roundStateTime = END_ROUND_DELAY; // same for round ending delay
            }
        } else {
            if (gameState == GameState.GAME_OVER &&
                playAgainButtonSprite.getBoundingRectangle().contains(position.x, position.y)) {
                // 'play again button' clicked
                startGame();
                // play click sound
                game.audioManager.playSound(Assets.CLICK_SOUND);
            } else if (gameState == GameState.PAUSED && continueButtonSprite.getBoundingRectangle().contains(position.x, position.y)) {
                // if game is paused and continue button clicked, resume game
                resumeGame();
                // play click sound
                game.audioManager.playSound(Assets.CLICK_SOUND);
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
