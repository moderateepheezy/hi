package com.exolvetechnologies.hidoctor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class VideoPanelPreviewRect extends LinearLayout {

	public VideoPanelPreviewRect(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }
}
