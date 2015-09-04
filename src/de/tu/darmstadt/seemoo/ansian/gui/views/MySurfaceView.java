package de.tu.darmstadt.seemoo.ansian.gui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import de.greenrobot.event.EventBus;
import de.tu.darmstadt.seemoo.ansian.control.threads.SurfaceUpdateThread;

public abstract class MySurfaceView extends SurfaceView
		implements ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

	// Gesture detectors to detect scaling, scrolling ...
	protected ScaleGestureDetector scaleGestureDetector = null;
	protected GestureDetector gestureDetector = null;

	public MySurfaceView(Context context) {
		super(context);
	}

	public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		SurfaceUpdateThread.registerView(this);
		EventBus.getDefault().register(this);
		scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
		gestureDetector = new GestureDetector(getContext(), this);
	}

	@Override
	protected void onDetachedFromWindow() {
		SurfaceUpdateThread.unregisterView(this);
		EventBus.getDefault().unregister(this);
		super.onDetachedFromWindow();
	}

	public abstract void draw();

	public int getDrawDivisor() {
		return 1;
	}

	public void clear() {

		Canvas c = getHolder().lockCanvas();
		if (c != null) {
			c.drawColor(Color.BLACK);
			getHolder().unlockCanvasAndPost(c);
		}
	}

}
