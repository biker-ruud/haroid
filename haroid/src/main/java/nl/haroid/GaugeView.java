package nl.haroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import nl.haroid.common.Theme;
import nl.haroid.util.ThemeSwitcherUtil;

/**
 * @author Ruud de Jong
 */
public final class GaugeView extends View {
    private static final String LOG_TAG = "GaugeView";

    private int measuredWidth;
    private int measuredHeight;

    private Paint smallTickPaint;
    private Paint mediumTickPaint;
    private Paint largeTickPaint;
    private Paint needlePaint;

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        smallTickPaint = new Paint();
        smallTickPaint.setColor(Color.BLACK);
        mediumTickPaint = new Paint();
        mediumTickPaint.setColor(Color.BLUE);
        largeTickPaint = new Paint();
        largeTickPaint.setColor(Color.GREEN);
        needlePaint = new Paint();
        needlePaint.setColor(Color.RED);
    }

    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float strokeWidth = measuredWidth / 100.0f;
        float startY = measuredWidth * 0.1f;
        float stopY = measuredWidth * 0.05f;
        largeTickPaint.setStrokeWidth(strokeWidth);
        mediumTickPaint.setStrokeWidth(strokeWidth / 2.0f);
        smallTickPaint.setStrokeWidth(strokeWidth / 3.0f);
        needlePaint.setStrokeWidth(strokeWidth / 3.0f);
        float center = measuredWidth / 2.0f;
        Log.i(LOG_TAG, "start Y: " + startY);
        Log.i(LOG_TAG, "stop Y: " + stopY);
        drawLines(canvas, center, startY, stopY);
        drawNeedle(canvas, center, measuredWidth * 0.01f);
    }

    private void drawLines(Canvas canvas, float center, float startY, float stopY) {
        canvas.save();
        int maxGraph = calculateMaxGraph(673);
        int smallTick = 10;
        int mediumTick = 50;
        int largeTick = 100;
        int numberOfTick = maxGraph / smallTick;
        float anglePerStep = calculateAngle(maxGraph);
        canvas.rotate(-135.0f, center, center);
        for (int i=0; i<=numberOfTick; i++) {
            if ((i*smallTick) % largeTick == 0) {
                // large tick
                canvas.drawLine(center, startY, center, stopY, largeTickPaint);
            } else if ((i*smallTick) % mediumTick == 0) {
                // medium tick
                float distance = Math.abs(startY - stopY);
                float mediumDistance = 2.0f * distance / 3.0f;
                canvas.drawLine(center, startY, center, startY - mediumDistance, mediumTickPaint);
            } else {
                // small tick
                float distance = Math.abs(startY - stopY);
                float smallDistance = distance / 3.0f;
                canvas.drawLine(center, startY, center, startY - smallDistance, smallTickPaint);
            }
            canvas.rotate(anglePerStep, center, center);
        }
        canvas.restore();
    }

    private void drawNeedle(Canvas canvas, float center, float radius) {
        int currentPos = 650;
        int maxGraph = calculateMaxGraph(673);
        float needlePercentage = ((float)currentPos) / ((float)maxGraph);
        float angle = -135.0f + (needlePercentage * 270.0f);
        canvas.rotate(angle, center, center);
        canvas.drawLine(center, center, center, 5.0f * radius, needlePaint);
        canvas.drawCircle(center, center, 2.0f * radius, smallTickPaint);
    }

    private float calculateAngle(int maxGraph) {
        int unitsBetweenLines = 10;
        float max = maxGraph;
        float stepsize = unitsBetweenLines;
        float totalAngle = 270.0f;
        return (stepsize / max) * totalAngle;
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.measuredWidth = parentWidth;
        this.measuredHeight = parentWidth;
        Log.i(LOG_TAG, "setMeasuredDimension: " + this.measuredWidth + ", " + this.measuredHeight);
        this.setMeasuredDimension(this.measuredWidth, this.measuredHeight);
    }

    private Paint getSolidLinePaint() {
        Paint paint = new Paint();
        if (ThemeSwitcherUtil.getChosenTheme() == Theme.LIGHT) {
            // Newer Android uses Light theme, so black lines
            paint.setColor(Color.BLACK);
        } else {
            // Old Android uses Black theme, so white lines
            paint.setColor(Color.WHITE);
        }
        return paint;
    }

    private int calculateMaxGraph(int maxUnits) {
        double maxUnitDouble = (double) maxUnits;
        double magnitude = Math.floor(Math.log10(maxUnitDouble));
        double base = Math.pow(10.0d, magnitude-1.0d);
        double graphHeight = Math.ceil(maxUnitDouble / base) * base;
        return (int) graphHeight;
    }

}
