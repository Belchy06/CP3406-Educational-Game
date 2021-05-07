package com.cp3406.educationalgame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class GameActivity extends AppCompatActivity implements OnClickListener {
    public static int GAME_REQUEST = 2;
    private int timerSeconds;
    private final int timerLength = 60;
    private int level = 0;
    private int answer = 0;
    private int operator = 0;
    private int operand1, operand2;
    private final String[] operators = {"+", "-", "*", "/"};
    private Random random;
    private final int[][][] levelVals = {
            // Levels
            // 1        2       3        4
            {{1, 10}, {0, 20}, {0, 50}, {0, 100}}, // +
            {{0, 0}, {0, 20}, {0, 50}, {0, 100}}, // -
            {{0, 0}, {0, 0}, {1, 5}, {1, 10}}, // *
            {{0, 0}, {0, 0}, {0, 0}, {1, 50}}  // /
    };

    private CountDownTimer cdt;
    private TextView question, answerTxt, scoreTxt;
    private TimerView timerView;
    private UpdateScoresTask updateScoresTask;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;

    private Twitter twitter = TwitterFactory.getSingleton();
    private boolean twitterAuth;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        timerView = findViewById(R.id.timer);
        question = findViewById(R.id.question);
        answerTxt = findViewById(R.id.answer);
        scoreTxt = findViewById(R.id.score);

        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        Button btn4 = findViewById(R.id.btn4);
        Button btn5 = findViewById(R.id.btn5);
        Button btn6 = findViewById(R.id.btn6);
        Button btn7 = findViewById(R.id.btn7);
        Button btn8 = findViewById(R.id.btn8);
        Button btn9 = findViewById(R.id.btn9);
        Button btn0 = findViewById(R.id.btn0);
        Button enterBtn = findViewById(R.id.enter);
        Button clearBtn = findViewById(R.id.clear);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btn0.setOnClickListener(this);
        enterBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

        mSensorListener.setOnShakeListener(() -> answerTxt.setText("?"));
        random = new Random();

        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                twitterAuth = extras.getBoolean("twitterAuth", false);
                int passedLevel = extras.getInt("level", -1);
                if (passedLevel >= 0) level = passedLevel;
                answerTxt.setText("?");
                int[] questionVals = generateQuestion(level, random, levelVals);
                setQuestionText(questionVals);
            }
            initTimer(timerLength);
        } else {
            timerSeconds = savedInstanceState.getInt("secsLeft");
            operand1 = savedInstanceState.getInt("op1");
            operand2 = savedInstanceState.getInt("op2");
            operator = savedInstanceState.getInt("operator");
            answer = savedInstanceState.getInt("answer");
            int score = savedInstanceState.getInt("score");
            String scoreText = "Score: \n" + score;
            scoreTxt.setText(scoreText);
            initTimer(timerLength - timerSeconds);
            setQuestionText(new int[] {operator, operand1, operand2});
        }
    }

    private void initTimer(int length) {
        cdt = new CountDownTimer(length * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimer();
            }

            @Override
            public void onFinish() {
                updateTimer();
                finishGame();
            }
        };
        cdt.start();
    }

    private void updateTimer() {
        float progress = ((float) timerSeconds / (float) timerLength) * 100;
        timerView.setProgress(progress);
        timerView.setSecondsRemaining(timerLength - timerSeconds);
        timerSeconds++;
    }

    private void finishGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("GAME OVER");
        builder.setMessage(String.format(Locale.ENGLISH, "You scored %d", getScore()));
        builder.setNeutralButton("Share to Twitter", ((dialog, id) -> {
            if(!twitterAuth) {
                authorise();
            } else {
                tweet();
                exitGame();
            }

        }));
        builder.setPositiveButton(
                "Okay",
                (dialog, id) -> {
                    dialog.cancel();
                    exitGame();
                });
        AlertDialog ad = builder.create();
        ad.show();

        updateScoresTask = new UpdateScoresTask();
        updateScoresTask.execute();
    }

    public int[] generateQuestion(int level, Random rand, int[][][] levelVals) {
        operator = rand.nextInt(level + 1);
        operand1 = getOperand(rand);
        operand2 = getOperand(rand);
        Operator op = Operator.values()[operator];
        switch (op) {
            case ADD:
                while (operand1 + operand2 > levelVals[operator][level][1]) {
                    operand1 = getOperand(rand);
                    operand2 = getOperand(rand);
                }
                answer = operand1 + operand2;
                break;
            case SUBTRACT:
                // if second number is larger than first we will get a negative number, run until the answer is either 0 or larger
                while (operand2 > operand1) {
                    operand1 = getOperand(rand);
                    operand2 = getOperand(rand);
                }
                answer = operand1 - operand2;
                break;
            case MULTIPLY:
                answer = operand1 * operand2;
                break;
            case DIVIDE:
                // generate question until we get an integer answer
                while ((((double) operand1 / (double) operand2) % 1 > 0) || (operand1 == operand2)) {
                    operand1 = getOperand(rand);
                    operand2 = getOperand(rand);
                }
                answer = operand1 / operand2;
                break;

        }
        return new int[]{operator, operand1, operand2, answer};
    }

    private int getOperand(Random rand) {
        //return operand number
        int max = levelVals[operator][level][1];
        int min = levelVals[operator][level][0];
        return rand.nextInt(max - min + 1) + min;
    }

    @Override
    public void onClick(View v) {
        // button clicked
        if (v.getId() == R.id.enter) {
            String answerContent = answerTxt.getText().toString();
            if (!answerContent.endsWith("?")) {
                //we have an answer
                int enteredAnswer = Integer.parseInt(answerContent);
                int exScore = getScore();
                if (enteredAnswer == answer) {
                    String score = "Score: \n" + (exScore + 1);
                    scoreTxt.setText(score);
                }
                answerTxt.setText("?");
                int[] questionVals = generateQuestion(level, random, levelVals);
                setQuestionText(questionVals);
            }
        } else if (v.getId() == R.id.clear) {
            answerTxt.setText("?");
        } else {
            int enteredNum = Integer.parseInt(v.getTag().toString());
            if (answerTxt.getText().toString().endsWith("?")) {
                String answerText = "" + enteredNum;
                answerTxt.setText(answerText);
            } else {
                answerTxt.append("" + enteredNum);
            }
        }
    }

    private int getScore() {
        String scoreStr = scoreTxt.getText().toString();
        return Integer.parseInt(scoreStr.substring(scoreStr.lastIndexOf(" ") + 2));
    }

    private void setQuestionText(int[] questionVals) {
        String questionTxt = questionVals[1] + " " + operators[questionVals[0]] + " " + questionVals[2] + " =";
        question.setText(questionTxt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateScoresTask != null) {
            updateScoresTask.cancel(true);
        }
    }

    private String getDate() {
        DateFormat format = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        return format.format(new Date());
    }

    private String getTime() {
        String timeFormat = "hh:mm a";
        return new SimpleDateFormat(timeFormat, Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    //Inner class to update the score
    private class UpdateScoresTask extends AsyncTask<Integer, Void, Boolean> {
        private ContentValues scoreValues;

        protected void onPreExecute() {


            scoreValues = new ContentValues();
            scoreValues.put("SCORE", getScore());
            scoreValues.put("DATE", getDate());
            scoreValues.put("LEVEL", level + 1);
        }

        protected Boolean doInBackground(Integer... scores) {
            if (scoreValues.getAsInteger("SCORE") != 0) {
                SQLiteOpenHelper databaseHelper =
                        new DatabaseHelper(GameActivity.this);
                try {
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();

                    db.insert("SCORES", null, scoreValues);
                    db.close();
                    return true;
                } catch (SQLiteException e) {
                    return false;
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast toast = Toast.makeText(GameActivity.this,
                        "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    enum Operator {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }


    public void authorise() {
        Intent intent = new Intent(this, com.cp3406.educationalgame.Twitter.class);
        startActivityForResult(intent, com.cp3406.educationalgame.Twitter.TWITTER_REQUEST);
    }

    private void exitGame() {
        Intent intent = new Intent();
        intent.putExtra("AUTH", twitterAuth);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void tweet() {
        if (twitterAuth) {
            com.cp3406.educationalgame.Twitter.run(() -> {
                try {
                    twitter.updateStatus(String.format(Locale.ENGLISH, "I just scored %d on %s Level %d @ %s - %s", getScore(), getString(R.string.app_name), level + 1, getDate(), getTime()));
                } catch (TwitterException e) {
                    Toast toast = Toast.makeText(GameActivity.this,
                            String.format(Locale.ENGLISH, "Error: %s", e), Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == com.cp3406.educationalgame.Twitter.TWITTER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    twitterAuth = data.getBooleanExtra("AUTH", false);
                    tweet();
                    exitGame();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            exitGame();
        }
        return super.onOptionsItemSelected(item);
    }

    //================================================================================
    // State Handlers
    //================================================================================
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        cdt.cancel();
        outState.putInt("secsLeft", timerSeconds);
        outState.putInt("score", getScore());
        outState.putInt("op1", operand1);
        outState.putInt("op2", operand2);
        outState.putInt("operator", operator);
        outState.putInt("answer", answer);
    }
}

