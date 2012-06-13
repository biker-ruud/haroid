package nl.haroid;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * @author Ruud de Jong
 */
abstract class GraphView extends View {
    private static final String LOG_TAG = "GraphView";
    protected static final float MARGIN = 10.0f;

    protected int measuredWidth;
    protected int measuredHeight;

    public GraphView(Context context) {
        super(context);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawInternal(canvas);
    }

    abstract void drawInternal(Canvas canvas);

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(LOG_TAG, "onMeasure: " + widthMeasureSpec + ", " + heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.i(LOG_TAG, "parent: " + parentWidth + ", " + parentHeight);
        this.measuredWidth = parentWidth;
        this.measuredHeight = (parentWidth * 3) / 4;
        Log.i(LOG_TAG, "setMeasuredDimension: " + this.measuredWidth + ", " + this.measuredHeight);
        this.setMeasuredDimension(this.measuredWidth, this.measuredHeight);
    }

    protected float getMinX() {
        return MARGIN;
    }

    protected float getMaxX() {
        return this.measuredWidth - (2.0f * MARGIN);
    }

    protected float getMinY() {
        return this.measuredHeight - MARGIN;
    }

    protected float getMaxY() {
        return 2.0f * MARGIN;
    }
}
