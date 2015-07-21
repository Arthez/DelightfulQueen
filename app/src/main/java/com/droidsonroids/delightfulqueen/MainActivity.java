package com.droidsonroids.delightfulqueen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.droidsonroids.awesomeprogressbar.AwesomeProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @Bind(R.id.super_awesome_progress_bar)
    AwesomeProgressBar mSuperAwesomeProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_custom_view);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.success_button)
    public void playSuccessAnimation() {
        mSuperAwesomeProgressBar.play(true);
    }

    @OnClick(R.id.failure_button)
    public void playFailureAnimation() {
        mSuperAwesomeProgressBar.play(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
