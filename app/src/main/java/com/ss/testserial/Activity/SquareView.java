package com.ss.testserial.Activity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Listen on 2016/10/23.
 */
public class SquareView extends RelativeLayout {
    public SquareView(Context context) {
        super(context);
    }
    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}