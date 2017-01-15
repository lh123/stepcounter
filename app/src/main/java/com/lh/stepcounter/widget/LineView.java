package com.lh.stepcounter.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.lh.stepcounter.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by home on 2017/1/13.
 * 折线图
 */

public class LineView extends View {

    private Paint mBackgroundPaint;
    private Paint mLinePaint;
    private Paint mPointPaint;
    private Paint mAxisTextPaint;

    private List<LineData> mDatas;

    private PathMeasure mPathMeasure;
    private Path mAnimateLinePath;

    private float mLineAnimatePercent;

    private float mMaxValue;
    private float mMinValue;

    private int mSelectIndex;
    private float mSelectAnimatePercent;

    private TextPaint.FontMetrics mFontMetrics;

    private int mHorizontalOffset = dp2px(15);
    private int mColumnCount = 7;

    private int mPrimaryColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
    private int mGrayColor = Color.rgb(194, 200, 208);

    private OnPointClickListener mOnPointClickListener;

    public LineView(Context context) {
        super(context);
        initPaint();
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mGrayColor);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(mPrimaryColor);
        mPointPaint.setTextAlign(Paint.Align.CENTER);
        mPointPaint.setTextSize(dp2px(12));

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mPrimaryColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(dp2px(2));

        mAxisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAxisTextPaint.setColor(mGrayColor);
        mAxisTextPaint.setTextAlign(Paint.Align.CENTER);
        mAxisTextPaint.setTextSize(dp2px(15));
        mFontMetrics = new Paint.FontMetrics();
    }

    public void setOnPointClickListener(OnPointClickListener listener) {
        this.mOnPointClickListener = listener;
    }

    public void setDatas(List<LineData> datas) {
        this.mDatas = datas;
        mSelectIndex = -1;
        calculateData();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLineAnimatePercent = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSelectIndex = mDatas.size() - 1;
                startSelectAnimate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
        postInvalidate();
    }

    private void calculateData() {
        if (mDatas == null) {
            return;
        }
        Path linePath = new Path();
        mAnimateLinePath = new Path();
        mMaxValue = getMaxValue();
        mMinValue = getMinValue();
        for (int i = 0; i < mDatas.size(); i++) {
            if (i >= mColumnCount) {
                break;
            }
            float value = mDatas.get(i).getValue();
            float pX = getPointX(i);
            float pY = getPointY(value);
            if (i == 0) {
                linePath.moveTo(pX, pY);
            } else {
                linePath.lineTo(pX, pY);
            }
        }
        mPathMeasure = new PathMeasure(linePath, false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDatas == null) {
            return;
        }
        drawBackgroundLine(canvas);
        drawXAxisText(canvas);
        drawLine(canvas);
        drawPoint(canvas);
        drawSelectLineAnimate(canvas);
    }

    private void drawBackgroundLine(Canvas canvas) {
        float startY = getHeight() * 0.1f;
        float stopY = getHeight() * 0.9f;
        for (int i = 0; i < mColumnCount; i++) {
            float startX = (getWidth() - mHorizontalOffset * 2) / (mColumnCount - 1) * i + mHorizontalOffset;
            canvas.drawLine(startX, startY, startX, stopY, mBackgroundPaint);
        }
        canvas.drawLine(mHorizontalOffset, getHeight() * 0.1f, getWidth() - mHorizontalOffset, getHeight() * 0.1f, mBackgroundPaint);
        canvas.drawLine(mHorizontalOffset, getHeight() * 0.9f, getWidth() - mHorizontalOffset, getHeight() * 0.9f, mBackgroundPaint);
    }

    private void drawXAxisText(Canvas canvas) {
        for (int i = 0; i < mDatas.size(); i++) {
            if (i >= mColumnCount) {
                break;
            }
            String txt = mDatas.get(i).getIndex();
            mAxisTextPaint.getFontMetrics(mFontMetrics);
            float x = (getWidth() - mHorizontalOffset * 2) / (mColumnCount - 1) * i + mHorizontalOffset;
            if (i == mSelectIndex) {
                mAxisTextPaint.setColor(mPrimaryColor);
            } else {
                mAxisTextPaint.setColor(mGrayColor);
            }
            canvas.drawText(txt, x, getHeight() * 0.9f - mFontMetrics.ascent, mAxisTextPaint);
        }
    }

    private void drawPoint(Canvas canvas) {
        mPointPaint.getFontMetrics(mFontMetrics);
        for (int i = 0; i < mDatas.size(); i++) {
            if (i >= mColumnCount) {
                break;
            }
            float value = mDatas.get(i).getValue();
            float pX = getPointX(i);
            float pY = getPointY(value);
            canvas.drawCircle(pX, pY, dp2px(5), mPointPaint);
            if (i != mSelectIndex) {
                mPointPaint.setColor(Color.WHITE);
                canvas.drawCircle(pX, pY, dp2px(2), mPointPaint);
                mPointPaint.setColor(mPrimaryColor);
            }
            if (value == mMaxValue || value == mMinValue) {
                canvas.drawText(String.format(Locale.getDefault(), "%.0f", value), pX, pY - dp2px(6), mPointPaint);
            }
        }
    }

    private void drawLine(Canvas canvas) {
        mAnimateLinePath.reset();
        mPathMeasure.getSegment(0, mLineAnimatePercent * mPathMeasure.getLength(), mAnimateLinePath, true);
        canvas.drawPath(mAnimateLinePath, mLinePaint);
    }

    private void drawSelectLineAnimate(Canvas canvas) {
        if (mSelectIndex < 0) {
            return;
        }
        float selectX = getPointX(mSelectIndex);
        float selectY = getPointY(mDatas.get(mSelectIndex).getValue());
        float length = getHeight() * 0.9f - selectY;
        canvas.drawLine(selectX, selectY, selectX, mSelectAnimatePercent * length + selectY, mLinePaint);
        String value = String.format(Locale.getDefault(), "%.0f", mDatas.get(mSelectIndex).getValue());
        float width = mPointPaint.measureText(value);
        mPointPaint.getFontMetrics(mFontMetrics);
        mPointPaint.setColor(Color.WHITE);
        canvas.drawRect(selectX - width / 2, getHeight() * 0.9f - mFontMetrics.bottom + mFontMetrics.ascent, selectX + width / 2, getHeight() * 0.9f - mFontMetrics.descent, mPointPaint);
        mPointPaint.setColor(mPrimaryColor);
        if (length * mSelectAnimatePercent + selectY > getHeight() * 0.8f) {
            canvas.drawText(value, selectX, getHeight() * 0.9f + (mFontMetrics.ascent + mFontMetrics.descent) / 2, mPointPaint);
        }
    }

    private void startSelectAnimate() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSelectAnimatePercent = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }

    private float getMaxValue() {
        float max = mDatas.get(0).getValue();
        for (int i = 0; i < mDatas.size(); i++) {
            float value = mDatas.get(i).getValue();
            if (max < value) {
                max = value;
            }
        }
        return max;
    }

    private float getMinValue() {
        float min = mDatas.get(0).getValue();
        for (int i = 0; i < mDatas.size(); i++) {
            float value = mDatas.get(i).getValue();
            if (min > value) {
                min = value;
            }
        }
        return min;
    }

    private float getPointX(float index) {
        return (getWidth() - mHorizontalOffset * 2) / (mColumnCount - 1) * index + mHorizontalOffset;
    }

    private float getPointY(float value) {
        if (mMaxValue == mMinValue) {
            return getHeight() * 0.8f;
        } else {
            return getHeight() * 0.8f - (value - mMinValue) * getHeight() * 0.6f / (mMaxValue - mMinValue);
        }
    }

    private float mLastX;
    private float mLastY;
    private boolean mIsDrag;
    private int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mIsDrag = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (!mIsDrag && (Math.abs(x - mLastX) > mTouchSlop || Math.abs(y - mLastY) > mTouchSlop)) {
                    mIsDrag = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mDatas == null || mIsDrag) {
                    break;
                }
                for (int i = 0; i < mDatas.size(); i++) {
                    float value = mDatas.get(i).getValue();
                    float pX = getPointX(i);
                    float pY = getPointY(value);
                    if (Math.abs(mLastX - pX) < dp2px(20) && Math.abs(mLastY - pY) < dp2px(20)) {
                        mSelectIndex = i;
                        startSelectAnimate();
                        if (mOnPointClickListener != null) {
                            mOnPointClickListener.onPointClick(mSelectIndex, value);
                        }
                    }
                }
                break;
        }
        return true;
    }

    private int dp2px(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    public interface OnPointClickListener {
        void onPointClick(int index, float value);
    }
}
