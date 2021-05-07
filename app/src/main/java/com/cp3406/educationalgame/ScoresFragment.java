package com.cp3406.educationalgame;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import java.util.Objects;

public class ScoresFragment extends Fragment {
    private Cursor cursor;
    private SQLiteDatabase db;
    private int level;
    private Context context;

    public ScoresFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.scores_fragment, container, false);
        SQLiteOpenHelper databaseHelper = new DatabaseHelper(context);
        ListView scoresList = v.findViewById(R.id.high_scores_list);
        assert getArguments() != null;
        level = getArguments().getInt("level");
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("SCORES",
                    new String[]{"_id", "('LEVEL ' || LEVEL || ':  ' || DATE || ' - ' || SCORE) AS RESULT"},
                    "LEVEL = " + level, null, null, null, "SCORE DESC", "10");
            SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(context,
                    R.layout.list_textview,
                    cursor,
                    new String[] {"RESULT"},
                    new int[] {R.id.list_content},
                    0);
            scoresList.setAdapter(listAdapter);
        } catch(SQLException e) {
            Toast toast = Toast.makeText(context, "Scores Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }

    public static ScoresFragment newInstance(int level) {
        Bundle args = new Bundle();
        args.putInt("level", level);
        ScoresFragment f = new ScoresFragment();
        f.setArguments(args);
        return f;
    }
}
