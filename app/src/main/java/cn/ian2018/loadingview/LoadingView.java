package cn.ian2018.loadingview;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Description: 圆形加载框
 * Author:chenshuai
 * E-mail:chenshuai@amberweather.com
 * Date:2019/3/22
 */
public class LoadingView extends View {

    private Paint mBackgroundPaint;
    private Paint mCurrentPaint;
    private float mCurrent = 0;
    private RectF mRectF;
    private boolean isFinish = false;
    private boolean isLoading = false;
    private ValueAnimator loadingAnimator;
    private ValueAnimator finishAnimator;

    private OnLoadingListener loadingListener;

    // 圆弧的宽度
    private float mStrokeWidth = 20;
    // 开始的角度
    private float mStartAngle = 270;
    // 加载时每次扫过的角度
    private float mLoadingSweepAngle = 45;
    // 加载转一圈的时间
    private long loadingTurnAroundTime = 3000;
    // 完成动画时间
    private long finishTime = 5000;
    // 加载动画插值器
    private TimeInterpolator loadingInterpolator = new LinearInterpolator();
    // 完成动画插值器
    private TimeInterpolator finishInterpolator = new LinearInterpolator();

    public LoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int backgroundColor = Color.GRAY;
        int progressColor = Color.BLUE;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
            mStrokeWidth = typedArray.getDimension(R.styleable.LoadingView_stroke_width, 20);
            backgroundColor = typedArray.getColor(R.styleable.LoadingView_background_color, Color.GRAY);
            progressColor = typedArray.getColor(R.styleable.LoadingView_progress_color, Color.BLUE);
            mStartAngle = typedArray.getFloat(R.styleable.LoadingView_start_angle, 270);
            mLoadingSweepAngle = typedArray.getFloat(R.styleable.LoadingView_loading_sweep_angle, 45);
            loadingTurnAroundTime = typedArray.getInteger(R.styleable.LoadingView_loading_turn_around_time, 3000);
            finishTime = typedArray.getInteger(R.styleable.LoadingView_finish_time, 5000);
            typedArray.recycle();
        }
        // 背景
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStrokeWidth(mStrokeWidth);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // 进度
        mCurrentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurrentPaint.setStrokeWidth(mStrokeWidth);
        mCurrentPaint.setStyle(Paint.Style.STROKE);
        mCurrentPaint.setColor(progressColor);
        mCurrentPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectF = new RectF();

        initAnim();
    }

    // 初始化动画
    private void initAnim() {
        // 加载动画
        loadingAnimator = ValueAnimator.ofFloat(0, 360);
        loadingAnimator.setDuration(loadingTurnAroundTime);
        loadingAnimator.setRepeatMode(ValueAnimator.RESTART);
        loadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        loadingAnimator.setInterpolator(loadingInterpolator);
        loadingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mStartAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        loadingAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isLoading = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // 完成动画
        float startValue = 100 / (360 / mLoadingSweepAngle);
        finishAnimator = ValueAnimator.ofFloat(startValue, 100);
        finishAnimator.setDuration(finishTime);
        finishAnimator.setInterpolator(finishInterpolator);
        finishAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        finishAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isLoading = false;
                isFinish = false;
                if (loadingListener != null) {
                    loadingListener.onFinish();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isLoading = false;
                isFinish = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    public void setmStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
    }

    public void setmStartAngle(float mStartAngle) {
        this.mStartAngle = mStartAngle;
    }

    public void setmLoadingSweepAngle(float mLoadingSweepAngle) {
        this.mLoadingSweepAngle = mLoadingSweepAngle;
    }

    public void setLoadingTurnAroundTime(long loadingTurnAroundTime) {
        this.loadingTurnAroundTime = loadingTurnAroundTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public void setLoadingInterpolator(TimeInterpolator loadingInterpolator) {
        this.loadingInterpolator = loadingInterpolator;
    }

    public void setFinishInterpolator(TimeInterpolator finishInterpolator) {
        this.finishInterpolator = finishInterpolator;
    }

    /**
     * 加载完成
     */
    public void finishLoad() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        loadingAnimator.cancel();
        finishAnimator.start();
    }

    /**
     * 开始加载
     */
    public void startLoading() {
        if (isLoading || isFinish) {
            return;
        }
        isLoading = true;
        loadingAnimator.start();
    }

    /**
     * 停止动画
     */
    public void stop() {
        loadingAnimator.cancel();
        finishAnimator.cancel();
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectF.set(mStrokeWidth / 2, mStrokeWidth / 2, getWidth() - mStrokeWidth / 2, getHeight() - mStrokeWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 画出背景
        canvas.drawArc(mRectF, 0, 360, false, mBackgroundPaint);

        if (isFinish) {
            float sweepAngle = 360 * mCurrent / 100;
            canvas.drawArc(mRectF, mStartAngle, sweepAngle, false, mCurrentPaint);
        } else if (isLoading){
            canvas.drawArc(mRectF, mStartAngle, mLoadingSweepAngle, false, mCurrentPaint);
        } else {
            canvas.drawArc(mRectF, 0, 360, false, mCurrentPaint);
        }
    }

    public void setOnLoadindListener(OnLoadingListener listener) {
        loadingListener = listener;
    }

    public interface OnLoadingListener {
        void onFinish();
    }
}

