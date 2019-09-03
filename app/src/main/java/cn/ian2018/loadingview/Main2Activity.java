package cn.ian2018.loadingview;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class Main2Activity extends AppCompatActivity {

    private RingProgressView progressView;
    private ValueAnimator valueAnimator1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        progressView = findViewById(R.id.progress_view);

        valueAnimator1 = ValueAnimator.ofFloat(0, 100);
        valueAnimator1.setDuration(2000);
        valueAnimator1.setInterpolator(new LinearInterpolator());
        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                progressView.setProgress(v);
            }
        });

        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.setMaxValue(100);
                if (!valueAnimator1.isRunning()) {
                    progressView.restore();
                    valueAnimator1.start();
                }
            }
        });
    }
}
