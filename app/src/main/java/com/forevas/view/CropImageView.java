package com.forevas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhuchenchen on 2018/3/4.
 */

public class CropImageView extends View {
    public static final int MIN_W = 100;
    public static final int MIN_H = 100;
    public static final int OFFSET = 50;

    Paint mThinPaint, mThickPaint;
    int mThinWidth = 5, mThickWidth = 15;
    int mLeft, mRight, mTop, mBottom;
    Rect[] mRects = new Rect[9];//左上，上，右上，右，右下，下，左下，左,中心区域
    int mCurRect = -1;

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mThinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThinPaint.setColor(Color.parseColor("#ffffff"));
        mThinPaint.setStrokeWidth(mThinWidth);

        mThickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThickPaint.setColor(Color.parseColor("#ffffff"));
        mThickPaint.setStrokeWidth(mThickWidth);

        mRects[0] = new Rect();
        mRects[1] = new Rect();
        mRects[2] = new Rect();
        mRects[3] = new Rect();
        mRects[4] = new Rect();
        mRects[5] = new Rect();
        mRects[6] = new Rect();
        mRects[7] = new Rect();
        mRects[8] = new Rect();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = 50;
        mTop = 50;
        mRight = getMeasuredWidth() - 50;
        mBottom = getMeasuredHeight() - 50;
        resetRects();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNet(canvas);

    }

    private void drawNet(Canvas canvas) {
        canvas.drawLine(mLeft, mTop, mLeft, mBottom, mThinPaint);
        canvas.drawLine(mRight, mTop, mRight, mBottom, mThinPaint);
        canvas.drawLine(mLeft, mTop, mRight, mTop, mThinPaint);
        canvas.drawLine(mLeft, mBottom, mRight, mBottom, mThinPaint);

        canvas.drawLine(mLeft, mTop + (mBottom - mTop) / 3, mRight, mTop + (mBottom - mTop) / 3, mThinPaint);
        canvas.drawLine(mLeft, mTop + 2 * (mBottom - mTop) / 3, mRight, mTop + 2 * (mBottom - mTop) / 3, mThinPaint);
        canvas.drawLine(mLeft + (mRight - mLeft) / 3, mTop, mLeft + (mRight - mLeft) / 3, mBottom, mThinPaint);
        canvas.drawLine(mLeft + 2 * (mRight - mLeft) / 3, mTop, mLeft + 2 * (mRight - mLeft) / 3, mBottom, mThinPaint);

        canvas.drawLine(mLeft - mThickWidth / 2, mTop, mLeft + OFFSET, mTop, mThickPaint);
        canvas.drawLine(mRight - OFFSET, mTop, mRight + mThickWidth / 2, mTop, mThickPaint);
        canvas.drawLine(mRight, mTop - mThickWidth / 2, mRight, mTop + OFFSET, mThickPaint);
        canvas.drawLine(mRight, mBottom - OFFSET, mRight, mBottom + mThickWidth / 2, mThickPaint);
        canvas.drawLine(mRight + mThickWidth / 2, mBottom, mRight - OFFSET, mBottom, mThickPaint);
        canvas.drawLine(mLeft - mThickWidth / 2, mBottom, mLeft + OFFSET, mBottom, mThickPaint);
        canvas.drawLine(mLeft, mBottom + mThickWidth / 2, mLeft, mBottom - OFFSET, mThickPaint);
        canvas.drawLine(mLeft, mTop - mThickWidth / 2, mLeft, mTop + OFFSET, mThickPaint);

    }

    int lastX, lastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int curX = (int) event.getX();
        int curY = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurRect = handleDownEvent(curX, curY);
                if (mCurRect == -1) {
                    return false;
                }
                lastX = curX;
                lastY = curY;
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(curX, curY);
                lastX = curX;
                lastY = curY;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private int handleDownEvent(int x, int y) {
        for (int i = 0; i < mRects.length; i++) {
            if (mRects[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void handleMoveEvent(int x, int y) {
        if (mCurRect != -1) {
            switch (mCurRect) {
                case 0:
                    mLeft += (x - lastX);
                    mTop += (y - lastY);
                    break;
                case 1:
                    mTop += (y - lastY);
                    break;
                case 2:
                    mRight += (x - lastX);
                    mTop += (y - lastY);
                    break;
                case 3:
                    mRight += (x - lastX);
                    break;
                case 4:
                    mRight += (x - lastX);
                    mBottom += (y - lastY);
                    break;
                case 5:
                    mBottom += (y - lastY);
                    break;
                case 6:
                    mLeft += (x - lastX);
                    mBottom += (y - lastY);
                    break;
                case 7:
                    mLeft += (x - lastX);
                    break;
                case 8:
                    mLeft += (x - lastX);
                    mRight += (x - lastX);
                    mTop += (y - lastY);
                    mBottom += (y - lastY);
                    break;
            }
            resetRects();
            invalidate();
        }

    }

    private void resetRects() {
        mRects[0].set(mLeft - OFFSET, mTop - OFFSET, mLeft + OFFSET, mTop + OFFSET);
        mRects[1].set(mLeft + OFFSET, mTop - OFFSET, mRight - OFFSET, mTop + OFFSET);
        mRects[2].set(mRight - OFFSET, mTop - OFFSET, mRight + OFFSET, mTop + OFFSET);
        mRects[3].set(mRight - OFFSET, mTop + OFFSET, mRight + OFFSET, mBottom - OFFSET);
        mRects[4].set(mRight - OFFSET, mBottom - OFFSET, mRight + OFFSET, mBottom + OFFSET);
        mRects[5].set(mLeft + OFFSET, mBottom - OFFSET, mRight - OFFSET, mBottom + OFFSET);
        mRects[6].set(mLeft - OFFSET, mBottom - OFFSET, mLeft + OFFSET, mBottom + OFFSET);
        mRects[7].set(mLeft - OFFSET, mTop + OFFSET, mLeft + OFFSET, mBottom - OFFSET);
        mRects[8].set(mLeft + OFFSET, mTop + OFFSET, mRight - OFFSET, mBottom - OFFSET);
    }
}
