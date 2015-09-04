package de.tu.darmstadt.seemoo.ansian.gui.tabs;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ViewPager for the main views
 *
 */
public class MyViewPager extends ViewPager {

	private double factor = 0.1;

	public MyViewPager(Context context) {
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getX() < factor * getWidth() || ev.getX() > (1 - factor) * getWidth()) {
			try {
				return super.onInterceptTouchEvent(ev);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return false;

	}

}
