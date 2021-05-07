package com.cp3406.educationalgame;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Toast;

import java.util.Locale;

import twitter4j.TwitterException;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private final String[] levels = {"1", "2", "3", "4"};
    private boolean twitterAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button playBtn = findViewById(R.id.play_btn);
        Button helpBtn = findViewById(R.id.help_btn);
        Button scoresBtn = findViewById(R.id.score_btn);
        Button settingsBtn = findViewById(R.id.settings_btn);

        playBtn.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        scoresBtn.setOnClickListener(this);
        settingsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_btn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose a level")
                    .setSingleChoiceItems(levels, 0, (dialog, level) -> {
                        dialog.dismiss();
                        //start gameplay
                        Intent playIntent = new Intent(this, GameActivity.class);
                        playIntent.putExtra("level", level);
                        playIntent.putExtra("twitterAuth", twitterAuth);
                        this.startActivityForResult(playIntent, GameActivity.GAME_REQUEST);
                    });
            AlertDialog ad = builder.create();
            ad.show();
        } else if (v.getId() == R.id.help_btn) {
            Intent helpIntent = new Intent(this, InstructionsActivity.class);
            this.startActivity(helpIntent);
        } else if (v.getId() == R.id.score_btn) {
            Intent scoresIntent = new Intent(this, ScoresActivity.class);
            this.startActivity(scoresIntent);
        } else if (v.getId() == R.id.settings_btn) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(settingsIntent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GameActivity.GAME_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    twitterAuth = data.getBooleanExtra("AUTH", false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}