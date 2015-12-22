package com.game.bee;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Bee extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bee);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bee, menu);
        return true;
    }
    
}
