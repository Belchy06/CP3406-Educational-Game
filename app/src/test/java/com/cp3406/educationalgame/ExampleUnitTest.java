package com.cp3406.educationalgame;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void question_isCorrect() {
        int[][][] levelVals = {
                // Levels
                // 1        2       3        4
                {{1,10}, {0,20}, {0,50}, {0,100}}, // +
                {{0, 0}, {0,20}, {0,50}, {0,100}}, // -
                {{0, 0}, {0, 0}, {1, 5}, {1, 10}}, // *
                {{0, 0}, {0, 0}, {0, 0}, {1, 50}}  // /
        };
        Random rand = new Random();
        GameActivity gameActivity = new GameActivity();
        int[] question_vals = gameActivity.generateQuestion(1, rand, levelVals);
        switch(question_vals[0]) {
            case 0:
                assertEquals(question_vals[3], question_vals[1] + question_vals[2]);
                break;
            case 1:
                assertEquals(question_vals[3], question_vals[1] - question_vals[2]);
                break;
            case 2:
                assertEquals(question_vals[3], question_vals[1] * question_vals[2]);
                break;
            case 3:
                assertEquals(question_vals[3], question_vals[1] / question_vals[2]);
                break;
        }
    }
}