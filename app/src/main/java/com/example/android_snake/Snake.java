package com.example.android_snake;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    public static List<Part> bodyParts = new ArrayList<>();
    public static Part head;
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

    public static void changeDirection(Direction newDirection) {
        if ((direction == Direction.UP && newDirection != Direction.DOWN) ||
                (direction == Direction.DOWN && newDirection != Direction.UP) ||
                (direction == Direction.LEFT && newDirection != Direction.RIGHT) ||
                (direction == Direction.RIGHT && newDirection != Direction.LEFT)) {
            direction = newDirection;
        }
    }

    public static boolean canMove() {
        return head.x >= 0f && head.x <= GameConfig.FIELD_WIDTH - GameConfig.SEGMENT_SIZE &&
                head.y >= 0f && head.y <= GameConfig.FIELD_HEIGHT - GameConfig.SEGMENT_SIZE;
    }

    public static void move() {
        // Add part at the old head
        bodyParts.add(0, new Part(head.x, head.y));

        // Remove part at the end
        bodyParts.remove(bodyParts.size() - 1);

        // Update head position
        switch (direction) {
            case UP:
                head.y += GameConfig.STEP;
                break;
            case DOWN:
                head.y -= GameConfig.STEP;
                break;
            case LEFT:
                head.x -= GameConfig.STEP;
                break;
            case RIGHT:
                head.x += GameConfig.STEP;
                break;
        }
    }

    public static void grow() {
        Part tail = bodyParts.get(bodyParts.size() - 1);
        bodyParts.add(new Part(tail.x, tail.y));
    }

    public static boolean hasCollisionWithSelf() {
        for (int i = 3; i < bodyParts.size(); i++) {
            if (head.isAtSamePosition(bodyParts.get(i))) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPartOfSnake(Part part) {
        if (part.isAtSamePosition(head))
            return true;

        for (Part bodyPart : bodyParts) {
            if (part.isAtSamePosition(bodyPart)) {
                return true;
            }
        }

        return false;
    }
}
