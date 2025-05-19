package com.example.android_snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint({"ClickableViewAccessibility", "SourceLockedOrientationActivity"})
public class MainActivity extends AppCompatActivity {
    private CanvasView canvasView;
    private TextView scoreTextView;
    private TextView bestScoreTextView;
    private ScheduledExecutorService executor;
    public int gameSpeedMax = GameConfig.MAX_SPEED;
    private int gameSpeed = GameConfig.START_SPEED;
    private int score = 0;
    private int bestScore = 0;

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

        loadBestScore();
        updateBestScore();

        startButton.setOnClickListener(v -> startGame());
        backButton.setOnClickListener(v -> showMainMenu());

        // touch control
        canvasView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                Snake.nextDirection = Snake.Direction.LEFT;
            }

            @Override
            public void onSwipeRight() {
                Snake.nextDirection = Snake.Direction.RIGHT;
            }

            @Override
            public void onSwipeTop() {
                Snake.nextDirection = Snake.Direction.UP;
            }

            @Override
            public void onSwipeBottom() {
                Snake.nextDirection = Snake.Direction.DOWN;
            }
        });

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

        runOnUiThread(this::showGameOverScreen);
    }

    private void startGameLoop() {
        if (executor != null) {
            executor.shutdown();
        }

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::updateGame, 0, gameSpeed, TimeUnit.MILLISECONDS);
    }

    private void stopGameLoop() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    private void updateGame() {
        if (!Snake.alive) {
            endGame();
            return;
        }

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
            runOnUiThread(this::updateScore);

            if (gameSpeed > gameSpeedMax) {
                gameSpeed -= GameConfig.SPEED_STEP;

                // Update the scheduler with new speed
                executor.shutdown();
                executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleWithFixedDelay(this::updateGame, 0, gameSpeed, TimeUnit.MILLISECONDS);
            }
        }

        // Redraw canvas on UI thread
        runOnUiThread(() -> canvasView.invalidate());
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
        if (executor == null && Snake.alive && gameLayout.getVisibility() == View.VISIBLE) {
            startGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGameLoop();
    }
}