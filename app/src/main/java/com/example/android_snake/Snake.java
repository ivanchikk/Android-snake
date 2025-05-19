package com.example.android_snake;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    public static List<Part> bodyParts = new ArrayList<>();
    public static Part head;
    public static Direction currentDirection;
    public static Direction nextDirection;
    public static boolean alive;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    static {
        reset();
    }

    public static void reset() {
        head = new Part(50f, 0f);
        bodyParts.clear();
//        bodyParts.add(head);
        bodyParts.add(new Part(0f, 0f));
        currentDirection = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        alive = true;
    }

    public static void changeDirection() {
        if ((currentDirection == Direction.UP && nextDirection != Direction.DOWN) ||
                (currentDirection == Direction.DOWN && nextDirection != Direction.UP) ||
                (currentDirection == Direction.LEFT && nextDirection != Direction.RIGHT) ||
                (currentDirection == Direction.RIGHT && nextDirection != Direction.LEFT)) {
            currentDirection = nextDirection;
        }
    }

    public static boolean canMove() {
        return head.x >= 0f && head.x <= GameConfig.FIELD_WIDTH - GameConfig.SEGMENT_SIZE &&
                head.y >= 0f && head.y <= GameConfig.FIELD_HEIGHT - GameConfig.SEGMENT_SIZE;
    }

    public static void move() {
        changeDirection();

        // Add part at the old head
        bodyParts.add(0, new Part(head.x, head.y));

        // Update head position
        switch (currentDirection) {
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

        // Remove part at the end
        bodyParts.remove(bodyParts.size() - 1);
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
