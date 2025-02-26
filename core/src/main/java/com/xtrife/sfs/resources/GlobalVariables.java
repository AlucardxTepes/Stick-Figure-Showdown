package com.xtrife.sfs.resources;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by 9S on 2/24/2025 - 11:05 PM.
 */
public class GlobalVariables {
    // window
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    // world
    public static final float WORLD_WIDTH = 192f;
    public static final float WORLD_HEIGHT = 108f;
    public static final float MIN_WORLD_HEIGHT  = WORLD_HEIGHT * 0.85f;
    public static final float WORLD_SCALE = 0.12f;

    // Colors
    public static final Color GOLD = new Color(0.94f, 0.85f, 0.32f, 1f);

    // Game
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

}
