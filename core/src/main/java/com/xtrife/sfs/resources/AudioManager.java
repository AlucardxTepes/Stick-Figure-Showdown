package com.xtrife.sfs.resources;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;

/**
 * Created by 9S on 2/25/2025 - 9:52 PM.
 */
public class AudioManager {
    // settings
    private boolean musicEnabled = true;
    private boolean soundsEnabled = true;

    // music
    private final Music music;

    // UI sounds
    private final Sound clickSound;

    // Game sounds
    private final Sound blockSound;
    private final Sound booSound;
    private final Sound cheerSound;
    private final Sound hitSound;

    private final ArrayList<Sound> allGameSounds;

    public AudioManager(AssetManager assetManager) {
        music = assetManager.get(Assets.MUSIC);
        clickSound = assetManager.get(Assets.CLICK_SOUND);
        blockSound = assetManager.get(Assets.BLOCK_SOUND);
        booSound = assetManager.get(Assets.BOO_SOUND);
        cheerSound = assetManager.get(Assets.CHEER_SOUND);
        hitSound = assetManager.get(Assets.HIT_SOUND);

        // array list with all sounds for easy playback of all sounds at once
        allGameSounds = new ArrayList<>();
        allGameSounds.add(clickSound);
        allGameSounds.add(blockSound);
        allGameSounds.add(booSound);
        allGameSounds.add(cheerSound);
        allGameSounds.add(hitSound);

        music.setLooping(true);
    }

    public void enableMusic() {
        musicEnabled = true;
        if (!music.isPlaying()) {
            music.play();
        }
    }

    public void disableMusic() {
        musicEnabled = false;
        if (music.isPlaying()) {
            music.stop();
        }
    }

    public void toggleMusic() {
        // enable or disable music
        if (musicEnabled) {
            disableMusic();
        } else {
            enableMusic();
        }
    }

    /**
     * play game music without enabling it / switching it on
     */
    public void playMusic() {
        if (musicEnabled && !music.isPlaying()) {
            music.play();
        }
    }

    public void pauseMusic() {
        if (musicEnabled && music.isPlaying()) {
            music.pause();
        }
    }

    public void enableSounds() {
        soundsEnabled = true;
    }

    public void disableSounds() {
        soundsEnabled = false;
    }

    public void playSound(String soundAsset) {
        if (soundsEnabled) {
            switch (soundAsset) {
                case Assets.CLICK_SOUND:
                    clickSound.play();
                    break;
                case Assets.BLOCK_SOUND:
                    blockSound.play();
                    break;
                case Assets.BOO_SOUND:
                    booSound.play();
                    break;
                case Assets.CHEER_SOUND:
                    cheerSound.play();
                    break;
                case Assets.HIT_SOUND:
                    hitSound.play();
                    break;
            }
        }
    }

    public void pauseGameSounds() {
        // pause any instances of game sounds
        for (Sound sound : allGameSounds) {
            sound.pause();
        }
    }

    public void resumeGameSounds() {
        for (Sound sound : allGameSounds) {
            sound.resume();
        }
    }

    public void stopGameSounds() {
        for (Sound sound : allGameSounds) {
            sound.stop();
        }
    }
}
