package com.example.android_snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

@SuppressLint({"ClickableViewAccessibility", "SourceLockedOrientationActivity"})
public class MainActivity extends AppCompatActivity {
    private CanvasView canvasView;
    private TextView scoreTextView;
    private TextView bestScoreTextView;
    private Handler gameHandler;
    private Runnable gameRunnable;
    private boolean isGameRunning = false;
    private int gameSpeed = GameConfig.START_SPEED;
    private int score = 0;
    private int bestScore = 0;
    private Snake.Direction snakeDirection = Snake.Direction.RIGHT;

    private ConstraintLayout mainMenuLayout;
    private ConstraintLayout gameLayout;
    private ConstraintLayout gameOverLayout;
    private TextView finalScoreTextView;

    private static final String PREFS_NAME = "SnakeGamePrefs";
    private static final String BEST_SCORE_KEY = "BestScore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mainMenuLayout = findViewById(R.id.main_menu_layout);
        gameLayout = findViewById(R.id.game_layout);
        gameOverLayout = findViewById(R.id.game_over_layout);

        canvasView = findViewById(R.id.canvas);
        scoreTextView = findViewById(R.id.score);
        bestScoreTextView = findViewById(R.id.best_score);
        finalScoreTextView = findViewById(R.id.final_score);

        Button startButton = findViewById(R.id.start_button);
        Button backButton = findViewById(R.id.back_button);

        gameHandler = new Handler(Looper.getMainLooper());
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    updateGame();
                    gameHandler.postDelayed(this, gameSpeed);
                }
            }
        };

        startButton.setOnClickListener(v -> startGame());
        backButton.setOnClickListener(v -> showMainMenu());

        // touch control
        canvasView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                snakeDirection = Snake.Direction.LEFT;
            }

            @Override
            public void onSwipeRight() {
                snakeDirection = Snake.Direction.RIGHT;
            }

            @Override
            public void onSwipeTop() {
                snakeDirection = Snake.Direction.UP;
            }

            @Override
            public void onSwipeBottom() {
                snakeDirection = Snake.Direction.DOWN;
            }
        });

        loadBestScore();
        updateBestScore();
        showMainMenu();
    }

    private void showMainMenu() {
        mainMenuLayout.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);

        stopGameLoop();
    }

    private void showGameScreen() {
        mainMenuLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);
    }

    private void showGameOverScreen() {
        mainMenuLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.VISIBLE);

        finalScoreTextView.setText(String.format(Locale.ENGLISH, "Your score: %d", score));
    }

    private void startGame() {
        Snake.reset();
        generateFood();
        score = 0;
        gameSpeed = GameConfig.START_SPEED;
        snakeDirection = Snake.direction;

        updateScore();
        showGameScreen();
        startGameLoop();
    }

    private void endGame() {
        stopGameLoop();

        if (score > bestScore) {
            bestScore = score;
            saveBestScore();
            updateBestScore();
        }

        showGameOverScreen();
    }

    private void startGameLoop() {
        if (!isGameRunning) {
            isGameRunning = true;
            gameHandler.postDelayed(gameRunnable, gameSpeed);
        }
    }

    private void stopGameLoop() {
        isGameRunning = false;
        gameHandler.removeCallbacks(gameRunnable);
    }

    private void updateGame() {
        if (!Snake.alive) {
            endGame();
            return;
        }

        Snake.changeDirection(snakeDirection);
        Snake.move();

        if (!Snake.canMove() || Snake.hasCollisionWithSelf()) {
            Snake.alive = false;
            endGame();
            return;
        }

        // Check if snake eats food
        if (Snake.head.isAtSamePosition(Food.food)) {
            Snake.grow();

            if (!generateFood()) {
                Snake.alive = false;
                endGame();
                return;
            }

            score++;
            updateScore();

            // Update game speed
            if (gameSpeed > GameConfig.MAX_SPEED) {
                gameSpeed -= GameConfig.SPEED_STEP;
            }
        }

        // Redraw canvas
        canvasView.invalidate();
    }

    private boolean generateFood() {
        int fieldArea = (int) ((GameConfig.FIELD_WIDTH / GameConfig.STEP) * (GameConfig.FIELD_HEIGHT / GameConfig.STEP));
        int snakeSize = Snake.bodyParts.size();

        if (snakeSize >= fieldArea)
            return false;

        boolean onSnake;
        do {
            Food.generate();
            onSnake = Snake.isPartOfSnake(Food.food);
        } while (onSnake);

        return true;
    }

    private void updateScore() {
        scoreTextView.setText(String.format(Locale.ENGLISH, "Score: %d", score));
    }

    private void updateBestScore() {
        bestScoreTextView.setText(String.format(Locale.ENGLISH, "Best score: %d", bestScore));
    }

    private void saveBestScore() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(BEST_SCORE_KEY, bestScore);
        editor.apply();
    }

    private void loadBestScore() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bestScore = prefs.getInt(BEST_SCORE_KEY, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGameLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameRunning && Snake.alive && gameLayout.getVisibility() == View.VISIBLE) {
            startGameLoop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGameLoop();
    }
}