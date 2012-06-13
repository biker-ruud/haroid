package nl.haroid;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class MonthlyGraphView extends GraphView {

    private int maxUnits;
    private int maxPeriod;
    private List<GeschiedenisMonitor.UsagePoint> usagePointList;

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
    }

    public void setMaxPeriod(int maxPeriod) {
        this.maxPeriod = maxPeriod;
    }

    public void setUsage(List<GeschiedenisMonitor.UsagePoint> usagePointList) {
        this.usagePointList = usagePointList;
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
    }

    private PointF transformToGraphCoordinates(Point point) {
        float period = ((float)point.x) / ((float)this.maxPeriod);
        float usage = ((float)point.y) / ((float)this.maxUnits);
        float xCoordinate = ((getMaxX() - getMinX()) * period) + getMinX();
        float yCoordinate = ((getMaxY() - getMinY()) * usage) + getMinY();
        return new PointF(xCoordinate, yCoordinate);
    }

}
