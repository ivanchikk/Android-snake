package com.example.android_snake;

public class Part {
    public static final float SIZE = GameConfig.SEGMENT_SIZE;
    public float x;
    public float y;

    public Part(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isAtSamePosition(Part other) {
        return this.x == other.x && this.y == other.y;
    }
}
