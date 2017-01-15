package com.lh.stepcounter.database;

import android.content.Context;

import com.lh.stepcounter.bean.DaoMaster;
import com.lh.stepcounter.bean.DaoSession;

import java.lang.ref.WeakReference;

/**
 * Created by home on 2017/1/12.
 */

public class DaoManager {
    private static DaoManager INSTANCE;

    private static WeakReference<Context> mContext;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    public static void init(Context context){
        mContext = new WeakReference<Context>(context.getApplicationContext());
    }

    public static DaoManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new DaoManager();
        }
        return INSTANCE;
    }

    private DaoManager(){
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(mContext.get(),"users-db", null);
        mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoMaster getDaoMaster() {
        return mDaoMaster;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
