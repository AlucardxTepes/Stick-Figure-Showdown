package com.xtrife.sfs.resources;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Created by 9S on 2/24/2025 - 10:43 PM.
 */
public class Assets {
    // asset manager
    public final AssetManager manager = new AssetManager();

    // gameplay assets
    public static final String BACKGROUND_TEXTURE = "textures/Background.png";
    public static final String FRONT_ROPES_TEXTURE = "textures/FrontRopes.png";
    public static final String IDLE_SPRITE_SHEET = "sprites/IdleSpriteSheet.png";
    public static final String WALK_SPRITE_SHEET = "sprites/WalkSpriteSheet.png";
    public static final String PUNCH_SPRITE_SHEET = "sprites/PunchSpriteSheet.png";
    public static final String KICK_SPRITE_SHEET = "sprites/KickSpriteSheet.png";
    public static final String HURT_SPRITE_SHEET = "sprites/HurtSpriteSheet.png";
    public static final String BLOCK_SPRITE_SHEET = "sprites/BlockSpriteSheet.png";
    public static final String WIN_SPRITE_SHEET = "sprites/WinSpriteSheet.png";
    public static final String LOSE_SPRITE_SHEET = "sprites/LoseSpriteSheet.png";
    public static final String GAMEPLAY_BUTTONS_ATLAS = "textures/GameplayButtons.atlas";
    public static final String BLOOD_ATLAS = "textures/Blood.atlas";

    // fonts
    public static final String ROBOTO_REGULAR = "fonts/Roboto-Regular.ttf";
    public static final String SMALL_FONT = "smallFont.ttf";
    public static final String MEDIUM_FONT = "mediumFont.ttf";
    public static final String LARGE_FONT = "largeFont.ttf";

    // audio assets
    public static final String BLOCK_SOUND = "audio/block.mp3";
    public static final String BOO_SOUND = "audio/boo.mp3";
    public static final String CHEER_SOUND = "audio/cheer.mp3";
    public static final String CLICK_SOUND = "audio/click.mp3";
    public static final String HIT_SOUND = "audio/hit.mp3";
    public static final String MUSIC = "audio/music.ogg";

    // menu assets
    public static final String MENU_ITEMS_ATLAS = "textures/MenuItems.atlas";

    public void load() {
        // load all assets
        loadGameplayAssets();
    }

    private void loadGameplayAssets() {
        manager.load(BACKGROUND_TEXTURE, Texture.class);
        manager.load(FRONT_ROPES_TEXTURE, Texture.class);
        manager.load(IDLE_SPRITE_SHEET, Texture.class);
        manager.load(WALK_SPRITE_SHEET, Texture.class);
        manager.load(PUNCH_SPRITE_SHEET, Texture.class);
        manager.load(KICK_SPRITE_SHEET, Texture.class);
        manager.load(HURT_SPRITE_SHEET, Texture.class);
        manager.load(BLOCK_SPRITE_SHEET, Texture.class);
        manager.load(WIN_SPRITE_SHEET, Texture.class);
        manager.load(LOSE_SPRITE_SHEET, Texture.class);
        manager.load(GAMEPLAY_BUTTONS_ATLAS, TextureAtlas.class);
        manager.load(BLOOD_ATLAS, TextureAtlas.class);
    }

    public void dispose() {
        manager.dispose();
    }

}
