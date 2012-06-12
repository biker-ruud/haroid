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
public final class GraphView extends View {
    private static final String LOG_TAG = "GraphView";
    private static final float MARGIN = 10.0f;

    private int maxUnits;
    private int maxPeriod;
    private int measuredWidth;
    private int measuredHeight;
    private List<GeschiedenisMonitor.UsagePoint> usagePointList;

    public GraphView(Context context) {
        super(context);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public void setMaxPeriod(int maxPeriod) {
        this.maxPeriod = maxPeriod;
    }

    public void setUsage(List<GeschiedenisMonitor.UsagePoint> usagePointList) {
        this.usagePointList = usagePointList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(LOG_TAG, "onDraw");
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        drawGraphXaxis(canvas, paint);
        drawGraphYaxis(canvas, paint);
        drawAverageUsageLine(canvas);
        drawCurrentUsageLines(canvas, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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

    private void drawGraphXaxis(Canvas canvas, Paint paint) {
        canvas.drawLine(MARGIN, this.measuredHeight - MARGIN, this.measuredWidth - MARGIN, this.measuredHeight - MARGIN, paint);
    }

    private void drawGraphYaxis(Canvas canvas, Paint paint) {
        canvas.drawLine(MARGIN, MARGIN, MARGIN, this.measuredHeight - MARGIN, paint);
    }

    private void drawAverageUsageLine(Canvas canvas) {
        Paint averagePaint = new Paint();
        averagePaint.setColor(Color.GRAY);
        averagePaint.setStyle(Paint.Style.STROKE);
        averagePaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        canvas.drawLine(getMinX(), getMaxY(), getMaxX(), getMinY(), averagePaint);
    }

    private void drawCurrentUsageLines(Canvas canvas, Paint paint) {
        if (this.maxPeriod > 0 && this.maxUnits > 0) {
            int fromPeriod = 0;
            int fromUnit = this.maxUnits;
            for (GeschiedenisMonitor.UsagePoint usagePoint : this.usagePointList) {
                int toPeriod = usagePoint.getDagInPeriode();
                int toUnit = usagePoint.getTegoed();
                drawCurrentUsageLine(canvas, paint, fromPeriod, fromUnit, toPeriod, toUnit);
                fromPeriod = toPeriod;
                fromUnit = toUnit;
            }
        }
    }

    private void drawCurrentUsageLine(Canvas canvas, Paint paint, int fromPeriod, int fromUnit, int toPeriod, int toUnit) {
        Point fromPoint = new Point(fromPeriod, fromUnit);
        PointF fromGraphPoint = transformToGraphCoordinates(fromPoint);
        Point toPoint = new Point(toPeriod, toUnit);
        PointF toGraphPoint = transformToGraphCoordinates(toPoint);
        canvas.drawLine(fromGraphPoint.x, fromGraphPoint.y, toGraphPoint.x, toGraphPoint.y, paint);
//        float currentXvector = ((float)this.currentPeriod) / ((float)this.maxPeriod);
//        float currentYvector = ((float)this.currentUnits) / ((float)this.maxUnits);
//        float xCoordinate = ((getMaxX() - getMinX()) * currentXvector) + getMinX();
//        float yCoordinate = ((getMaxY() - getMinY()) * currentYvector) + getMinY();
    }

    private PointF transformToGraphCoordinates(Point point) {
        float period = ((float)point.x) / ((float)this.maxPeriod);
        float usage = ((float)point.y) / ((float)this.maxUnits);
        float xCoordinate = ((getMaxX() - getMinX()) * period) + getMinX();
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        return new PointF(xCoordinate, yCoordinate);
    }

    private float getMinX() {
        return MARGIN;
    }

    private float getMaxX() {
        return this.measuredWidth - (2.0f * MARGIN);
    }

    private float getMinY() {
        return this.measuredHeight - MARGIN;
    }

    private float getMaxY() {
        return 2.0f * MARGIN;
    }
}
