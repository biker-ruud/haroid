package nl.haroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author Ruud de Jong
 */
abstract class GraphView extends View {
    private static final String LOG_TAG = "GraphView";
    private static final float MARGIN_LEFT_PERCENTAGE = 6.0f;
    private static final float MARGIN_RIGHT_PERCENTAGE = 2.0f;
    private static final float MARGIN_TOP_PERCENTAGE = 2.0f;
    private static final float MARGIN_BOTTOM_PERCENTAGE = 6.0f;

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
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.measuredWidth = parentWidth;
        this.measuredHeight = (parentWidth * 3) / 4;
        Log.i(LOG_TAG, "setMeasuredDimension: " + this.measuredWidth + ", " + this.measuredHeight);
        this.setMeasuredDimension(this.measuredWidth, this.measuredHeight);
    }

    protected float getLeftMargin() {
        return ((float)this.measuredWidth) * MARGIN_LEFT_PERCENTAGE / 100.0f;
    }

    protected float getRightMargin() {
        return ((float)this.measuredWidth) * MARGIN_RIGHT_PERCENTAGE / 100.0f;
    }

    protected float getTopMargin() {
        return ((float)this.measuredHeight) * MARGIN_TOP_PERCENTAGE / 100.0f;
    }

    protected float getBottomMargin() {
        return ((float)this.measuredHeight) * MARGIN_BOTTOM_PERCENTAGE / 100.0f;
    }

    protected float getMinX() {
        return getLeftMargin();
    }

    protected float getMaxX() {
        return this.measuredWidth - (getRightMargin());
    }

    protected float getMinY() {
        return this.measuredHeight - getBottomMargin();
    }

    protected float getMaxY() {
        return getTopMargin();
    }

    protected float getTextSize() {
        return ((float)this.measuredHeight) / 30.0f;
    }

    protected float getVerticalMarkerSize() {
        return ((float)this.measuredHeight) / 75.0f;
    }

    protected float getHorizontalMarkerSize() {
        return ((float)this.measuredWidth) / 75.0f;
    }

    protected int calculateMaxGraph(int maxUnits) {
        double maxUnitDouble = (double) maxUnits;
        double magnitude = Math.floor(Math.log10(maxUnitDouble));
        double base = Math.pow(10.0d, magnitude-1.0d);
        double graphHeight = Math.ceil(maxUnitDouble / base) * base;
        return (int) graphHeight;
    }

}
