package com.game.bee;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class BeeActivity extends Activity {

	private PlaneView mPlaneView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bee);

		mPlaneView = (PlaneView) findViewById(R.id.plane_view);

		mPlaneView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mPlaneView.getGameState() == PlaneView.RUNNING) {
					
					// Normalize x,y between 0 and 1
					float x = event.getX() / v.getWidth();
					float y = event.getY() / v.getHeight();

					// Direction will be [0,1,2,3] depending on quadrant
					int direction = 0;
					direction = (x > y) ? 1 : 0;
					Log.d("bee", direction+"");
					direction |= (x > 1 - y) ? 2 : 0;
					Log.d("bee", direction+"");
					// Direction is same as the quadrant which was clicked
					mPlaneView.movePlane(direction);

				} else {
					// If the game is not running then on touching any part of
					// the screen
					// we start the game by sending MOVE_UP signal to SnakeView
					mPlaneView.movePlane(Direction.UP);
				}
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bee, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			mPlaneView.movePlane(Direction.UP);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			mPlaneView.movePlane(Direction.RIGHT);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			mPlaneView.movePlane(Direction.DOWN);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			mPlaneView.movePlane(Direction.LEFT);
			break;
		}

		return super.onKeyDown(keyCode, msg);
	}

}
