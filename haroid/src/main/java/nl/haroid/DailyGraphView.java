package nl.haroid;

import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import nl.haroid.service.HistoryMonitor;

import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class DailyGraphView extends GraphView{
    private static final String LOG_TAG = "DailyGraphView";

    private float dailyAverage;
    private int maxUnits;
    private int maxPeriod;
    private int maxGraph;
    private List<HistoryMonitor.UsagePoint> usagePointList;

    public DailyGraphView(Context context) {
        super(context);
    }

    public DailyGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DailyGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
        calculateDailyAverage();
    }

    public void setMaxPeriod(int maxPeriod) {
        this.maxPeriod = maxPeriod;
        calculateDailyAverage();
    }

    public void setUsage(List<HistoryMonitor.UsagePoint> usagePointList) {
        this.usagePointList = usagePointList;
        int peakUsage = (int) Math.ceil(this.dailyAverage);
        for (HistoryMonitor.UsagePoint usagePoint : usagePointList) {
            if (usagePoint.getUsed() > peakUsage) {
                peakUsage = usagePoint.getUsed();
            }
        }
        this.maxGraph = calculateMaxGraph(peakUsage);
    }

    @Override
    void drawInternal(Canvas canvas) {
        drawGraphXaxis(canvas);
        drawGraphYaxis(canvas);
        if (this.maxGraph > 0) {
            drawAverageUsageLine(canvas);
            drawDailyUsageBars(canvas);
        }
    }

    private void calculateDailyAverage() {
        if (this.maxPeriod > 0 && this.maxUnits > 0) {
            this.dailyAverage = ((float)maxUnits) / ((float)maxPeriod);
        }
    }

    private void drawGraphXaxis(Canvas canvas) {
        canvas.drawLine(getMinX(), getMinY(), getMaxX(), getMinY(), getSolidLinePaint());
        drawHorizontalMarkers(canvas);
    }

    private void drawGraphYaxis(Canvas canvas) {
        canvas.drawLine(getMinX(), getMinY(), getMinX(), getMaxY(), getSolidLinePaint());
        if (this.maxGraph > 0) {
            int markerSize = this.maxGraph / 4;
            drawVerticalMarker(markerSize, canvas);
            drawVerticalMarker(markerSize*2, canvas);
            drawVerticalMarker(markerSize*3, canvas);
            if (markerSize*4 < maxGraph) {
                drawVerticalMarker(markerSize*4, canvas);
            }
        }
    }

    private void drawAverageUsageLine(Canvas canvas) {
        Paint averagePaint = new Paint();
        averagePaint.setColor(Color.GRAY);
        averagePaint.setStyle(Paint.Style.STROKE);
        averagePaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        float usage = this.dailyAverage / ((float)this.maxGraph);
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        canvas.drawLine(getMinX(), yCoordinate, getMaxX(), yCoordinate, averagePaint);
    }

    private void drawDailyUsageBars(Canvas canvas) {
        if (this.usagePointList != null) {
            for (HistoryMonitor.UsagePoint usagePoint : this.usagePointList) {
                if (usagePoint.getDagInPeriode() > 0) {
                    drawDailyUsageBar(canvas, usagePoint);
                }
            }
        }
    }

    private void drawDailyUsageBar(Canvas canvas, HistoryMonitor.UsagePoint usagePoint) {
        float usage = ((float)usagePoint.getUsed()) / ((float)this.maxGraph);
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();

        int numberOfHorizontalPositions = this.maxPeriod + (this.maxPeriod - 1);
        int dayStartIndex = (usagePoint.getDagInPeriode()-1) * 2;

        float leftBar = ((float)dayStartIndex) / ((float)numberOfHorizontalPositions);
        float rightBar = ((float)dayStartIndex+1) / ((float)numberOfHorizontalPositions);
        float leftXCoordinate = ((getMaxX() - getMinX()) * leftBar) + getMinX();
        float rightXCoordinate = ((getMaxX() - getMinX()) * rightBar) + getMinX();

        int averagedBalance = this.maxUnits - ((usagePoint.getDagInPeriode() * this.maxUnits) / this.maxPeriod);
        boolean currentBalanceLowerThanAverage = usagePoint.getBalance() < averagedBalance;

        Paint paint = getSolidLinePaint();
        paint.setStrokeWidth(0f);
        if (currentBalanceLowerThanAverage && usagePoint.getBalance() != -1) {
            paint.setColor(Color.RED);
        } else if (((float)usagePoint.getUsed()) < this.dailyAverage) {
            paint.setColor(Color.GREEN);
        } else {
            paint.setColor(Color.rgb(255, 165, 0));
        }
        canvas.drawRect(leftXCoordinate, yCoordinate, rightXCoordinate, getMinY(), paint);
    }

    private void drawHorizontalMarkers(Canvas canvas) {
        for (int i=1; i<=maxPeriod; i++) {
            if (i % 5 == 0) {
                int numberOfHorizontalPositions = this.maxPeriod + (this.maxPeriod - 1);
                int dayStartIndex = (i-1) * 2;

                float leftBar = ((float)dayStartIndex) / ((float)numberOfHorizontalPositions);
                float rightBar = ((float)dayStartIndex+1) / ((float)numberOfHorizontalPositions);
                float leftXCoordinate = ((getMaxX() - getMinX()) * leftBar) + getMinX();
                float rightXCoordinate = ((getMaxX() - getMinX()) * rightBar) + getMinX();

                float markerXCoordinate = (leftXCoordinate + rightXCoordinate) / 2.0f;
                drawHorizontalMarkerLine(i, markerXCoordinate, canvas);
            }
        }
    }

    private void drawHorizontalMarkerLine(int markerPos, float markerXCoordinate, Canvas canvas) {
        float markerYcoordinateStart = getMinY();
        float markerYcoordinateEnd = markerYcoordinateStart + getVerticalMarkerSize();
        Paint paint = getSolidLinePaint();
        canvas.drawLine(markerXCoordinate, markerYcoordinateStart, markerXCoordinate, markerYcoordinateEnd, paint);
        paint.setTextSize(getTextSize());
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(markerPos), markerXCoordinate, markerYcoordinateEnd + getTextSize(), paint);
    }

    private void drawVerticalMarker(int markerPos, Canvas canvas) {
        float usage = ((float)markerPos) / ((float)this.maxGraph);
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        float markerXcoordinateStart = getMinX();
        float markerXcoordinateEnd = markerXcoordinateStart - getHorizontalMarkerSize();
        Paint paint = getSolidLinePaint();
        canvas.drawLine(markerXcoordinateStart, yCoordinate, markerXcoordinateEnd, yCoordinate, paint);
        paint.setTextSize(getTextSize());
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf(markerPos), markerXcoordinateEnd, yCoordinate + (getTextSize() / 3.0f), paint);
    }

}
