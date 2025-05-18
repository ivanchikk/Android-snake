package com.example.android_snake;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    public static Part head;
    public static List<Part> bodyParts = new ArrayList<>();
    public static Direction direction;
    public static boolean alive;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    static {
        reset();
    }

    public static void reset() {
        head = new Part(0f, 0f);
        bodyParts.clear();
        bodyParts.add(new Part(0f, 0f));
        direction = Direction.RIGHT;
        alive = true;
    }

    public static boolean canMove() {
        return head.x >= 0f && head.x <= GameConfig.FIELD_WIDTH - GameConfig.SEGMENT_SIZE &&
                head.y >= 0f && head.y <= GameConfig.FIELD_HEIGHT - GameConfig.SEGMENT_SIZE;
    }
}
