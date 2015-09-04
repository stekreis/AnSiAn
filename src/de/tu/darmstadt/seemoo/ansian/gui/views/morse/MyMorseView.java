package de.tu.darmstadt.seemoo.ansian.gui.views.morse;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import de.greenrobot.event.EventBus;

public abstract class MyMorseView extends LinearLayout {

	public MyMorseView(Context context) {
		super(context);
		isInEditMode();
		init();
		setBackgroundColor(Color.BLACK);
	}

	public MyMorseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		isInEditMode();
		init();
		setBackgroundColor(Color.BLACK);
	}

	public MyMorseView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
		isInEditMode();
		init();
		setBackgroundColor(Color.BLACK);
	}

	protected abstract void init();

	public abstract void update();

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		EventBus.getDefault().register(this);
	}
}
