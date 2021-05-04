package com.cp3406.educationalgame;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;

public class MainActivity extends AppCompatActivity implements OnClickListener{
    private Button playBtn;
    private Button helpBtn;
    private Button scoresBtn;
    private final String[] levels = {"1", "2", "3", "4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playBtn = findViewById(R.id.play_btn);
        helpBtn = findViewById(R.id.help_btn);
        scoresBtn = findViewById(R.id.score_btn);

        playBtn.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        scoresBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_btn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose a level")
                        .setSingleChoiceItems(levels, 0, (dialog, level) -> {
                            dialog.dismiss();
                            //start gameplay
                            Intent playIntent = new Intent(this, GameActivity.class);
                            playIntent.putExtra("level", level);
                            this.startActivity(playIntent);
                        });
                AlertDialog ad = builder.create();
                ad.show();
                break;
            case R.id.help_btn:
                Intent helpIntent = new Intent(this, InstructionsActivity.class);
                this.startActivity(helpIntent);
                break;
            case R.id.score_btn:
                Intent scoresIntent = new Intent(this, ScoresActivity.class);
                this.startActivity(scoresIntent);
                break;
            default:
                break;
        }
    }
}