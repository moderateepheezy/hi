package com.exolvetechnologies.hidoctor.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import com.oovoo.sdk.api.ui.VideoPanel;

public class VideoPanelRect extends VideoPanel {
	
	public VideoPanelRect(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Point point = (Point) getTag();
        int height = getMeasuredHeight();
        if (point != null) {
            height = point.y;
        }
        
        setMeasuredDimension(getMeasuredWidth(), height);
    }
}
