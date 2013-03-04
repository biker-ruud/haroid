package nl.haroid;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import nl.haroid.service.HistoryMonitor;

import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class MonthlyGraphView extends GraphView {
    private static final String LOG_TAG = "MonthlyGraphView";

    private int maxUnits;
    private int peakUnits;
    private int maxGraph;
    private int maxPeriod;
    private List<HistoryMonitor.UsagePoint> usagePointList;

    public MonthlyGraphView(Context context) {
        super(context);
    }

    public MonthlyGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MonthlyGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
        if (maxUnits > this.peakUnits) {
            this.peakUnits = maxUnits;
        }
        this.maxGraph = calculateMaxGraph(peakUnits);
    }

    public void setMaxPeriod(int maxPeriod) {
        this.maxPeriod = maxPeriod;
    }

    public void setUsage(List<HistoryMonitor.UsagePoint> usagePointList) {
        this.usagePointList = usagePointList;
        for (HistoryMonitor.UsagePoint usagePoint : usagePointList) {
            if (usagePoint.getBalance() > this.peakUnits) {
                this.peakUnits = usagePoint.getBalance();
            }
        }
        this.maxGraph = calculateMaxGraph(peakUnits);
    }

    @Override
    void drawInternal(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        drawGraphXaxis(canvas, paint);
        drawGraphYaxis(canvas, paint);
        drawAverageUsageLine(canvas);
        drawCurrentUsageLines(canvas, paint);
    }

    private void drawGraphXaxis(Canvas canvas, Paint paint) {
        canvas.drawLine(getMinX(), getMinY(), getMaxX(), getMinY(), paint);
        if (this.maxPeriod > 0) {
            drawHorizontalMarker(5, canvas, paint);
            drawHorizontalMarker(10, canvas, paint);
            drawHorizontalMarker(15, canvas, paint);
            drawHorizontalMarker(20, canvas, paint);
            drawHorizontalMarker(25, canvas, paint);
            if (this.maxPeriod >= 30) {
                drawHorizontalMarker(30, canvas, paint);
            }
        }
    }

    private void drawGraphYaxis(Canvas canvas, Paint paint) {
        canvas.drawLine(getMinX(), getMinY(), getMinX(), getMaxY(), paint);
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

    private void drawVerticalMarker(int markerPos, Canvas canvas) {
        float usage = ((float)markerPos) / ((float)this.maxGraph);
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        float markerXcoordinateStart = getMinX();
        float markerXcoordinateEnd = markerXcoordinateStart - getHorizontalMarkerSize();
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawLine(markerXcoordinateStart, yCoordinate, markerXcoordinateEnd, yCoordinate, paint);
        paint.setTextSize(getTextSize());
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf(markerPos), markerXcoordinateEnd, yCoordinate + (getTextSize() / 3.0f), paint);
    }

    private void drawHorizontalMarker(int pos, Canvas canvas, Paint paint) {
        float markerXpos = ((float)pos) / ((float)this.maxPeriod);
        float markerXCoordinate = ((getMaxX() - getMinX()) * markerXpos) + getMinX();
        float markerYcoordinateStart = getMinY();
        float markerYcoordinateEnd = markerYcoordinateStart + getVerticalMarkerSize();
        canvas.drawLine(markerXCoordinate, markerYcoordinateStart, markerXCoordinate, markerYcoordinateEnd, paint);
        paint.setTextSize(getTextSize());
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(pos), markerXCoordinate, markerYcoordinateEnd + getTextSize(), paint);
    }

    private void drawAverageUsageLine(Canvas canvas) {
        if (this.maxPeriod > 0 && this.maxGraph > 0) {
            Paint averagePaint = new Paint();
            averagePaint.setColor(Color.GRAY);
            averagePaint.setStyle(Paint.Style.STROKE);
            averagePaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
            float startAverage = ((float)this.maxUnits) / ((float)this.maxGraph);
            float maxYCoordinate = ((getMaxY() - getMinY()) * startAverage) + getMinY();
            canvas.drawLine(getMinX(), maxYCoordinate, getMaxX(), getMinY(), averagePaint);
        }
    }

    private void drawCurrentUsageLines(Canvas canvas, Paint paint) {
        if (this.maxPeriod > 0 && this.maxUnits > 0) {
            int fromPeriod = 0;
            int fromUnit = this.maxUnits;
            for (HistoryMonitor.UsagePoint usagePoint : this.usagePointList) {
                if (usagePoint.getBalance() != -1 && usagePoint.getDagInPeriode() == 0) {
                    fromUnit = usagePoint.getBalance();
                }
                if (usagePoint.getBalance() != -1 && usagePoint.getDagInPeriode() > 0) {
                    int toPeriod = usagePoint.getDagInPeriode();
                    int toUnit = usagePoint.getBalance();
                    drawCurrentUsageLine(canvas, paint, fromPeriod, fromUnit, toPeriod, toUnit);
                    fromPeriod = toPeriod;
                    fromUnit = toUnit;
                }
            }
        }
    }

    private void drawCurrentUsageLine(Canvas canvas, Paint paint, int fromPeriod, int fromUnit, int toPeriod, int toUnit) {
        Point fromPoint = new Point(fromPeriod, fromUnit);
        PointF fromGraphPoint = transformToGraphCoordinates(fromPoint);
        Point toPoint = new Point(toPeriod, toUnit);
        PointF toGraphPoint = transformToGraphCoordinates(toPoint);
        canvas.drawLine(fromGraphPoint.x, fromGraphPoint.y, toGraphPoint.x, toGraphPoint.y, paint);
    }

    private PointF transformToGraphCoordinates(Point point) {
        float period = ((float)point.x) / ((float)this.maxPeriod);
        float usage = ((float)point.y) / ((float)this.maxGraph);
        float xCoordinate = ((getMaxX() - getMinX()) * period) + getMinX();
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        return new PointF(xCoordinate, yCoordinate);
    }

}
