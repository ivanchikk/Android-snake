package com.example.android_snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
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
    private boolean isPaused = false;
    private int gameSpeed = GameConfig.START_SPEED;
    private int score = 0;
    private int bestScore = 0;
    private Snake.Direction snakeDirection = Snake.Direction.RIGHT;

    private ConstraintLayout mainMenuLayout;
    private ConstraintLayout gameLayout;
    private ConstraintLayout gameOverLayout;
    private FrameLayout pauseOverlay;
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
        pauseOverlay = findViewById(R.id.pause_overlay);

        canvasView = findViewById(R.id.canvas);
        scoreTextView = findViewById(R.id.score);
        bestScoreTextView = findViewById(R.id.best_score);
        finalScoreTextView = findViewById(R.id.final_score);

        Button startButton = findViewById(R.id.start_button);
        Button backButton = findViewById(R.id.back_button);
        Button pauseButton = findViewById(R.id.pause_button);
        Button resumeButton = findViewById(R.id.resume_button);

        gameHandler = new Handler(Looper.getMainLooper());
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning && !isPaused) {
                    updateGame();
                    gameHandler.postDelayed(this, gameSpeed);
                }
            }
        };

        startButton.setOnClickListener(v -> startGame());
        backButton.setOnClickListener(v -> showMainMenu());
        pauseButton.setOnClickListener(v -> pauseGame());
        resumeButton.setOnClickListener(v -> resumeGame());

        // touch control
        canvasView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                changeSnakeDirection(Snake.Direction.LEFT);
            }

            @Override
            public void onSwipeRight() {
                changeSnakeDirection(Snake.Direction.RIGHT);
            }

            @Override
            public void onSwipeTop() {
                changeSnakeDirection(Snake.Direction.UP);
            }

            @Override
            public void onSwipeBottom() {
                changeSnakeDirection(Snake.Direction.DOWN);
            }
        });

        setupGameFieldSize();
        loadBestScore();
        updateBestScore();
        showMainMenu();
    }

    private void setupGameFieldSize() {
        WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
        Rect bounds = windowMetrics.getBounds();

        int screenWidth = bounds.width();
        int screenHeight = bounds.height();

        // Calculate max square size that fits the screen
        // Leaving some space for UI elements
        int topUiHeight = 100;
        int availableHeight = screenHeight - topUiHeight;

        // Use the smaller of the screen dimensions to ensure it fits
        int gameFieldSize = Math.min(screenWidth, availableHeight);

        // Update GameConfig
        GameConfig.FIELD_WIDTH = gameFieldSize;
        GameConfig.FIELD_HEIGHT = gameFieldSize;

        // Adjust step size for a nice grid
        GameConfig.STEP = (float) gameFieldSize / GameConfig.FIELD_CELLS_COUNT;
        GameConfig.SEGMENT_SIZE = GameConfig.STEP - GameConfig.STEP / 10;
        GameConfig.DRAW_OFFSET = (GameConfig.STEP % GameConfig.SEGMENT_SIZE) / 2;
    }

    private void showMainMenu() {
        mainMenuLayout.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);
        pauseOverlay.setVisibility(View.GONE);

        stopGameLoop();
    }

    private void showGameScreen() {
        mainMenuLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);
        pauseOverlay.setVisibility(View.GONE);
    }

    private void showGameOverScreen() {
        mainMenuLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.VISIBLE);
        pauseOverlay.setVisibility(View.GONE);

        finalScoreTextView.setText(String.format(Locale.ENGLISH, "Your score: %d", score));
    }

    private void pauseGame() {
        if (!isPaused && isGameRunning) {
            isPaused = true;
            pauseOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void resumeGame() {
        if (isPaused) {
            isPaused = false;
            pauseOverlay.setVisibility(View.GONE);

            if (isGameRunning) {
                gameHandler.postDelayed(gameRunnable, gameSpeed);
            }
        }
    }

    private void startGame() {
        Snake.reset();
        generateFood();
        snakeDirection = Snake.Direction.RIGHT;
        score = 0;
        gameSpeed = GameConfig.START_SPEED;
        isPaused = false;

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

    private void changeSnakeDirection(Snake.Direction direction) {
        if (isGameRunning && !isPaused) {
            snakeDirection = direction;
        }
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && isGameRunning && !isPaused) {
            pauseGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isGameRunning && !isPaused) {
            pauseGame();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isGameRunning && !isPaused) {
            pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGameLoop();
    }
}