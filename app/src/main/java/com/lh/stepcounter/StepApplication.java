package com.lh.stepcounter;

import android.app.Application;

import com.lh.stepcounter.database.DaoManager;

/**
 * Created by home on 2017/1/12.
 */

public class StepApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DaoManager.init(this);
    }
}
