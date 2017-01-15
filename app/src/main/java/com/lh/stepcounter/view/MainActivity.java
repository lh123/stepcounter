package com.lh.stepcounter.view;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lh.stepcounter.R;
import com.lh.stepcounter.bean.StepInfo;
import com.lh.stepcounter.service.StepService;
import com.lh.stepcounter.widget.LineData;
import com.lh.stepcounter.widget.LineView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private Toolbar mToolbar;
    private View mSyncView;

    private TextView mStepView;
    private TextView mStepTime;
    private LineView mLineView;
    private TextView mDistance;
    private TextView mCalorie;

    private List<LineData> mLineStepData;
    private List<StepInfo> mRawStepData;

    private StepService.StepBinder mStepService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mStepView = (TextView) findViewById(R.id.step_count);
        mStepTime = (TextView) findViewById(R.id.time);
        mDistance = (TextView) findViewById(R.id.distance);
        mCalorie = (TextView) findViewById(R.id.calorie);
        mLineView = (LineView) findViewById(R.id.line_view);
        mStepView.setText("获取中");
        initToolbar();
        initService();
        initLineView();
        loadHistoryStep();
    }

    private void initLineView() {
        mLineView.setOnPointClickListener(new LineView.OnPointClickListener() {
            @Override
            public void onPointClick(int index, float value) {
                setStepInfo((int) value,true);
                mStepTime.setText(mRawStepData.get(index).getDate());
            }
        });
    }

    private void initToolbar() {
        mToolbar.setTitle(R.string.app_name);
        mToolbar.inflateMenu(R.menu.main_menu);
        mSyncView = mToolbar.getMenu().findItem(R.id.sync).getActionView();
        mSyncView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSyncView.animate().rotation(360).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mSyncView.setRotation(0);
                    }
                });
                if (mStepService!=null){
                    setStepInfo(mStepService.getCurrentStep(),true);
                    mStepTime.setText(mStepService.getCurrentTime());
                    loadHistoryStep();
                }
            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.history) {
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    private void initService(){
        Intent intent = new Intent(this, StepService.class);
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    private void loadHistoryStep() {
        if (mStepService == null){
            return;
        }
        mStepService.readHistoryStepInfo(7)
                .flatMap(new Function<List<StepInfo>, ObservableSource<StepInfo>>() {
                    @Override
                    public ObservableSource<StepInfo> apply(List<StepInfo> stepInfos) throws Exception {
                        mRawStepData = stepInfos;
                        return Observable.fromIterable(stepInfos);
                    }
                })
                .map(new Function<StepInfo, LineData>() {
                    @Override
                    public LineData apply(StepInfo stepInfo) throws Exception {
                        String[] strs = stepInfo.getDate().split("-");
                        return new LineData(strs[strs.length - 1],stepInfo.getStep());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LineData>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mLineStepData = new ArrayList<>();
                    }

                    @Override
                    public void onNext(LineData data) {
                        mLineStepData.add(data);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        mLineView.setDatas(mLineStepData);
                    }
                });
    }

    private void setStepInfo(int step, boolean animate) {
        if (animate) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, step);
            valueAnimator.setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mStepView.setText(String.format(Locale.getDefault(), "%d", value));

                }
            });
            valueAnimator.start();
        } else {
            mStepView.setText(String.format(Locale.getDefault(), "%d", step));
        }
        mDistance.setText(String.format(Locale.getDefault(), "距离\n%.1f公里", 0.0006f * step));
        mCalorie.setText(String.format(Locale.getDefault(), "热量\n%.1f千卡", 0.05f * step));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStepService.forceSaveStepCount();
        mStepService = null;
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mStepService = (StepService.StepBinder) service;
        setStepInfo(mStepService.getCurrentStep(), true);
        mStepTime.setText(mStepService.getCurrentTime());
        loadHistoryStep();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
