package nl.haroid;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;

import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class DailyGraphView extends GraphView{
    private static final String LOG_TAG = "DailyGraphView";

    private float dailyAverage;
    private int maxUnits;
    private int maxPeriod;
    private int peakUsage;
    private List<GeschiedenisMonitor.UsagePoint> usagePointList;

    public DailyGraphView(Context context) {
        super(context);
    }

    public DailyGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DailyGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void setDailyAverage(float dailyAverage) {
        this.dailyAverage = dailyAverage;
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public void setMaxPeriod(int maxPeriod) {
        this.maxPeriod = maxPeriod;
    }

    public void setUsage(List<GeschiedenisMonitor.UsagePoint> usagePointList) {
        this.usagePointList = usagePointList;
        this.peakUsage = (int) Math.ceil(this.dailyAverage);
        Log.i(LOG_TAG, "Usage set.");
        for (GeschiedenisMonitor.UsagePoint usagePoint : usagePointList) {
            if (usagePoint.getTegoed() > this.peakUsage) {
                this.peakUsage = usagePoint.getTegoed();
                Log.i(LOG_TAG, "Piek verbruik: " + this.peakUsage);
            }
        }
    }

    @Override
    void drawInternal(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        drawGraphXaxis(canvas, paint);
        drawGraphYaxis(canvas, paint);
        drawAverageUsageLine(canvas);
        drawDailyUsageBars(canvas, paint);
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

        float usage = this.dailyAverage / ((float)this.peakUsage);
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        canvas.drawLine(getMinX(), yCoordinate, getMaxX(), yCoordinate, averagePaint);
    }

    private void drawDailyUsageBars(Canvas canvas, Paint paint) {
        if (this.usagePointList != null) {
            for (GeschiedenisMonitor.UsagePoint usagePoint : this.usagePointList) {
                drawDailyUsageBar(canvas, paint, usagePoint);
            }
        }
    }

    private void drawDailyUsageBar(Canvas canvas, Paint paint, GeschiedenisMonitor.UsagePoint usagePoint) {
        float usage = ((float)usagePoint.getTegoed()) / ((float)this.peakUsage);
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();

        int numberOfHorizontalPositions = this.maxPeriod + (this.maxPeriod - 1);
        int dayStartIndex = (usagePoint.getDagInPeriode()-1) * 2;

        float leftBar = ((float)dayStartIndex) / ((float)numberOfHorizontalPositions);
        float rightBar = ((float)dayStartIndex+1) / ((float)numberOfHorizontalPositions);
        float leftXCoordinate = ((getMaxX() - getMinX()) * leftBar) + getMinX();
        float rightXCoordinate = ((getMaxX() - getMinX()) * rightBar) + getMinX();

        paint.setStrokeWidth(0f);
        if (((float)usagePoint.getTegoed()) < this.dailyAverage) {
            paint.setColor(Color.GREEN);
        } else {
            paint.setColor(Color.rgb(255, 165, 0));
        }
        canvas.drawRect(leftXCoordinate, yCoordinate, rightXCoordinate, this.measuredHeight - MARGIN, paint);
    }

}
