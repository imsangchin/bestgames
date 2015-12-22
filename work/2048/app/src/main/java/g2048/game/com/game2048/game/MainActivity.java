package g2048.game.com.game2048.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.WindowManager;

import g2048.game.com.game2048.R;
import g2048.game.com.game2048.game.settings.SettingsActivity;
import g2048.game.com.game2048.game.settings.SettingsProvider;


public class MainActivity extends Activity {

    MainView view;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsProvider.initPreferences(this);
        InputListener.loadSensitivity();
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        
        view = new MainView(getBaseContext());
        if (savedInstanceState != null) {
            Tile[][] field = view.game.grid.field;
            int[][] saveState = new int[field.length][field[0].length];
            for (int xx = 0; xx < saveState.length; xx++) {
                saveState[xx] = savedInstanceState.getIntArray("" + xx);
            }
            for (int xx = 0; xx < saveState.length; xx++) {
                for (int yy = 0; yy < saveState[0].length; yy++) {
                    if (saveState[xx][yy] != 0) {
                        view.game.grid.field[xx][yy] = new Tile(xx, yy, saveState[xx][yy]);
                    } else {
                        view.game.grid.field[xx][yy] = null;
                    }
                }
            }
            view.game.score = savedInstanceState.getLong("score");
            view.game.highScore = savedInstanceState.getLong("high score");
            view.game.won = savedInstanceState.getBoolean("won");
            view.game.lose = savedInstanceState.getBoolean("lose");
        }
        setContentView(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_undo).setEnabled(view.game.grid.canRevert);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_undo:
                view.game.revertState();
                return true;
            case R.id.menu_settings:
                Intent i = new Intent();
                i.setAction(Intent.ACTION_MAIN);
                i.setClass(this, SettingsActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Tile[][] field = view.game.grid.field;
        int[][] saveState = new int[field.length][field[0].length];
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    saveState[xx][yy] = field[xx][yy].getValue();
                } else {
                    saveState[xx][yy] = 0;
                }
            }
        }
        for (int xx = 0; xx < saveState.length; xx++) {
            savedInstanceState.putIntArray("" + xx, saveState[xx]);
        }
        savedInstanceState.putLong("score", view.game.score);
        savedInstanceState.putLong("high score", view.game.highScore);
        savedInstanceState.putBoolean("won", view.game.won);
        savedInstanceState.putBoolean("lose", view.game.lose);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	switch (event.getKeyCode()){
    	case KeyEvent.KEYCODE_DPAD_UP:
    		view.game.move(0);
    		break;
    	case KeyEvent.KEYCODE_DPAD_DOWN:
    		view.game.move(2);
    		break;
    	case KeyEvent.KEYCODE_DPAD_LEFT:
    		view.game.move(3);
    		break;
    	case KeyEvent.KEYCODE_DPAD_RIGHT:
    		view.game.move(1);
    		break;
    	}
    	return super.onKeyDown(keyCode, event);
    }
}
