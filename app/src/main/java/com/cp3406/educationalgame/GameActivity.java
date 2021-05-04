package com.cp3406.educationalgame;
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
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity implements OnClickListener {
    private int timerSeconds;
    private final int timerLength = 60;
    private int level = 0;
    private int answer = 0;
    private int operator = 0;
    private final String[] operators = {"+", "-", "*", "/"};
    private Random random;
    private final int[][][] levelVals = {
            // Levels
            // 1        2       3        4
            {{1,10}, {0,20}, {0,50}, {0,100}}, // +
            {{0, 0}, {0,20}, {0,50}, {0,100}}, // -
            {{0, 0}, {0, 0}, {1, 5}, {1, 10}}, // *
            {{0, 0}, {0, 0}, {0, 0}, {1, 50}}  // /
    };

    private TextView question, answerTxt, scoreTxt;
    private TimerView timerView;

    private UpdateScoresTask updateScoresTask;

    private SensorManager mSensorManager;

    private ShakeEventListener mSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        timerView = findViewById(R.id.timer);
        question =  findViewById(R.id.question);
        answerTxt = findViewById(R.id.answer);
        scoreTxt =  findViewById(R.id.score);

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

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

        mSensorListener.setOnShakeListener(() -> answerTxt.setText("?"));

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            int passedLevel = extras.getInt("level", -1);
            if(passedLevel >= 0) level = passedLevel;
            random = new Random();
            answerTxt.setText("?");
            int[] questionVals = generateQuestion(level, random, levelVals);
            setQuestionText(questionVals);
        }

        new CountDownTimer(timerLength * 1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimer();
            }
            @Override
            public void onFinish() {
                updateTimer();
                finishGame();
            }
        }.start();
    }

    private void updateTimer() {
        float progress =  ((float) timerSeconds / (float) timerLength) * 100;
        timerView.setProgress(progress);
        timerView.setSecondsRemaining(timerLength - timerSeconds);
        timerSeconds++;
    }

    private void finishGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("GAME OVER");
        builder.setMessage(String.format(Locale.ENGLISH, "You scored %d", getScore()));
        builder.setPositiveButton(
                "Okay",
                (dialog, id) -> {
                    dialog.cancel();
                    finish();
                });
        AlertDialog ad = builder.create();
        ad.show();

        updateScoresTask = new UpdateScoresTask();
        updateScoresTask.execute();

    }

    public int[] generateQuestion(int level, Random rand, int[][][] levelVals) {
        operator = rand.nextInt(level + 1);
        int operand1 = getOperand(rand);
        int operand2 = getOperand(rand);
        Operator op = Operator.values()[operator];
        switch(op) {
            case ADD:
                while(operand1 + operand2 > levelVals[operator][level][1]) {
                    operand1 = getOperand(rand);
                    operand2 = getOperand(rand);
                }
                answer = operand1 + operand2;
                break;
            case SUBTRACT:
                // if second number is larger than first we will get a negative number, run until the answer is either 0 or larger
                while(operand2 > operand1) {
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
                while((((double) operand1 /(double) operand2) % 1 > 0) || (operand1 == operand2))
                {
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
        if(v.getId() == R.id.enter) {
            String answerContent = answerTxt.getText().toString();
            if(!answerContent.endsWith("?")) {
                //we have an answer
                int enteredAnswer = Integer.parseInt(answerContent);
                int exScore = getScore();
                if(enteredAnswer == answer){
                    String score = "Score: " + (exScore+1);
                    scoreTxt.setText(score);
                } else {
                    // TODO add a strike
                }
                answerTxt.setText("?");
                int[] questionVals = generateQuestion(level, random, levelVals);
                setQuestionText(questionVals);
            }
        } else if(v.getId() == R.id.clear) {
            answerTxt.setText("?");
        } else {
            int enteredNum = Integer.parseInt(v.getTag().toString());
            if(answerTxt.getText().toString().endsWith("?")) {
                String answerText = "" + enteredNum;
                answerTxt.setText(answerText);
            } else {
                answerTxt.append("" + enteredNum);
            }
        }
    }

    private int getScore(){
        String scoreStr = scoreTxt.getText().toString();
        return Integer.parseInt(scoreStr.substring(scoreStr.lastIndexOf(" ") + 1));
    }

    private void setQuestionText(int[] questionVals) {
        String questionTxt = questionVals[1] + " " + operators[questionVals[0]] + " " + questionVals[2] + " =";
        question.setText(questionTxt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(updateScoresTask != null) {
            updateScoresTask.cancel(true);
        }
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

    //Inner class to update the drink.
    private class UpdateScoresTask extends AsyncTask<Integer, Void, Boolean> {
        private ContentValues scoreValues;

        protected void onPreExecute() {
            DateFormat format = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
            String date = format.format(new Date());

            scoreValues = new ContentValues();
            scoreValues.put("SCORE", getScore());
            scoreValues.put("DATE", date);
            scoreValues.put("LEVEL", level + 1);
        }

        protected Boolean doInBackground(Integer... scores) {
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
}

