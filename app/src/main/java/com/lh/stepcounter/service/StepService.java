package com.lh.stepcounter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.lh.stepcounter.R;
import com.lh.stepcounter.bean.StepInfo;
import com.lh.stepcounter.bean.StepInfoDao;
import com.lh.stepcounter.database.DaoManager;
import com.lh.stepcounter.utils.DateUtils;
import com.lh.stepcounter.view.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by home on 2017/1/12.
 * 计步服务
 */

public class StepService extends Service {

    private static final int NOTIFICATION_ID = 100;
    private static final int NOTIFICATION_SUMMARY_ID = 101;

    private static final int SAVE_TIME = 2 * 60 * 1000;

    private SensorManager mSensorManager;
    private NotificationManager mNotificationManager;
    private StepListener mStepListener;
    private TimeReceiver mTimeReceiver;

    private Bitmap mNotificationBitmap;

    private long mLastSavedTime;
    private int mUnSavedStepCount;

    private int mTodayStepCount;
    private int mPreStepCount;
    private String mCurrentDate;
    private boolean mShouldShowSummary;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new StepBinder();
    }

    private void init() {
        mNotificationBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        initStepInfo();
        initSensor();
        initBroadCast();
        initForeground();
    }

    private void initBroadCast() {
        mTimeReceiver = new TimeReceiver();
        registerReceiver(mTimeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(mTimeReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
    }

    private void initForeground() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("今日步数：" + mTodayStepCount + "步")
                .setContentText("计步中...")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(mNotificationBitmap)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void initStepInfo() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        mCurrentDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        mShouldShowSummary = getSharedPreferences("step", MODE_PRIVATE).getBoolean("show_summary", true);
        mTodayStepCount = readStepCount();
    }

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepListener = new StepListener();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSensorManager.registerListener(mStepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void showSummary() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("今日已走:" + mTodayStepCount)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(mNotificationBitmap)
                .setAutoCancel(true)
                .build();
        mNotificationManager.notify(NOTIFICATION_SUMMARY_ID, notification);
    }

    private void saveStepInfo() {
        mLastSavedTime = System.currentTimeMillis();
        mUnSavedStepCount = 0;
        StepInfoDao dao = DaoManager.getInstance().getDaoSession().getStepInfoDao();
        List<StepInfo> stepInfos = dao.queryBuilder().where(StepInfoDao.Properties.Date.eq(mCurrentDate)).list();
        if (stepInfos.size() > 0) {
            StepInfo stepInfo = stepInfos.get(0);
            stepInfo.setStep(mTodayStepCount);
            dao.update(stepInfo);
        } else {
            StepInfo stepInfo = new StepInfo();
            stepInfo.setDate(mCurrentDate);
            dao.insert(stepInfo);
        }
    }

    private int readStepCount() {
        StepInfoDao dao = DaoManager.getInstance().getDaoSession().getStepInfoDao();
        List<StepInfo> stepInfos = dao.queryBuilder().where(StepInfoDao.Properties.Date.eq(mCurrentDate)).list();
        if (stepInfos.size() > 0) {
            StepInfo stepInfo = stepInfos.get(0);
            return stepInfo.getStep();
        }
        return 0;
    }

    private void updateNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("今日步数:" + mTodayStepCount + "步")
                .setContentText("计步中...")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(mNotificationBitmap)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveStepInfo();
        stopForeground(true);
        mSensorManager.unregisterListener(mStepListener);
        unregisterReceiver(mTimeReceiver);
        if (mNotificationBitmap != null && !mNotificationBitmap.isRecycled()) {
            mNotificationBitmap.recycle();
        }
    }

    private class StepListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            int currentStep = (int) event.values[0];
            if (mPreStepCount == 0) {
                mPreStepCount = currentStep;
            }
            int increasStep = currentStep - mPreStepCount;
            mUnSavedStepCount += increasStep;
            mTodayStepCount += increasStep;
            mPreStepCount = currentStep;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public Observable<List<StepInfo>> readHistoryStepInfo(final int count) {
        return Observable.create(new ObservableOnSubscribe<List<StepInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<StepInfo>> e) throws Exception {
                List<StepInfo> stepInfos = DaoManager
                        .getInstance()
                        .getDaoSession()
                        .getStepInfoDao()
                        .queryBuilder()
                        .where(StepInfoDao.Properties.Date.notEq(mCurrentDate))
                        .orderAsc(StepInfoDao.Properties.Date)
                        .limit(count - 1)
                        .list();
                stepInfos.add(new StepInfo(0L, mCurrentDate, mTodayStepCount));
                int diff = count - stepInfos.size();
                Date date = DateUtils.str2Date(stepInfos.get(0).getDate());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                for (int i = 0; i < diff; i++) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    stepInfos.add(0, new StepInfo(0L, DateUtils.date2Str(calendar.getTime()), 0));
                }
                e.onNext(stepInfos);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    public class StepBinder extends Binder {
        public int getCurrentStep() {
            updateNotification();
            return mTodayStepCount;
        }

        public String getCurrentTime() {
            return mCurrentDate;
        }

        public void forceSaveStepCount() {
            saveStepInfo();
        }

        public Observable<List<StepInfo>> readHistoryStepInfo(int count) {
            return StepService.this.readHistoryStepInfo(count);
        }
    }

    private class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_DATE_CHANGED:
                    updateNotification();
                    saveStepInfo();
                    initStepInfo();
                    mShouldShowSummary = true;
                    getSharedPreferences("step", MODE_PRIVATE).edit().putBoolean("show_summary", true).apply();
                    break;
                case Intent.ACTION_TIME_TICK:
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if (hour >= 23 && mShouldShowSummary) {
                        mShouldShowSummary = false;
                        getSharedPreferences("step", MODE_PRIVATE).edit().putBoolean("show_summary", mShouldShowSummary).apply();
                        showSummary();
                    }
                    if (mUnSavedStepCount > 0) {
                        updateNotification();
                    }
                    if (mUnSavedStepCount > 100 || System.currentTimeMillis() - mLastSavedTime >= SAVE_TIME) {
                        saveStepInfo();
                    }
                    break;
            }
        }
    }
}
