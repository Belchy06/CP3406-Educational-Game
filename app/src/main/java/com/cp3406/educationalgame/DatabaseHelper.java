package com.cp3406.educationalgame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 0;

    DatabaseHelper(Context context) {
        super(context, context.getResources().getString(R.string.app_name), null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE SCORES (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "SCORE INTEGER);");
        }
        if (oldVersion < 2) {
            // db.execSQL("ALTER TABLE SCORES ADD COLUMN FAVORITE NUMERIC;");
        }
    }
}
