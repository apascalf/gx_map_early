package view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import model.SensorValue;
import pages.inclinoDrawer;


public class inclinoView2 extends View {
    private final Handler mHandler = new Handler();
    private final Runnable mDraw = new Runnable() {
        @Override
        public void run() {
            invalidate();
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 1000 / 60);
        }
    };

    private inclinoDrawer minclinoDrawer;
    private boolean mIsPortrait;

    public inclinoView2(Context context) {
        super(context);
        init(context);
    }

    public inclinoView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public inclinoView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mIsPortrait = ((float) displayMetrics.heightPixels) / ((float) displayMetrics.widthPixels) > 1.4f;
        minclinoDrawer = new inclinoDrawer(context);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int mode2 = MeasureSpec.getMode(heightMeasureSpec);

        int rawWidth = MeasureSpec.getSize(widthMeasureSpec);
        int rawHeight = MeasureSpec.getSize(heightMeasureSpec);

        int width = (int) ((this.mIsPortrait ? 1.0f : 0.8f) * ((float) rawWidth));
        int height = (int) (((float) rawWidth) * 0.86f);
        if (mode == MeasureSpec.EXACTLY) {
            width = rawWidth;
        } else if (mode == Integer.MIN_VALUE) {
            width = Math.min(width, rawWidth);
        }
        if (mode2 == MeasureSpec.EXACTLY) {
            height = rawHeight;
        } else if (mode2 == Integer.MIN_VALUE) {
            height = Math.min(height, rawHeight);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.post(mDraw);

    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacks(mDraw);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        minclinoDrawer.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        minclinoDrawer.draw(canvas);
    }

    public SensorValue getSensorValue() {
        return minclinoDrawer.getSensorValue();
    }
    public inclinoDrawer getMinclinoDrawer(){
        return this.minclinoDrawer;
    }
}
