package com.example.android_snake;

import java.util.Random;

public class Food {
    public static Part food;
    private static final Random random = new Random();

    public static void generate() {
        int maxX = (int) ((GameConfig.FIELD_WIDTH - Part.SIZE) / GameConfig.STEP);
        int maxY = (int) ((GameConfig.FIELD_HEIGHT - Part.SIZE) / GameConfig.STEP);
        float x = random.nextInt(maxX + 1) * GameConfig.STEP;
        float y = random.nextInt(maxY + 1) * GameConfig.STEP;
        food = new Part(x, y);
    }
}
