package cn.ian2018.loadingview;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private Loading2View loadingView;
    private ImageLoadingView loadingView1;
    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private EditText editText5;
    private EditText editText6;
    private EditText editText7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingView = findViewById(R.id.loading);

        Button startBt = findViewById(R.id.start);
        Button finishBt = findViewById(R.id.finish);
        Button stophBt = findViewById(R.id.stop);

        editText1 = findViewById(R.id.edit1);
        editText2 = findViewById(R.id.edit2);
        editText3 = findViewById(R.id.edit3);
        editText4 = findViewById(R.id.edit4);
        editText5 = findViewById(R.id.edit5);
        editText6 = findViewById(R.id.edit6);
        editText7 = findViewById(R.id.edit7);


        stophBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingView.cancel();
            }
        });
        startBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingView.startLoading();
            }
        });
        finishBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trim1 = editText1.getText().toString().trim();
                String trim2 = editText2.getText().toString().trim();
                String trim3 = editText3.getText().toString().trim();
                String trim4 = editText4.getText().toString().trim();
                String trim5 = editText5.getText().toString().trim();
                String trim6 = editText6.getText().toString().trim();
                String trim7 = editText7.getText().toString().trim();
                if (TextUtils.isEmpty(trim1)) {
                    trim1 = "37";
                }
                if (TextUtils.isEmpty(trim2)) {
                    trim2 = "0.5";
                }
                if (TextUtils.isEmpty(trim3)) {
                    trim3 = "0.9";
                }
                if (TextUtils.isEmpty(trim4)) {
                    trim4 = "40";
                }
                if (TextUtils.isEmpty(trim5)) {
                    trim5 = "55";
                }
                if (TextUtils.isEmpty(trim6)) {
                    trim6 = "0";
                }
                if (TextUtils.isEmpty(trim7)) {
                    trim7 = "0";
                }
                loadingView.setFinishCircleRadius(Float.parseFloat(trim1));
                loadingView.setLengthPercent1(Float.parseFloat(trim2));
                loadingView.setLengthPercent2(Float.parseFloat(trim3));
                loadingView.setAngle1(Float.parseFloat(trim4));
                loadingView.setAngle2(Float.parseFloat(trim5));
                loadingView.setTranslateX(Float.parseFloat(trim6));
                loadingView.setTranslateY(Float.parseFloat(trim7));
                loadingView.finishLoad();
            }
        });

        loadingView1 = findViewById(R.id.image);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 60);
        valueAnimator.setDuration(4000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                loadingView1.setCurrent(value, 60);
            }
        });
        valueAnimator.start();

    }
}
