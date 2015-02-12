package rodion.izotov.donetsk.tronestclock;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import android.view.ViewTreeObserver;

import android.widget.FrameLayout;


import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class extends View to Draw Trone Style Clock
 */
public class ClockView extends View implements ViewTreeObserver.OnPreDrawListener {

    private Timer currentClock;
    private final Calendar currentDate;

    /* Paints */

    private final int arcWidth;
    private final int arcPadding;
    private float radius;

    private final Paint gridPaint;
    private final Paint hoursPaintSimple;
    private final Paint hoursPaintBlur;
    private final Paint secondsPaintSimple;
    private final Paint secondsPaintBlur;

    private final boolean drawCenter;


    private RectF  hoursBound = new RectF();
    private RectF  minutesBound = new RectF();
    private RectF  secondsBound = new RectF();

    private final float degreesInWatch = 360.0f;
    private final int hoursInHalfADay = 12;

    /* constructors */
    public ClockView(Context context) {

        this(context, null, 0);
    }

    public ClockView(Context context, AttributeSet attributeSet) {

        this(context, attributeSet, 0);
    }

    public ClockView(Context context, AttributeSet attributeSet, int defStyle) {

        super(context, attributeSet, defStyle);

        Locale locale = getResources().getConfiguration().locale;
        currentDate = Calendar.getInstance(locale);

        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.ClockView);

        if (attrs == null)
            throw new AssertionError("Can't get attributes");

        this.getViewTreeObserver().addOnPreDrawListener(this);


        drawCenter = attrs.getBoolean(R.styleable.ClockView_drawGrid, true);
        Paint basePaint = createNewBasePaint(getResources().getDimension(R.dimen.arc_width));

