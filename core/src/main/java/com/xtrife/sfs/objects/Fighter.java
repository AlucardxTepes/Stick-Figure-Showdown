package com.xtrife.sfs.objects;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.xtrife.sfs.Main;
import com.xtrife.sfs.resources.Assets;

/**
 * Created by 9S on 2/24/2025 - 11:31 PM.
 */
public class Fighter {
    // num of frame rows and columns in each anim sprite sheet
    public static final int FRAME_ROWS = 2, FRAME_COLS = 3;

    // how fast a fighter can move
    public static final float MOVEMENT_SPEED = 10f;

    // max life
    public static final float MAX_LIFE = 100f;

    public static final float HIT_STRENGTH = 5f;
    // Damage decrease when blocking
    public static final float BLOCK_DAMAGE_FACTOR = 0.2f; // 5 * 0.2 = 1 damage reduction


    // distinguishing details
    private String name;
    private Color color;

    // state
    public enum State {
        BLOCK,
        HURT,
        IDLE,
        KICK,
        LOSE,
        PUNCH,
        WALK,
        WIN
    }

    private State state;
    private float stateTime;
    private State renderState;
    private float renderStateTime;
    private final Vector2 position = new Vector2();
    private final Vector2 movementDirection = new Vector2();
    private float life; // HP
    private int facing;
    private boolean madeContact; // attack hit or miss

    // animations
    private Animation<TextureRegion> blockAnimation;
    private Animation<TextureRegion> hurtAnimation;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> kickAnimation;
    private Animation<TextureRegion> loseAnimation;
    private Animation<TextureRegion> punchAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> winAnimation;

    public Fighter(Main game, String name, Color color) {
        this.name = name;
        this.color = color;

        // init animations
        initializeBlockAnimation(game.assets.manager);
        initializeHurtAnimation(game.assets.manager);
        initializeIdleAnimation(game.assets.manager);
        initializeKickAnimation(game.assets.manager);
        initializeLoseAnimation(game.assets.manager);
        initializePunchAnimation(game.assets.manager);
        initializeWalkAnimation(game.assets.manager);
        initializeWinAnimation(game.assets.manager);
    }

    private void initializeBlockAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.BLOCK_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        blockAnimation = new Animation<>(0.05f, frames); // duration in seconds for each frame
    }
    private void initializeHurtAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.HURT_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        hurtAnimation = new Animation<>(0.03f, frames); // duration in seconds for each frame
    }
    private void initializeIdleAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.IDLE_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        idleAnimation = new Animation<>(0.01f, frames); // duration in seconds for each frame
    }
    private void initializeKickAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.KICK_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        kickAnimation = new Animation<>(0.05f, frames); // duration in seconds for each frame
    }
    private void initializeLoseAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.LOSE_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        loseAnimation = new Animation<>(0.05f, frames); // duration in seconds for each frame
    }
    private void initializePunchAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.PUNCH_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        punchAnimation = new Animation<>(0.05f, frames); // duration in seconds for each frame
    }
    private void initializeWalkAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.WALK_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        walkAnimation = new Animation<>(0.08f, frames); // duration in seconds for each frame
    }
    private void initializeWinAnimation(AssetManager assetManager) {
        Texture spriteSheet = assetManager.get(Assets.WIN_SPRITE_SHEET);
        TextureRegion[] frames = getAnimationFrames(spriteSheet);
        winAnimation = new Animation<>(0.05f, frames); // duration in seconds for each frame
    }

    private TextureRegion[] getAnimationFrames(Texture spriteSheet) {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / FRAME_COLS,
            spriteSheet.getHeight() / FRAME_ROWS);
        // Convert 2D array to 1D array
        TextureRegion[] frames = new TextureRegion[FRAME_ROWS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return frames;
    }


}
