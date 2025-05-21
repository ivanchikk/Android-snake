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
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // draw snake
        Part head = Snake.head;
        canvas.drawRect(head.x + GameConfig.DRAW_OFFSET, head.y + GameConfig.DRAW_OFFSET, head.x + Part.SIZE + GameConfig.DRAW_OFFSET, head.y + Part.SIZE + GameConfig.DRAW_OFFSET, snakeHead);

        for (Part part : Snake.bodyParts) {
            canvas.drawRect(part.x + GameConfig.DRAW_OFFSET, part.y + GameConfig.DRAW_OFFSET, part.x + Part.SIZE + GameConfig.DRAW_OFFSET, part.y + Part.SIZE + GameConfig.DRAW_OFFSET, snakeBody);
        }

        // draw food
        canvas.drawRect(Food.food.x + GameConfig.DRAW_OFFSET, Food.food.y + GameConfig.DRAW_OFFSET, Food.food.x + Part.SIZE + GameConfig.DRAW_OFFSET, Food.food.y + Part.SIZE + GameConfig.DRAW_OFFSET, food);
    }
}
