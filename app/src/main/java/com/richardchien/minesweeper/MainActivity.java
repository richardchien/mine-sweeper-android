package com.richardchien.minesweeper;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private EditText numOfBombsText;
    private Button startBtn;
    private ToggleButton flagModeToggleBtn;
    private TableLayout tableLayout;

    private final int kRowN = 10;
    private final int kColN = 9;
    private MineSweeperGame game;
    private boolean isFlagMode = false;
    private long beginTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numOfBombsText = (EditText) findViewById(R.id.editText);
        startBtn = (Button) findViewById(R.id.button);
        flagModeToggleBtn = (ToggleButton) findViewById(R.id.toggleButton);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);

        /*
         * Initialize map on UI
         */
        for (int i = 0; i < kRowN; i++) {
            TableRow row = new TableRow(this);
            row.setId(i);
            row.setGravity(Gravity.CENTER);
            for (int j = 0; j < kColN; j++) {
                TextView tv = new TextView(this);
                tv.setId(j);
                tv.setText("");
                tv.setTextColor(Color.BLACK);
                final int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.textview_border));
                } else {
                    tv.setBackgroundResource(R.drawable.textview_border);
                }
                tv.setGravity(Gravity.CENTER);
                tv.setWidth(DisplayHelper.dpToPixel(36, this));
                tv.setHeight(DisplayHelper.dpToPixel(36, this));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleClickAt(((View) v.getParent()).getId(), v.getId());
                    }
                });
                TableRow.LayoutParams lp = new TableRow.LayoutParams();
                lp.rightMargin = DisplayHelper.dpToPixel(-1, this);
                tv.setLayoutParams(lp);
                row.addView(tv);
            }
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams();
            lp.bottomMargin = DisplayHelper.dpToPixel(-1, this);
            row.setLayoutParams(lp);
            tableLayout.addView(row);
        }

        /*
         * Set button listener
         */
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int bombN = Integer.parseInt(numOfBombsText.getText().toString());
                int maxBombN = kRowN * kColN - 1;
                int minBombN = 1;
                if (bombN > maxBombN || bombN < minBombN) {
                    Toast t = Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_set_bombN) + " " + minBombN + "~" + maxBombN, Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                game = new MineSweeperGame(kRowN, kColN, bombN);
                game.prepareGame();
                refreshDisplay();
                startBtn.setText(getResources().getString(R.string.restart));
            }
        });

        flagModeToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFlagMode = isChecked;
            }
        });
    }

    private void handleClickAt(int row, int col) {
        if (game == null ||
                (game.getState() != MineSweeperGame.GameState.Waiting &&
                        game.getState() != MineSweeperGame.GameState.Playing)) {
            Toast t = Toast.makeText(this, getResources().getString(R.string.please_start_first), Toast.LENGTH_SHORT);
            t.show();
            return;
        }

        if (game.getState() == MineSweeperGame.GameState.Waiting) {
            /*
             * First click,
             */
            Date now = new Date();
            beginTime = now.getTime();
        }

        if (isFlagMode) {
            if (game.isFlagged(row, col)) {
                game.unflagAt(row, col);
            } else {
                game.flagAt(row, col);
            }
        } else {
            game.digAt(row, col);
        }
        refreshDisplay();

        if (game.getState() == MineSweeperGame.GameState.Win ||
                game.getState() == MineSweeperGame.GameState.Lose) {
            Date now = new Date();
            long finishTime = now.getTime();
            long span = finishTime - beginTime;
            long secondUsed = (long)Math.ceil((double)span / 1000.0);

            Toast t = Toast.makeText(this, game.getState() == MineSweeperGame.GameState.Win ?
                    getResources().getString(R.string.you_win) + " Time used: " + secondUsed + "s" : getResources().getString(R.string.you_lose), Toast.LENGTH_SHORT);
            t.show();
            startBtn.setText(getResources().getString(R.string.start));
        }
    }

    private void refreshDisplay() {
        if (game == null) {
            return;
        }

        int map[][] = game.getMapToDisplay();
        for (int i = 0; i < kRowN; i++) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            for (int j = 0; j < kColN; j++) {
                TextView tv = (TextView) tableRow.getChildAt(j);
                if (map[i][j] == MineSweeperGame.kMapUnshown) {
                    tv.setText("");
                } else if (map[i][j] == MineSweeperGame.kMapFlaged) {
                    tv.setText("\uD83D\uDEA9️");
                } else if (map[i][j] == MineSweeperGame.kMapBomb) {
                    tv.setText("\uD83D\uDCA3");
                } else {
                    if (map[i][j] == 0) {
                        tv.setText("0");
                    } else {
                        tv.setText(String.valueOf(map[i][j]));
                    }
                }
            }
        }
    }
}
