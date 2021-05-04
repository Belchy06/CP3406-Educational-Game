package com.cp3406.educationalgame;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ScoresActivity extends AppCompatActivity {
    private Cursor cursor;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
        ListView scoresList = findViewById(R.id.high_scores_list);
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("SCORES",
                    new String[]{"_id", "('LEVEL ' || LEVEL || ':  ' || DATE || ' - ' || SCORE) AS RESULT"},
                    null, null, null, null, "SCORE DESC", "10");
            SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[] {"RESULT"},
                    new int[] {android.R.id.text1},
                    0);
            // listAdapter.setViewBinder();
            scoresList.setAdapter(listAdapter);
        } catch(SQLException e) {
            Toast toast = Toast.makeText(this, "Scores Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }
}
