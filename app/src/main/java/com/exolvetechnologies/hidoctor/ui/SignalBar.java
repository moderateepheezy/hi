package com.exolvetechnologies.hidoctor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RatingBar;

public class SignalBar extends RatingBar {

	private final int MIN_LEVEL = 1;
	private final int MAX_LEVEL = 4;
	private final Paint paint = new Paint();
	private int mLevel = MAX_LEVEL;

	private int mLevelColor = Color.YELLOW ;
	private int mLevelColorBg = Color.argb(50, 0x2f, 0x4f, 0x4f);
 
	public SignalBar(Context context) {
		super(context);
	}
 
	public SignalBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
 
	public SignalBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        setMeasuredDimension(getMeasuredHeight(), getMeasuredHeight());
    }
 
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		paint.setAntiAlias(true);
	}
 
	@Override
	public void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		final float w = getWidth();
		final float h = getHeight();
		final float sw = w / MAX_LEVEL;
		
		for (int i = 1; i <= MAX_LEVEL; ++i) {
			final float cx = (float)((MAX_LEVEL - i - 1) + 0.5)*sw/2;
			final float r = sw/1.5f;
			float x = (float)(w/2 + r/2);
			float y = (float)(h/2);
			float deltaY = ((y/2)/MAX_LEVEL)*i;
 
			paint.setStrokeWidth((float) -1.0);
			paint.setStyle(Paint.Style.FILL);
			if (i <= getLevel()) {
				paint.setColor(mLevelColor);
			} else {
				paint.setColor(mLevelColorBg);
			}

			canvas.drawRect(new RectF(x - cx, y - deltaY, x - cx + sw/2.5f, h/1.5f), paint);
		}
	}
 
	public void setLevel(int level) {
		if (MIN_LEVEL <= level && level <= MAX_LEVEL) {
			mLevel = level;
			invalidate();
		} else {
			throw new IllegalArgumentException("Level should be greater or equal 1 and less or equal 5!");
		}
	}
 
	public int getLevel() {
		return mLevel;
	}
}
