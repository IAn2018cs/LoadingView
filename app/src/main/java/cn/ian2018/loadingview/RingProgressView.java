package cn.ian2018.loadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chenshuai on 2019-09-03
 */
public class RingProgressView extends ViewGroup {


    private static final String TAG = "RingProgressView";

    private float maxValue = 100f;

    private Context context;

    private ImageView ringView;
    private TextView progressText;
    private TextView progressValueText;
    private ImageView checkView;

    private int viewWidth;
    private int viewHeight;

    private Paint circlePaint;
    private Paint alphaCirclePaint;

    private AnimatorSet finish1AnimSet;
    private ValueAnimator finish3Animator;
    private AnimatorSet finish2AnimSet;
    private AnimatorSet finish4AnimSet;
    private ValueAnimator finish5valueAnimator;

    private boolean isFinish = false;
    private boolean isInitAnim = false;
    private float mRadius;

    private int progressColor = Color.parseColor("#4061FF");
    private int failColor = Color.parseColor("#D54D4D");
    private int textColor = Color.parseColor("#494A4B");


    public RingProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public RingProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RingProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        setWillNotDraw(false);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView);
            progressColor = typedArray.getColor(R.styleable.RingProgressView_ring_progress_color, progressColor);
            textColor = typedArray.getColor(R.styleable.RingProgressView_text_color, textColor);
            failColor = typedArray.getColor(R.styleable.RingProgressView_fail_color, failColor);
            typedArray.recycle();
        }

        initPaint();

        initView();
    }

    private void initPaint() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(progressColor);

        alphaCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        alphaCirclePaint.setColor(progressColor);
        alphaCirclePaint.setAlpha(255);
    }

    private void initView() {
        ringView = new ImageView(context);
        ringView.setImageResource(R.drawable.ic_ring);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ringView.setImageTintList(ColorStateList.valueOf(progressColor));
        }
        addView(ringView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        progressText = new TextView(context);
        progressText.setText("%");
        progressText.setTextColor(textColor);
        progressText.setVisibility(GONE);
        addView(progressText);

        progressValueText = new TextView(context);
        progressValueText.setText("0");
        progressValueText.setTextColor(textColor);
        progressValueText.setVisibility(GONE);
        addView(progressValueText);

        checkView = new ImageView(context);
        checkView.setImageResource(R.drawable.ic_check);
        checkView.setAlpha(0f);
        addView(checkView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private void initAnim() {
        if (isInitAnim) {
            return;
        }

        // 外圈旋转动画
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(ringView, "rotation", 360f, 0f);
        rotationAnimator.setDuration(700);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotationAnimator.start();

        // 结束动画1 外圈缩小加变透明
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(ringView, "scaleX", 1.0f, 0.48f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(ringView, "scaleY", 1.0f, 0.48f);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(ringView, "alpha", 1.0f, 0.2f);
        finish1AnimSet = new AnimatorSet();
        finish1AnimSet.setDuration(500);
        finish1AnimSet.playTogether(animatorX, animatorY, alphaAnim);
        finish1AnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ringView.setVisibility(GONE);
                finish3Animator.start();
            }
        });

        // 结束动画2 文字变透明
        ObjectAnimator textAlphaAnim1 = ObjectAnimator.ofFloat(progressText, "alpha", 1.0f, 0.0f);
        ObjectAnimator textAlphaAnim2 = ObjectAnimator.ofFloat(progressValueText, "alpha", 1.0f, 0.0f);
        finish2AnimSet = new AnimatorSet();
        finish2AnimSet.setDuration(400);
        finish2AnimSet.playTogether(textAlphaAnim1, textAlphaAnim2);

        // 结束动画3 弹出背景圆圈
        finish3Animator = ValueAnimator.ofFloat(0, viewWidth * 0.95f / 2f);
        finish3Animator.setDuration(300);
        finish3Animator.setInterpolator(new LinearInterpolator());
        finish3Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRadius = (float) animation.getAnimatedValue();
                invalidate();

                if (mRadius > viewWidth * 0.7f / 2f && !finish4AnimSet.isRunning()) {
                    finish4AnimSet.start();
                    finish5valueAnimator.start();
                }
            }
        });

        // 结束动画4 对勾放大加逐渐显示
        ObjectAnimator checkViewAnimatorX = ObjectAnimator.ofFloat(checkView, "scaleX", 0.0f, 1.0f);
        ObjectAnimator checkViewAnimatorY = ObjectAnimator.ofFloat(checkView, "scaleY", 0.0f, 1.0f);
        ObjectAnimator checkViewAlphaAnim = ObjectAnimator.ofFloat(checkView, "alpha", 0.0f, 1.0f);
        finish4AnimSet = new AnimatorSet();
        finish4AnimSet.setDuration(200);
        finish4AnimSet.setInterpolator(new AccelerateInterpolator());
        finish4AnimSet.playTogether(checkViewAnimatorX, checkViewAnimatorY, checkViewAlphaAnim);

        // 结束动画5 背景外圈逐渐透明
        finish5valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        finish5valueAnimator.setDuration(500);
        finish5valueAnimator.setInterpolator(new AccelerateInterpolator());
        finish5valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                alphaCirclePaint.setAlpha((int) (255 * v));
                invalidate();
            }
        });

        isInitAnim = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        measureChild(ringView, widthMeasureSpec, heightMeasureSpec);

        measureChild(progressText, widthMeasureSpec, heightMeasureSpec);
        measureChild(progressValueText, widthMeasureSpec, heightMeasureSpec);
        adjustTvTextSize(viewHeight * 0.38f);

        int checkViewW = MeasureSpec.makeMeasureSpec((int) (viewWidth * 0.35f), MeasureSpec.EXACTLY);
        int checkViewH = MeasureSpec.makeMeasureSpec((int) (viewHeight * 0.35f), MeasureSpec.EXACTLY);
        measureChild(checkView, checkViewW, checkViewH);

        initAnim();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 摆放旋转图片在中间
        int ringViewWidth = ringView.getMeasuredWidth();
        int ringViewHeight = ringView.getMeasuredHeight();
        int ringL = (int) ((viewWidth - ringViewWidth) / 2f);
        int ringT = (int) ((viewHeight - ringViewHeight) / 2f);
        int ringR = ringL + ringViewWidth;
        int ringB = ringT + ringViewHeight;
        ringView.layout(ringL, ringT, ringR, ringB);

        // 摆放%的位置
        int progressTextWidth = progressText.getMeasuredWidth();
        int progressTextHeight = progressText.getMeasuredHeight();
        int progressTextL = (int) (viewWidth * 0.84f - progressTextWidth);
        int progressTextT = (int) ((viewHeight - progressTextHeight) / 2f);
        int progressTextR = progressTextL + progressTextWidth;
        int progressTextB = progressTextT + progressTextHeight;
        progressText.layout(progressTextL, progressTextT, progressTextR, progressTextB);

        // 摆放进度值的位置
        int progressValueTextWidth = progressValueText.getMeasuredWidth();
        int progressValueTextHeight = progressValueText.getMeasuredHeight();
        int progressValueTextL = (int) (progressTextL * 0.95f - progressValueTextWidth);
        int progressValueTextT = (int) ((viewHeight - progressTextHeight) / 2f);
        int progressValueTextR = progressValueTextL + progressValueTextWidth;
        int progressValueTextB = progressValueTextT + progressValueTextHeight;
        progressValueText.layout(progressValueTextL, progressValueTextT, progressValueTextR, progressValueTextB);

        // 摆放对勾的位置
        int checkViewWidth = checkView.getMeasuredWidth();
        int checkViewHeight = checkView.getMeasuredHeight();
        int checkViewL = (int) ((viewWidth - checkViewWidth) / 2f);
        int checkViewT = (int) ((viewHeight - checkViewHeight) / 2f);
        int checkViewR = checkViewL + checkViewWidth;
        int checkViewB = checkViewT + checkViewHeight;
        checkView.layout(checkViewL, checkViewT, checkViewR, checkViewB);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFinish) {
            canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, mRadius * 0.8f, circlePaint);
            canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, mRadius, alphaCirclePaint);
        }
    }

    /**
     * 设置最大值
     *
     * @param max
     */
    public void setMaxValue(float max) {
        maxValue = max;
    }

    /**
     * 设置进度
     *
     * @param progress
     */
    public void setProgress(float progress) {
        String progressText = String.valueOf((int) (progress / maxValue * 100));
        progressValueText.setText(progressText);
        progressValueText.requestLayout();
        if (progress >= maxValue) {
            startFinishAnim();
        }
    }

    /**
     * 开始检测动画
     */
    public void startCheckAnim() {
        progressText.setVisibility(GONE);
        progressValueText.setVisibility(GONE);
        restore();
    }

    /**
     * 开始下载动画
     */
    public void startDownloadAnim() {
        progressText.setVisibility(VISIBLE);
        progressValueText.setVisibility(VISIBLE);
        restore();
    }

    /**
     * 完成的动画
     */
    public void startFinishAnim() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        circlePaint.setColor(progressColor);
        alphaCirclePaint.setColor(progressColor);
        checkView.setImageResource(R.drawable.ic_check);
        finish1AnimSet.start();
        finish2AnimSet.start();
    }

    /**
     * TODO 失败动画
     */
    public void startFailAnim() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        circlePaint.setColor(failColor);
        alphaCirclePaint.setColor(failColor);
        checkView.setImageResource(R.drawable.ic_check);
        finish1AnimSet.start();
        finish2AnimSet.start();
    }

    // 重置状态
    private void restore() {
        isFinish = false;
        mRadius = 0f;
        ringView.setScaleX(1f);
        ringView.setScaleY(1f);
        ringView.setAlpha(1f);
        ringView.setVisibility(VISIBLE);
        progressText.setAlpha(1f);
        progressValueText.setAlpha(1f);
        checkView.setScaleX(0f);
        checkView.setScaleY(0f);
        checkView.setAlpha(0f);
        alphaCirclePaint.setAlpha(255);
        setProgress(0);
        invalidate();
    }

    // 动态修改textview大小
    private void adjustTvTextSize(float height) {
        int trySize = 0;
        while (Math.abs(progressText.getMeasuredHeight() - height) > 10) {
            progressText.setTextSize(dp2px(trySize));
            progressValueText.setTextSize(dp2px(trySize));
            int ringW = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
            int ringH = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY);
            measureChild(progressText, ringW, ringH);
            trySize++;
        }
    }

    private int dp2px(float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
