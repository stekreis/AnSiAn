package de.tu.darmstadt.seemoo.ansian.gui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.DataEvent;
import de.tu.darmstadt.seemoo.ansian.model.WaveformDrawDataAdapter;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;

/**
 * @author Steffen Kreis
 * 
 *         Displays data in time spectrum as waveform. Also takes care of
 *         scaling (zooming) internally as this is not needed outside this class
 *         anyway. Scrolling in paused mode is planned, not implemented by now.
 *         Imaginary part of data is not shown as it would decrease the size for
 *         the real part and therefore the user experience. The real part
 *         suffices to show desired information.
 * 
 * 
 *         Roughly oriented at
 *         https://github.com/google/ringdroid/blob/master/src/com/ringdroid/
 *         WaveformView.java
 */
public class WaveformView extends MySurfaceView {

	private WaveformDrawDataAdapter drawDataAdapter;
	private Paint mPaintRe;
	private Paint mPaintIm;
	private float shownDataAmount = 1;
	private WaveformDrawDataAdapter wfDrawDataAdapter;
	private final int MAXSAMPLEPACKETS = 20;
	private IQSourceInterface source;
	private final float MAX_YAMP = 70;
	private final float MIN_YAMP = 1;

	private long oldestShownSample;
	private long newestShownSample = 0;

	private String LOGTAG = "WaveformView";

	private long freq;

	private float yAmplifier = 15;

	public WaveformView(Context context) {
		this(context, null, 0);
	}

	public WaveformView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaveformView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
		source = SourceControl.getSource();
		oldestShownSample = source.getPacketSize();
		newestShownSample = 0;
	}

	/**
	 * Returns the height/width of the frequency/power grid in px
	 *
	 * @return size of the grid (frequency grid height / power grid width) in px
	 */
	public int getGridSize() {
		return (int) (75 * getResources().getDisplayMetrics().xdpi / 200);
	}

	private void initPaint() {
		mPaintRe = new Paint();
		mPaintRe.setStyle(Paint.Style.STROKE);
		mPaintRe.setColor(Color.WHITE);
		mPaintRe.setStrokeWidth(0);
		mPaintRe.setAntiAlias(true);
		mPaintIm = new Paint();
		mPaintIm.setStyle(Paint.Style.STROKE);
		mPaintIm.setColor(Color.GREEN);
		mPaintIm.setStrokeWidth(0);
		mPaintIm.setAntiAlias(true);
	}

	/**
	 * Updates the waveform view with a new "frame" of samples and renders it.
	 * The new frame gets added to the front of the rendering queue, pushing the
	 * previous frames back, causing them to be faded out visually.
	 *
	 * @param packet
	 *            the most recent buffer of audio samples
	 */
	public synchronized void draw() {
		// Draw:
		Canvas canvas = null;

		try {
			canvas = this.getHolder().lockCanvas();

			synchronized (this.getHolder()) {
				if (canvas != null) {
					// Draw all the components
					drawWaveform(canvas);

				} else
					Log.d(LOGTAG, "draw: Canvas is null.");
			}
		} catch (Exception e) {
			Log.e(LOGTAG, "draw: Error while drawing on the canvas. Stop!");
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				this.getHolder().unlockCanvasAndPost(canvas);
			}

		}
	}

	/**
	 * Repaints the view's surface.
	 *
	 * @param canvas
	 *            the {@link Canvas} object on which to draw
	 */
	private void drawWaveform(Canvas canvas) {
		// Clear the screen each time because SurfaceView won't do this for us.
		canvas.drawColor(Color.BLACK);

		if (drawDataAdapter == null || !StateHandler.isPaused()) {
			// drawDataAdapter = new
			// WaveformDrawDataAdapter(DataHandler.getInstance().getWaveformDrawData(shownDataAmount));
			freq = Preferences.GUI_PREFERENCE.getFrequency();
		}
		int width = getWidth();
		int height = getHeight();
		int centerY = height / 2;
		float leftBorder = width * 0.01f;
		float yPos = height * 0.01f;

		Rect bounds = new Rect();
		String text = String.format("Frequency: %4.3f MHz", freq / 1000000f)
				+ String.format(" Time window: %5.3f ms", (shownDataAmount * SourceControl.getSource().getSampleRate())
						/ SourceControl.getSource().getPacketSize());
		ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
		yPos += bounds.height();
		canvas.drawText(text, leftBorder, yPos, ColorPreference.TEXT_SMALL_PAINT);
		if (StateHandler.isDemodulating()) {
			text = String.format("showing demodulated Data (%4.3f MHz)",
					Preferences.GUI_PREFERENCE.getDemodFrequency() / 1000000f);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			yPos += bounds.height();
			canvas.drawText(text, leftBorder, yPos, ColorPreference.TEXT_SMALL_PAINT);
		}

		wfDrawDataAdapter = new WaveformDrawDataAdapter();
		float[] drawArrayRe = wfDrawDataAdapter.getDrawArrayRe(width, shownDataAmount, yAmplifier);

		int arReLength = drawArrayRe.length;
		if (drawArrayRe != null && arReLength > 0) {
			for (int x = 0; x < arReLength - 1; x++) {
				canvas.drawLine(x, centerY + yAmplifier * drawArrayRe[x], x + 1,
						centerY + yAmplifier * drawArrayRe[x + 1], mPaintRe);

			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean retVal = this.scaleGestureDetector.onTouchEvent(event);
		retVal = this.gestureDetector.onTouchEvent(event) || retVal;
		return retVal;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		shownDataAmount = 1;
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO calc and move
		if (newestShownSample != 0
				&& (oldestShownSample - newestShownSample) < (MAXSAMPLEPACKETS * source.getPacketSize())) {
			oldestShownSample += distanceX;
			newestShownSample += distanceX;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float xScale = ((float) detector.getCurrentSpanX()) / detector.getPreviousSpanX();
		yAmplifier *= (((float) detector.getCurrentSpanY()) / detector.getPreviousSpanY());
		yAmplifier = Math.max(Math.min(yAmplifier, MAX_YAMP), MIN_YAMP);
		shownDataAmount /= xScale;
		shownDataAmount = Math.min(Math.max(1, (shownDataAmount)), MAXSAMPLEPACKETS);
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getDrawDivisor() {
		return 1;
	}

	@Subscribe
	public void onEvent(DataEvent event) {
		//
	}

}
