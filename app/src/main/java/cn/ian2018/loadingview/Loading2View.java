package cn.ian2018.loadingview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Description: 圆形加载框
 * Author:chenshuai
 * E-mail:chenshuai@amberweather.com
 * Date:2019/3/22
 */
public class Loading2View extends View {

    private Paint mCirclePaint;
    private Paint mLoadingPaint;
    private Paint mLinePaint;

    private int mWidth;
    private int mHeight;
    private RectF mRectF;

    private boolean isFinish = false;
    private boolean isLoading = false;

    // view属性初始值
    private float mStrokeWidth = 10;
    private float mLineWidth = 20;
    private float mCircleRadius = 100;

    // 动画初始值
    private float mStartAngle = 0 - 90;
    private float mSweepAngle = 0;
    private float mRadius = 0;
    private float mLength = 0;

    private ValueAnimator valueAnimator1;
    private ValueAnimator valueAnimator2;
    private ValueAnimator valueAnimator3;
    private ValueAnimator circleValueAnimator;
    private ValueAnimator lineValueAnimator;

    private OnLoadingListener loadingListener;
    private boolean isCancel = true;

    public Loading2View(Context context) {
        super(context);
        init(context, null);
    }

    public Loading2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Loading2View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int circleColor = Color.BLUE;
        int loadingColor = Color.BLUE;
        int lineColor = Color.WHITE;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Loading2View);
            mCircleRadius = typedArray.getDimension(R.styleable.Loading2View_circle_radius, 100);
            mLineWidth = typedArray.getDimension(R.styleable.Loading2View_line_width, 20);
            mStrokeWidth = typedArray.getDimension(R.styleable.Loading2View_stroke, 10);
            circleColor = typedArray.getColor(R.styleable.Loading2View_circle_color, Color.BLUE);
            loadingColor = typedArray.getColor(R.styleable.Loading2View_loading_color, Color.BLUE);
            lineColor = typedArray.getColor(R.styleable.Loading2View_line_color, Color.WHITE);
            typedArray.recycle();
        }

        // 进度
        mLoadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLoadingPaint.setStrokeWidth(mStrokeWidth);
        mLoadingPaint.setStyle(Paint.Style.STROKE);
        mLoadingPaint.setColor(loadingColor);
        mLoadingPaint.setStrokeCap(Paint.Cap.ROUND);

        // 完成时的圆
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(circleColor);

        // 对勾
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(lineColor);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mRectF = new RectF();

        initAnim();
    }

    // 初始化动画
    private void initAnim() {
        // 1. start: 0                 sweep: 0 -> 1/3 * 360
        valueAnimator1 = ValueAnimator.ofFloat(0, 1 / 3f * 360);
        valueAnimator1.setDuration(650);
        valueAnimator1.setInterpolator(new AccelerateInterpolator());
        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSweepAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator1.addListener(new BaseAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 开始第二段动画
                valueAnimator2.start();
            }
        });

        // 2. start: 0 -> 2/3 * 360    sweep: 1/3 * 360
        valueAnimator2 = ValueAnimator.ofFloat(0 - 90, 2 / 3f * 360 - 90);
        valueAnimator2.setDuration(500);
        valueAnimator2.setInterpolator(new LinearInterpolator());
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mStartAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator2.addListener(new BaseAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 开始第三段动画
                valueAnimator3.start();
            }
        });

        // 3. start: 2/3 * 360 -> 360  sweep: 1/3 * 360 -> 0
        valueAnimator3 = ValueAnimator.ofFloat(2 / 3f * 360, 360);
        valueAnimator3.setDuration(380);
        valueAnimator3.setInterpolator(new DecelerateInterpolator());
        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mStartAngle = (float) animation.getAnimatedValue() - 90;
                mSweepAngle = 360 - (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator3.addListener(new BaseAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 开始第四段动画
                // 4. start: 0                 sweep: 0
                mStartAngle = 0 - 90;
                mSweepAngle = 0;
                invalidate();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinish && !isCancel) {
                            valueAnimator1.start();
                        }
                    }
                }, 300);
            }
        });

        // 完成时的圆形动画
        circleValueAnimator = ValueAnimator.ofFloat(0, mCircleRadius * 1.2f);
        circleValueAnimator.setDuration(1000);
        circleValueAnimator.setInterpolator(new OvershootInterpolator());
        circleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRadius = (float) animation.getAnimatedValue();
                if (mRadius > mCircleRadius * 0.9f && mRadius < mCircleRadius) {
                    lineValueAnimator.start();
                }
                invalidate();
            }
        });

        // 画对勾的动画
        lineValueAnimator = ValueAnimator.ofFloat(0, mCircleRadius / 2f * 3.0f);
        lineValueAnimator.setDuration(500);
        lineValueAnimator.setInterpolator(new LinearInterpolator());
        lineValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLength = (float) animation.getAnimatedValue();
            }
        });
        lineValueAnimator.addListener(new BaseAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isLoading = false;
                if (loadingListener != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingListener.onFinish();
                        }
                    }, 400);
                }
            }
        });
    }


    /**
     * 加载完成
     */
    public void finishLoad() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        circleValueAnimator.start();
        valueAnimator1.cancel();
        valueAnimator2.cancel();
        valueAnimator3.cancel();
    }

    /**
     * 开始加载
     */
    public void startLoading() {
        if (isLoading || isFinish) {
            return;
        }
        isCancel = false;
        isLoading = true;
        valueAnimator1.start();
    }

    /**
     * 停止动画
     */
    public void cancel() {
        valueAnimator1.cancel();
        valueAnimator2.cancel();
        valueAnimator3.cancel();
        isCancel = true;
        isLoading = false;
        isFinish = false;
        mRadius = 0;
        mLength = 0;
        mStartAngle = 0 - 90;
        mSweepAngle = 0;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mHeight = getHeight();
        mRectF.set(mWidth / 2f - mCircleRadius + mStrokeWidth / 2,
                mHeight / 2f - mCircleRadius + mStrokeWidth / 2,
                mWidth / 2f + mCircleRadius - mStrokeWidth / 2,
                mHeight / 2f + mCircleRadius - mStrokeWidth / 2);
    }

    /**
     * 当前是否正在加载
     *
     * @return
     */
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isFinish) {
            // 画圆
            canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius, mCirclePaint);
            if (mLength <= mCircleRadius * 0.4f) {
                // 画对勾第一条线
                canvas.drawLine(mWidth / 2f - mCircleRadius / 2f, mHeight / 2f,
                        mWidth / 2f - mCircleRadius / 2f + (float) (mLength * Math.cos(Math.toRadians(38.65))),
                        mHeight / 2f + (float) (mLength * Math.sin(Math.toRadians(38.65))),
                        mLinePaint);
            } else {
                canvas.drawLine(mWidth / 2f - mCircleRadius / 2f, mHeight / 2f,
                        mWidth / 2f,
                        mHeight / 2f + mCircleRadius * 0.4f,
                        mLinePaint);
                // 画对勾第二条线
                canvas.drawLine(mWidth / 2f, mHeight / 2f + mCircleRadius * 0.4f,
                        mWidth / 2f + (float) ((mLength - mCircleRadius * 0.4f) * Math.cos(Math.toRadians(55))),
                        mHeight / 2f + mCircleRadius / 2f - (float) ((mLength - mCircleRadius * 0.4f) * Math.sin(Math.toRadians(55))),
                        mLinePaint);
            }
        } else {
            canvas.drawArc(mRectF, mStartAngle, mSweepAngle, false, mLoadingPaint);
        }
    }

    public void setOnLoadindListener(OnLoadingListener listener) {
        loadingListener = listener;
    }

    public interface OnLoadingListener {
        void onFinish();
    }
}

