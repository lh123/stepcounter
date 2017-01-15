package com.lh.stepcounter.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.lh.stepcounter.R;
import com.lh.stepcounter.bean.StepInfo;
import com.lh.stepcounter.bean.StepInfoDao;
import com.lh.stepcounter.database.DaoManager;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by home on 2017/1/13.
 */

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        initToolbar();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        loadHistoryStep();
    }

    private void initToolbar() {
        mToolbar.setTitle("历史记录");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadHistoryStep() {
        Observable
                .create(new ObservableOnSubscribe<List<StepInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<StepInfo>> e) throws Exception {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
                        List<StepInfo> stepInfos = DaoManager
                                .getInstance()
                                .getDaoSession()
                                .getStepInfoDao()
                                .queryBuilder()
                                .where(StepInfoDao.Properties.Date.notEq(time))
                                .list();
                        Collections.reverse(stepInfos);
                        e.onNext(stepInfos);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<StepInfo>, ObservableSource<List<StepInfo>>>() {
                    @Override
                    public ObservableSource<List<StepInfo>> apply(List<StepInfo> stepInfos) throws Exception {
                        if (stepInfos.size()>0){
                            mEmptyView.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            return Observable.just(stepInfos);
                        }else {
                            mEmptyView.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                            return Observable.empty();
                        }
                    }
                })
                .subscribe(new Consumer<List<StepInfo>>() {
                    @Override
                    public void accept(List<StepInfo> stepInfos) throws Exception {
                        StepAdapter adapter = new StepAdapter(stepInfos);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
    }

}
