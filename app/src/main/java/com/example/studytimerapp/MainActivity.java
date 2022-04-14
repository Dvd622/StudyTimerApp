package com.example.studytimerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String LAST_TASK = "task";
    private static final String LAST_TIME = "last_time";
    SharedPreferences sharedPreferences;
    long startTime = 0;
    long pauseTime = 0;
    Boolean timeRunning = false;
    Boolean timeStarted = false;
    String lastTask;
    String lastTime;

    TextView lastStudyHoursTextView;
    TextView timerTextView;
    ImageButton startImageButton;
    ImageButton pauseImageButton;
    ImageButton stopImageButton;
    EditText enterTaskEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* test
        TextView lastStudyHoursTextView = findViewById(R.id.lastStudyHoursTextView);
        TextView timerTextView = findViewById(R.id.timerTextView);
        ImageButton startImageButton = findViewById(R.id.startImageButton);
        ImageButton pauseImageButton = findViewById(R.id.pauseImageButton);
        ImageButton stopImageButton = findViewById(R.id.stopImageButton);
        EditText enterTaskEditText = findViewById(R.id.enterTaskEditText);
         */

        lastStudyHoursTextView = findViewById(R.id.lastStudyHoursTextView);
        timerTextView = findViewById(R.id.timerTextView);
        startImageButton = findViewById(R.id.startImageButton);
        pauseImageButton = findViewById(R.id.pauseImageButton);
        stopImageButton = findViewById(R.id.stopImageButton);
        enterTaskEditText = findViewById(R.id.enterTaskEditText);

        sharedPreferences = getSharedPreferences("com.example.task", MODE_PRIVATE);
        lastTask = sharedPreferences.getString(LAST_TASK, "...");
        lastTime = sharedPreferences.getString(LAST_TIME, "00:00");

        lastStudyHoursTextView.setText(String.format(Locale.ENGLISH, "You spent %s on %s last time", lastTime, lastTask));

        Handler timerHandler = new Handler();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerTextView.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        };

        // screen orientation is changed
        if (savedInstanceState!=null) {
            lastTask = savedInstanceState.getString(LAST_TASK);
            lastTime = savedInstanceState.getString(LAST_TIME);
            startTime = savedInstanceState.getLong("startTime");
            pauseTime = savedInstanceState.getLong("pauseTime");
            timeRunning = savedInstanceState.getBoolean("timeRunning");
            timeStarted = savedInstanceState.getBoolean("timeStarted");
            timerTextView.setText(savedInstanceState.getString("time"));
            if (timeRunning.equals(true)) {
                timerHandler.postDelayed(timerRunnable, 0);
            }
        }

        startImageButton.setOnClickListener(view -> {
            if (!timeRunning) { // timer is not running
                if (!timeStarted) { // time has not started (is stopped, make new start time)
                    startTime = System.currentTimeMillis();
                    timeStarted = true;
                } else { // timer was paused, calculate new startTime to continue from
                    startTime = System.currentTimeMillis() + startTime - pauseTime;
                }
                timerHandler.postDelayed(timerRunnable, 0);
                timeRunning = true;
            }
        });

        pauseImageButton.setOnClickListener(view -> {
            if (timeRunning) {
                pauseTime = System.currentTimeMillis();
                timerHandler.removeCallbacks(timerRunnable);
                timeRunning = false;
            }
        });

        stopImageButton.setOnClickListener(view -> {
            timerHandler.removeCallbacks(timerRunnable);
            timeRunning = false;
            timeStarted = false;
            startTime = System.currentTimeMillis();
            pauseTime = 0;
            lastTask = enterTaskEditText.getText().toString();
            lastTime = timerTextView.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(LAST_TASK, lastTask);
            editor.putString(LAST_TIME, lastTime);
            editor.apply();
            Toast.makeText(this, "Study time saved", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle OutState) {
        OutState.putString(LAST_TASK, lastTask);
        OutState.putString(LAST_TIME, lastTime);
        OutState.putLong("startTime", startTime);
        OutState.putLong("pauseTime", pauseTime);
        OutState.putBoolean("timeRunning", timeRunning);
        OutState.putBoolean("timeStarted", timeStarted);
        OutState.putString("time", timerTextView.getText().toString());
        super.onSaveInstanceState(OutState);
    }
}