        gridPaint = createNewGridPaint(basePaint, attrs);
        hoursPaintSimple = createNewHoursPaint(basePaint, attrs);
        hoursPaintBlur = new Paint(hoursPaintSimple);
        hoursPaintBlur.setMaskFilter( new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));

        secondsPaintSimple = createNewSecondsPaint(basePaint, attrs);
        secondsPaintBlur = new Paint(secondsPaintSimple);
        secondsPaintBlur.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));

        arcWidth = attrs.getDimensionPixelSize(R.styleable.ClockView_arcWidth, 0);
        arcPadding = attrs.getDimensionPixelSize(R.styleable.ClockView_arcPadding, 0);
    }

    private Paint createNewSecondsPaint(Paint basePaint, TypedArray attrs) {

        Paint newPaint = new Paint(basePaint);
        newPaint.setColor(attrs.getColor(R.styleable.ClockView_secondsColor, Color.WHITE));
        return newPaint;
    }

    Paint createNewBasePaint(float paintWidth) {

        Paint newPaint = new Paint();
        newPaint.setAntiAlias(true);
        newPaint.setDither(true);
        newPaint.setStyle(Paint.Style.STROKE);
        newPaint.setStrokeJoin(Paint.Join.ROUND);
        newPaint.setStrokeCap(Paint.Cap.ROUND);
        newPaint.setStrokeWidth(paintWidth);
        return newPaint;
    }

    Paint createNewGridPaint(Paint base, TypedArray attrs) {

        Paint newPaint = new Paint(base);
        newPaint.setStrokeWidth(1.0f);
        newPaint.setColor(attrs.getColor(R.styleable.ClockView_secondsColor, Color.WHITE));
        return newPaint;
    }

    Paint createNewHoursPaint(Paint base, TypedArray attrs) {

        Paint newPaint = new Paint(base);
        newPaint.setColor(attrs.getColor(R.styleable.ClockView_hoursColor, Color.BLUE));
        return newPaint;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {

        super.onVisibilityChanged(changedView, visibility);

        if (visibility == VISIBLE) {
            startClock();
        } else
        {
            stopClock();
        }

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        super.onSizeChanged(width, height, oldWidth, oldHeight);

        int xPadding = getPaddingLeft() + getPaddingRight();
        int yPadding = getPaddingTop() + getPaddingBottom();

        float diameter = Math.min(width - xPadding, height - yPadding);
        FrameLayout.LayoutParams layoutParams= new FrameLayout.LayoutParams(Math.round(diameter),Math.round(diameter));
        layoutParams.gravity = Gravity.CENTER;
        setLayoutParams(layoutParams);

        radius = diameter / 2.0f;
        RectF bound = new RectF(0, 0, diameter, diameter);

        minutesBound = new RectF(bound);
        minutesBound.inset((arcWidth + arcPadding), (arcWidth + arcPadding));
        hoursBound = new RectF(minutesBound);
        hoursBound.inset((arcWidth + arcPadding)*3, (arcWidth + arcPadding)*3);
        secondsBound = new RectF(minutesBound);
        secondsBound.inset((arcWidth + arcPadding) * 1.5f, (arcWidth + arcPadding) * 1.5f);

    }


    @Override
    public boolean onPreDraw() {

        currentDate.setTime(new Date());
        return true;
    }

    /* on every refresh */
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.rotate(-90.0f, hoursBound.centerX(), hoursBound.centerY());
        int hrs = currentDate.get(Calendar.HOUR_OF_DAY);
        int min = currentDate.get(Calendar.MINUTE);
        int sec = currentDate.get(Calendar.SECOND);
        int msec = currentDate.get(Calendar.MILLISECOND);

        msec += sec * 1000;

        /* if time in 24 hrs format turn it in 12 hrs */
        hrs = (hrs > hoursInHalfADay)?(hrs-hoursInHalfADay):(hrs);
        float hDegree = ((hrs / 12.0f) * degreesInWatch);
        float mDegree = ((min / 60.0f) * degreesInWatch);
        float sDegree = ((msec / 60000.0f) * degreesInWatch);

        /* draw grid, if asked */
        if (drawCenter) {

            drawGridArc(canvas);
        }

        drawSecondsArc(canvas, sDegree);
        drawHoursArc(canvas, hDegree);
        drawMinutesArc(canvas, mDegree);


    }

    private void drawMinutesArc(Canvas canvas, float mDegree) {

        canvas.drawArc(minutesBound,  mDegree + 15.0f, 345.0f, false, secondsPaintBlur);
        canvas.drawArc(minutesBound,  mDegree + 15.0f, 345.0f, false, secondsPaintSimple);
    }

    private void drawSecondsArc(Canvas canvas, float sDegree) {

        canvas.drawArc(secondsBound, sDegree - 5, 10, false, secondsPaintBlur);
        canvas.drawArc(secondsBound, sDegree, 5, false, secondsPaintSimple);
    }

    private void drawGridArc(Canvas canvas) {
        
        canvas.drawLine(minutesBound.centerX(),minutesBound.top,minutesBound.centerX(),minutesBound.bottom,gridPaint);
        canvas.drawLine(minutesBound.left, minutesBound.centerY(),minutesBound.right,minutesBound.centerY(),gridPaint);
        canvas.drawRect(minutesBound.left, minutesBound.top, minutesBound.right, minutesBound.bottom,gridPaint);
        canvas.drawCircle(minutesBound.centerX(), minutesBound.centerY(), radius, gridPaint);
    }

    protected void drawHoursArc(Canvas canvas, float hDegree) {

        canvas.drawArc(hoursBound,hDegree + 15.0f, 345.0f,false,hoursPaintBlur);
        canvas.drawArc(hoursBound,hDegree + 15.0f, 345.0f,false,hoursPaintSimple);
    }

    private void startClock() {

        stopClock();
        currentClock = new Timer();
        currentClock.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ClockView.this.postInvalidate();
            }
        }, 0, 1000/45);

    }

    private void stopClock() {

        if (currentClock != null) {
            currentClock.cancel();
            currentClock = null;
        }
    }
}
