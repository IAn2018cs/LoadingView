package cn.ian2018.loadingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Description:
 * Author:chenshuai
 * E-mail:chenshuai@amberweather.com
 * Date:2019/3/22
 */
public class ImageLoadingView extends android.support.v7.widget.AppCompatImageView {

    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private int current = 0;

    public ImageLoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public ImageLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    public void setCurrent(int current, int max) {
        this.current = (mWidth + mHeight) * 2 * current / max;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (current <= mWidth) {
            // 画第一条线
            canvas.drawLine(0, 0, current, 0, mPaint);
        } else if (current <= mWidth + mHeight) {
            canvas.drawLine(0, 0, mWidth, 0, mPaint);
            // 画第二条线
            canvas.drawLine(mWidth, 0, mWidth, current - mWidth, mPaint);
        } else if (current > mWidth + mHeight && current <= mWidth * 2 + mHeight) {
            canvas.drawLine(0, 0, mWidth, 0, mPaint);
            canvas.drawLine(mWidth, 0, mWidth, mHeight, mPaint);
            // 画第三条线
            canvas.drawLine(mWidth, mHeight, mWidth - (current - mWidth - mHeight), mHeight, mPaint);
        } else {
            canvas.drawLine(0, 0, mWidth, 0, mPaint);
            canvas.drawLine(mWidth, 0, mWidth, mHeight, mPaint);
            canvas.drawLine(mWidth, mHeight, 0, mHeight, mPaint);
            // 画第四条线
            canvas.drawLine(0, mHeight, 0, mHeight - (current - mWidth * 2 - mHeight), mPaint);
        }
    }
}
