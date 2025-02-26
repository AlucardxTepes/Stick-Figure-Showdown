package com.xtrife.sfs.objects;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.xtrife.sfs.Main;
import com.xtrife.sfs.resources.Assets;
import com.xtrife.sfs.resources.GlobalVariables;

/**
 * Created by 9S on 2/26/2025 - 12:32 PM.
 */
public class BloodSplatter {
    // state
    private float stateTime;
    private boolean active;
    private final Vector2 position = new Vector2();

    // animation
    private Animation<TextureRegion> splatterAnimation;

    public BloodSplatter(Main game) {
        // init state
        stateTime = 0f;
        active = false;

        // init splatter animation
        initializeSplatterAnimation(game.assets.manager);
    }

    private void initializeSplatterAnimation(AssetManager assetManager) {
        TextureAtlas bloodAtlas = assetManager.get(Assets.BLOOD_ATLAS);
        splatterAnimation = new Animation<>(0.03f, bloodAtlas.findRegions("BloodSplatter"));
    }

    public void activate(float posX, float posY) {
        active = true;
        stateTime = 0f;
        position.set(posX, posY);
    }

    public void deactivate() {
        active = false;
    }

    public void update(float delta) {
        // if not active, dont update
        if (!active) return;

        // increment state time
        stateTime += delta;

        // if splatter animation has finished, deactive splatter
        if (splatterAnimation.isAnimationFinished(stateTime)) {
            deactivate();
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        // draw the current animation frame
        TextureRegion currentFrame = splatterAnimation.getKeyFrame(stateTime);
        batch.draw(currentFrame, position.x, position.y,
            currentFrame.getRegionWidth() * GlobalVariables.WORLD_SCALE,
            currentFrame.getRegionHeight() * GlobalVariables.WORLD_SCALE);
    }
}
