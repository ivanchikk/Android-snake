package com.example.android_snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class CanvasView extends View {
    private Paint snakeHead;
    private Paint snakeBody;
    private Paint food;
    private Paint level;

    public CanvasView(Context context) {
        super(context);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        snakeHead = new Paint();
        snakeHead.setColor(Color.YELLOW);

        snakeBody = new Paint();
        snakeBody.setColor(Color.GREEN);

        food = new Paint();
        food.setColor(Color.RED);

        level = new Paint();
        level.setColor(Color.DKGRAY);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // draw field
        canvas.drawRect(0f, 0f, GameConfig.FIELD_WIDTH, GameConfig.FIELD_HEIGHT, level);

        // draw snake
        Part head = Snake.getHead();
        canvas.drawRect(head.x, head.y, head.x + Part.SIZE, head.y + Part.SIZE, snakeHead);

        for (int i = 1; i < Snake.bodyParts.size(); i++) {
            Part part = Snake.bodyParts.get(i);
            canvas.drawRect(part.x, part.y, part.x + Part.SIZE, part.y + Part.SIZE, snakeBody);
        }

        // draw food
        canvas.drawRect(Food.food.x, Food.food.y, Food.food.x + Part.SIZE, Food.food.y + Part.SIZE, food);
    }
}
