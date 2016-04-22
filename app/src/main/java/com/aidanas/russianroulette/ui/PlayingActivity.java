package com.aidanas.russianroulette.ui;

import android.app.Activity;
import android.os.Bundle;

import com.aidanas.russianroulette.R;

/**
 * Class to contain the "Playing" screen of the game.
 */
public class PlayingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
    }
}
