package com.forevas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhuchenchen on 2018/3/4.
 * 图片裁剪控件
 */

public class CropImageView extends View {
    public static final int MIN_W = 200;
    public static final int MIN_H = 200;
    public static final int OFFSET = 50;

    Paint mThinPaint, mThickPaint, mShadowPaint;
    int mThinWidth = 5, mThickWidth = 15;
    int mLeft, mRight, mTop, mBottom;
    int mLastLeft, mLastRight, mLastTop, mLastBottom;
    int mBorderLeft, mBorderRight, mBorderTop, mBorderBottom;//边界限制
    Rect[] mRects = new Rect[9];//左上，上，右上，右，右下，下，左下，左,中心区域
    int mCurRect = -1;
    int angle;
    Bitmap srcBitmap;
    int bitmapW, bitmapH;
    Matrix drawMatrixs;
    float scale;//原图的缩放比例
    float dx = 0, dy = 0;//偏移值


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

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.parseColor("#7f000000"));
        mShadowPaint.setStyle(Paint.Style.FILL);

        mRects[0] = new Rect();
        mRects[1] = new Rect();
        mRects[2] = new Rect();
        mRects[3] = new Rect();
        mRects[4] = new Rect();
        mRects[5] = new Rect();
        mRects[6] = new Rect();
        mRects[7] = new Rect();
        mRects[8] = new Rect();

        drawMatrixs = new Matrix();
    }

    public void setImageRes(int id) {
//        srcBitmap = BitmapFactory.decodeResource(getResources(), id);
        srcBitmap = getBitmapFromRes(id, 2048, 2048);
        srcBitmap = resizeBitmap(srcBitmap);
        setBitmap(srcBitmap);
    }

    public void setImagePath(String path) {
        srcBitmap = getBitmapFromFile(path, 2048, 2048);//处理
        srcBitmap = resizeBitmap(srcBitmap);
        setBitmap(srcBitmap);
    }

    public Bitmap getBitmapFromRes(int resId, int width, int height) {
        BitmapFactory.Options opts = null;
        if (width > 0 && height > 0) {
            opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;//设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
            BitmapFactory.decodeResource(getResources(), resId, opts);
            // 计算图片缩放比例
            opts.inSampleSize = computeSampleSize(opts, width * height);
            opts.inJustDecodeBounds = false;//这里一定要将其设置回false，因为之前我们将其设置成了true
            opts.inInputShareable = true;
            opts.inPurgeable = true;
        }
        try {
            return BitmapFactory.decodeResource(getResources(), resId, opts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getBitmapFromFile(String path, int width, int height) {
        BitmapFactory.Options opts = null;
        if (width > 0 && height > 0) {
            opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;//设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
            BitmapFactory.decodeFile(path, opts);
            // 计算图片缩放比例
            opts.inSampleSize = computeSampleSize(opts, width * height);
            opts.inJustDecodeBounds = false;//这里一定要将其设置回false，因为之前我们将其设置成了true
            opts.inInputShareable = true;
            opts.inPurgeable = true;
        }
        try {
            return BitmapFactory.decodeFile(path, opts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;//缩放比例必须是2的指数函数值
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        return lowerBound;
    }

    public Bitmap resizeBitmap(Bitmap bitmap) {
        if (bitmap.getWidth() > 4096 || bitmap.getHeight() > 4096) {
            bitmap = Bitmap.createScaledBitmap(bitmap, srcBitmap.getWidth() / 2, srcBitmap.getHeight() / 2, true);
            return resizeBitmap(bitmap);
        }
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        srcBitmap = bitmap;
        bitmapW = srcBitmap.getWidth();
        bitmapH = srcBitmap.getHeight();
        angle = 0;
        resetMatrix();
        invalidate();
    }

    public void rotate() {
        angle += 90;
        angle %= 360;
        resetMatrix();
        invalidate();
    }

    public Bitmap crop(boolean refresh) {
        int startX = 0, startY = 0, cropWidth = 0, cropHeight = 0;
        if (angle == 0) {
            startX = (int) ((mLeft - dx) / scale);
            startY = (int) ((mTop - dy) / scale);
            cropWidth = (int) ((mRight - mLeft) / scale);
            cropHeight = (int) ((mBottom - mTop) / scale);
        } else if (angle == 90) {
            startX = (int) ((mTop - dy) / scale);
            startY = (int) ((getMeasuredWidth() - mRight - dx) / scale);
            cropWidth = (int) ((mBottom - mTop) / scale);
            cropHeight = (int) ((mRight - mLeft) / scale);
        } else if (angle == 180) {
            startX = (int) ((getMeasuredWidth() - mRight - dx) / scale);
            startY = (int) ((getMeasuredHeight() - mBottom - dy) / scale);
            cropWidth = (int) ((mRight - mLeft) / scale);
            cropHeight = (int) ((mBottom - mTop) / scale);
        } else if (angle == 270) {
            startX = (int) ((mLeft - dx) / scale);
            startY = (int) ((getMeasuredHeight() - mBottom - dy) / scale);
            cropWidth = (int) ((mBottom - mTop) / scale);
            cropHeight = (int) ((mRight - mLeft) / scale);
        }

        if ((startX + cropWidth) > bitmapW) {
            cropWidth = bitmapW - startX;
        }
        if ((startY + cropHeight) > bitmapH) {
            cropHeight = bitmapH - startY;
        }

        Matrix cropMatrix = new Matrix();
        cropMatrix.setRotate(angle);
        Bitmap tempBitmap = Bitmap.createBitmap(srcBitmap, startX, startY, cropWidth, cropHeight, cropMatrix, true);
        if (refresh) {
            if (!srcBitmap.equals(tempBitmap)) {
                srcBitmap.recycle();
            }
            setBitmap(tempBitmap);
        }
        return tempBitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resetMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
        drawShadow(canvas);
        drawNet(canvas);

    }

    private void drawBitmap(Canvas canvas) {
        if (srcBitmap != null) {
            canvas.drawBitmap(srcBitmap, drawMatrixs, null);
        }
    }

    private void drawShadow(Canvas canvas) {
        int i = canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), null, Canvas.ALL_SAVE_FLAG);
        canvas.drawRect(mBorderLeft, mBorderTop, mBorderRight, mBorderBottom, mShadowPaint);
        mShadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(mLeft, mTop, mRight, mBottom, mShadowPaint);
        mShadowPaint.setXfermode(null);
        canvas.restoreToCount(i);
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

        canvas.drawLine(mLeft - mThickWidth / 2, mTop, mLeft + OFFSET * 2, mTop, mThickPaint);
        canvas.drawLine(mRight - OFFSET * 2, mTop, mRight + mThickWidth / 2, mTop, mThickPaint);
        canvas.drawLine(mRight, mTop - mThickWidth / 2, mRight, mTop + OFFSET * 2, mThickPaint);
        canvas.drawLine(mRight, mBottom - OFFSET * 2, mRight, mBottom + mThickWidth / 2, mThickPaint);
        canvas.drawLine(mRight + mThickWidth / 2, mBottom, mRight - OFFSET * 2, mBottom, mThickPaint);
        canvas.drawLine(mLeft - mThickWidth / 2, mBottom, mLeft + OFFSET * 2, mBottom, mThickPaint);
        canvas.drawLine(mLeft, mBottom + mThickWidth / 2, mLeft, mBottom - OFFSET * 2, mThickPaint);
        canvas.drawLine(mLeft, mTop - mThickWidth / 2, mLeft, mTop + OFFSET * 2, mThickPaint);

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
            minJudgement();
            resetLast();
            resetRects();
            invalidate();
        }
    }

    /**
     * 矩阵重置
     */
    private void resetMatrix() {
        if (angle == 0 || angle == 180) {
            if (bitmapW <= getMeasuredWidth() && bitmapH <= getMeasuredHeight()) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) getMeasuredWidth() / (float) bitmapW,
                        (float) getMeasuredHeight() / (float) bitmapH);
            }
            dx = Math.round((getMeasuredWidth() - bitmapW * scale) / 2);
            dy = Math.round((getMeasuredHeight() - bitmapH * scale) / 2);
            drawMatrixs.setRotate(angle, bitmapW / 2, bitmapH / 2);
            drawMatrixs.postScale(scale, scale);
            drawMatrixs.postTranslate(dx, dy);
        } else {
            if (bitmapW <= getMeasuredHeight() && bitmapH <= getMeasuredWidth()) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) getMeasuredWidth() / (float) bitmapH,
                        (float) getMeasuredHeight() / (float) bitmapW);
            }
            dx = Math.round((getMeasuredWidth() - bitmapH * scale) / 2);
            dy = Math.round((getMeasuredHeight() - bitmapW * scale) / 2);
            drawMatrixs.setRotate(angle);
            drawMatrixs.preScale(scale, scale);
            if (angle == 90) {
                drawMatrixs.postTranslate(bitmapH * scale + dx, dy);
            } else {
                drawMatrixs.postTranslate(dx, bitmapW * scale + dy);
            }
        }
        resetBorderWithBitmap();
    }

    /**
     * 以Bitmap为基准重置边界值
     */
    private void resetBorderWithBitmap() {
        mBorderLeft = (int) dx;
        mBorderRight = (int) (getMeasuredWidth() - dx);
        mBorderTop = (int) dy;
        mBorderBottom = (int) (getMeasuredHeight() - dy);
        mLeft = mBorderLeft;
        mRight = mBorderRight;
        mTop = mBorderTop;
        mBottom = mBorderBottom;
        resetLast();
        resetRects();
    }

    /**
     * 保留上一次的边界值
     */
    private void resetLast() {
        mLastLeft = mLeft;
        mLastTop = mTop;
        mLastRight = mRight;
        mLastBottom = mBottom;
    }

    /**
     * 判断是否越界
     */
    private void minJudgement() {
        if (mRight - mLeft < MIN_W) {
            mLeft = mLastLeft;
            mRight = mLastRight;
        }
        if (mBottom - mTop < MIN_H) {
            mTop = mLastTop;
            mBottom = mLastBottom;
        }
        if (srcBitmap != null) {
            if (mCurRect == 8) {
                if (mLeft < mBorderLeft || mRight > mBorderRight) {
                    mLeft = mLastLeft;
                    mRight = mLastRight;
                }
                if (mTop < mBorderTop || mBottom > mBorderBottom) {
                    mTop = mLastTop;
                    mBottom = mLastBottom;
                }
            }
            if (mLeft < mBorderLeft) {
                mLeft = mLastLeft;
            }
            if (mTop < mBorderTop) {
                mTop = mLastTop;
            }
            if (mRight > mBorderRight) {
                mRight = mLastRight;
            }
            if (mBottom > mBorderBottom) {
                mBottom = mLastBottom;
            }
        }
    }

    /**
     * 重置点击区域
     */
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