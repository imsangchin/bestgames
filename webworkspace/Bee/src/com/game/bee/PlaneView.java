package com.game.bee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

public class PlaneView extends View {

	private int mDirection = Direction.UP;
	private int mNextDirection = Direction.UP;

	private int mMode = READY;
	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;

	private Plane mPlane;
	private Bitmap mBitmapPlane;

	public PlaneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPlane = new Plane(50, 50);
		mBitmapPlane = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.jacket);
	}

	public int getGameState() {
		return RUNNING;
	}

	public void movePlane(int direction) {
		if (direction == Direction.UP) {
			if (mDirection != Direction.DOWN) {
				mNextDirection = Direction.UP;
				update();
			}
			return;
		}

		if (direction == Direction.DOWN) {
			if (mDirection != Direction.UP) {
				mNextDirection = Direction.DOWN;
				update();
			}
			return;
		}

		if (direction == Direction.LEFT) {
			if (mDirection != Direction.RIGHT) {
				mNextDirection = Direction.LEFT;
				update();
			}
			return;
		}

		if (direction == Direction.RIGHT) {
			if (mDirection != Direction.LEFT) {
				mNextDirection = Direction.RIGHT;
				update();
			}
			return;
		}
		
	}

	private void update() {
		mDirection = mNextDirection;
		switch (mDirection) {
		case Direction.UP:
			mPlane.setY(mPlane.getY() - 1);
			break;
		case Direction.LEFT:
			mPlane.setX(mPlane.getX() + 1);
			break;
		case Direction.DOWN:
			mPlane.setY(mPlane.getY() + 1);
			break;
		case Direction.RIGHT:
			mPlane.setX(mPlane.getX() - 1);
			break;
		default:
			break;
		}
		
		mRedrawHandler.sleep(100);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mBitmapPlane, mPlane.getX(), mPlane.getY(), null);
	}
	
	private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            PlaneView.this.update();
            PlaneView.this.invalidate();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };


	public void setMode(int newMode) {
		int oldMode = mMode;
		mMode = newMode;

		// if (newMode == RUNNING && oldMode != RUNNING) {
		// // hide the game instructions
		// mStatusText.setVisibility(View.INVISIBLE);
		// update();
		// // make the background and arrows visible as soon the snake starts
		// moving
		// mArrowsView.setVisibility(View.VISIBLE);
		// mBackgroundView.setVisibility(View.VISIBLE);
		// return;
		// }
		//
		// Resources res = getContext().getResources();
		// CharSequence str = "";
		// if (newMode == PAUSE) {
		// mArrowsView.setVisibility(View.GONE);
		// mBackgroundView.setVisibility(View.GONE);
		// str = res.getText(R.string.mode_pause);
		// }
		// if (newMode == READY) {
		// mArrowsView.setVisibility(View.GONE);
		// mBackgroundView.setVisibility(View.GONE);
		//
		// str = res.getText(R.string.mode_ready);
		// }
		// if (newMode == LOSE) {
		// mArrowsView.setVisibility(View.GONE);
		// mBackgroundView.setVisibility(View.GONE);
		// str = res.getString(R.string.mode_lose, mScore);
		// }
		//
		// mStatusText.setText(str);
		// mStatusText.setVisibility(View.VISIBLE);
	}
}
