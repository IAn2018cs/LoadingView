package cn.ian2018.loadingview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingView = findViewById(R.id.loading);

        Button startBt = findViewById(R.id.start);
        Button finishBt = findViewById(R.id.finish);
        Button stophBt = findViewById(R.id.stop);


        stophBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingView.stop();
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
                loadingView.finishLoad();
            }
        });

    }
}
