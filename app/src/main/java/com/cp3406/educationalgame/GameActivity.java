package com.cp3406.educationalgame;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Random;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity implements OnClickListener {
    private int timerSeconds;
    private int timerLength = 60;
    private int level = 0, answer = 0, operator = 0, operand1 = 0, operand2 = 0;
    private final int ADD_OPERATOR = 0, SUBTRACT_OPERATOR = 1, MULTIPLY_OPERATOR = 2, DIVIDE_OPERATOR = 3;
    private String[] operators = {"+", "-", "*", "/"};
    private Random random;
    private int[][] levelMin = {
            {1, 11, 21},
            {1, 5, 10},
            {2, 5, 10},
            {2, 3, 5}};
    private int[][] levelMax = {
            {10, 25, 50},
            {10, 20, 30},
            {5, 10, 15},
            {10, 50, 100}};

    private TextView question, answerTxt, scoreTxt;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, enterBtn, clearBtn;
    private TimerView timerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        timerView = findViewById(R.id.timer);
        question =  findViewById(R.id.question);
        answerTxt = findViewById(R.id.answer);
        scoreTxt =  findViewById(R.id.score);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btn0 = findViewById(R.id.btn0);
        enterBtn = findViewById(R.id.enter);
        clearBtn = findViewById(R.id.clear);

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

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            int passedLevel = extras.getInt("level", -1);
            if(passedLevel >= 0) level = passedLevel;
            random = new Random();
            generateQuestion();
        }

        new CountDownTimer(timerLength * 1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                float progress =  ((float) timerSeconds / (float) timerLength) * 100;
                timerView.setProgress(progress);
                timerView.setSecondsRemaining(timerLength - timerSeconds);
                // counttime.setText(String.valueOf(timer));
                timerSeconds++;
            }
            @Override
            public void onFinish() {
                // counttime.setText("Finished");
            }
        }.start();
    }

    private void generateQuestion() {
        //get a question
        answerTxt.setText("?");
        operator = random.nextInt(operators.length);
        operand1 = getOperand();
        operand2 = getOperand();

        switch(operator) {
            case ADD_OPERATOR:
                answer = operand1 + operand2;
                break;
            case MULTIPLY_OPERATOR:
                answer = operand1 * operand2;
                break;
            case SUBTRACT_OPERATOR:
                // if second number is larger than first we will get a negative number, run until the answer is either 0 or larger
                while(operand2 > operand1) {
                    operand1 = getOperand();
                    operand2 = getOperand();
                }
                answer = operand1 - operand2;
                break;
            case DIVIDE_OPERATOR:
                // generate question until we get an integer answer
                while((((double)operand1/(double)operand2)%1 > 0) || (operand1==operand2))
                {
                    operand1 = getOperand();
                    operand2 = getOperand();
                }
                answer = operand1 / operand2;
                break;

        }
        String questionTxt = operand1 + " " + operators[operator] + " " + operand2 + " =";
        question.setText(questionTxt);

    }

    private int getOperand() {
        //return operand number
        return random.nextInt(levelMax[operator][level] - levelMin[operator][level] + 1)
                + levelMin[operator][level];
    }

    @Override
    public void onClick(View v) {
        // button clicked
        switch(v.getId()) {
            case R.id.enter:
                String answerContent = answerTxt.getText().toString();
                if(!answerContent.endsWith("?")) {
                    //we have an answer
                    int enteredAnswer = Integer.parseInt(answerContent);
                    int exScore = getScore();
                    if(enteredAnswer == answer){
                        scoreTxt.setText("Score: " + (exScore+1));
                        // response.setImageResource(R.drawable.tick);
                    } else {
                        scoreTxt.setText("Score: 0");
                        // response.setImageResource(R.drawable.cross);
                    }
                    generateQuestion();
                }
                break;
            case R.id.clear:
                answerTxt.setText("?");
                break;
            default:
                int enteredNum = Integer.parseInt(v.getTag().toString());
                if(answerTxt.getText().toString().endsWith("?")) {
                    String answerText = "" + enteredNum;
                    answerTxt.setText(answerText);
                } else {
                    answerTxt.append("" + enteredNum);
                }
                break;
        }
    }

    private int getScore(){
        String scoreStr = scoreTxt.getText().toString();
        return Integer.parseInt(scoreStr.substring(scoreStr.lastIndexOf(" ") + 1));
    }
}
