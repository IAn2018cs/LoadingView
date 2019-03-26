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
import android.util.Log;
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

    private Context mContext;
    private float finishCircleRadius;

    private float lengthPercent1 = 0.5f;
    private float lengthPercent2 = 0.9f;
    private float angle1 = 40;
    private float angle2 = 55;
    private float translateX = 0;
    private float translateY = 0;

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
        mContext = context;
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

        finishCircleRadius = dp2px(mContext, 37f);

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

    public void setTranslateX(float translateX) {
        this.translateX = dp2px(mContext, translateX);
    }

    public void setTranslateY(float translateY) {
        this.translateY = dp2px(mContext, translateY);
    }

    public void setFinishCircleRadius(float finishCircleRadius) {
        this.finishCircleRadius = dp2px(mContext, finishCircleRadius);
    }

    public void setLengthPercent1(float lengthPercent1) {
        this.lengthPercent1 = lengthPercent1;
    }

    public void setLengthPercent2(float lengthPercent2) {
        this.lengthPercent2 = lengthPercent2;
    }

    public void setAngle1(float angle1) {
        this.angle1 = angle1;
    }

    public void setAngle2(float angle2) {
        this.angle2 = angle2;
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
        circleValueAnimator = ValueAnimator.ofFloat(0, finishCircleRadius);
        circleValueAnimator.setDuration(900);
        circleValueAnimator.setInterpolator(new OvershootInterpolator());
        circleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                long currentPlayTime = animation.getCurrentPlayTime();
                Log.d("CHEN", "onAnimationUpdate() returned: " + currentPlayTime);
                mRadius = (float) animation.getAnimatedValue();
                if (currentPlayTime > 400 && currentPlayTime < 420 && !lineValueAnimator.isRunning()) {
                    lineValueAnimator.start();
                }
                invalidate();
            }
        });
        circleValueAnimator.addListener(new BaseAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (loadingListener != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isLoading = false;
                            loadingListener.onFinish();
                        }
                    }, 600);
                }
            }
        });

        // 画对勾的动画
        lineValueAnimator = ValueAnimator.ofFloat(0, finishCircleRadius * (lengthPercent1 + lengthPercent2));
        lineValueAnimator.setDuration(500);
        lineValueAnimator.setInterpolator(new LinearInterpolator());
        lineValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLength = (float) animation.getAnimatedValue();
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
        isCancel = true;
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
            canvas.translate(translateX, translateY);
            if (mLength <= finishCircleRadius * lengthPercent1) {
                // 画对勾第一条线
                canvas.drawLine(mWidth / 2f - finishCircleRadius * lengthPercent1 * (float) Math.cos(Math.toRadians(angle1)), mHeight / 2f,
                        mWidth / 2f - finishCircleRadius * lengthPercent1 * (float) Math.cos(Math.toRadians(angle1)) + (float) (mLength * Math.cos(Math.toRadians(angle1))),
                        mHeight / 2f + (float) (mLength * Math.sin(Math.toRadians(angle1))),
                        mLinePaint);
            } else {
                canvas.drawLine(mWidth / 2f - finishCircleRadius * lengthPercent1 * (float) Math.cos(Math.toRadians(angle1)), mHeight / 2f,
                        mWidth / 2f,
                        mHeight / 2f + finishCircleRadius * lengthPercent1 * (float) Math.sin(Math.toRadians(angle1)),
                        mLinePaint);
                // 画对勾第二条线
                canvas.drawLine(mWidth / 2f, mHeight / 2f + finishCircleRadius * lengthPercent1 * (float) Math.sin(Math.toRadians(angle1)),
                        mWidth / 2f + (float) ((mLength - finishCircleRadius * lengthPercent1) * Math.cos(Math.toRadians(angle2))),
                        mHeight / 2f + finishCircleRadius * lengthPercent1 * (float) Math.sin(Math.toRadians(angle1)) - (float) ((mLength - finishCircleRadius * lengthPercent1) * Math.sin(Math.toRadians(angle2))),
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

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

