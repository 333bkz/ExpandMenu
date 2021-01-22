package com.bkz.menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public final class ExpandMenu extends View {

    private int mWidth, mHeight;
    private int mIconRes;
    private Bitmap mIcon;
    private int mRadius;
    private int mIconRadius;
    private int mPadding = mIconRadius - mRadius;
    private boolean mIsExpand;
    private boolean mIsProgressing;
    private boolean mIsRotating;
    private String mText;
    private int mTextSize;
    private int mTextColor;
    private int mBackGroundColor;
    private float mTextWidth;
    private int mMaxMargin;
    private int mMargin;
    private Matrix mMatrix = new Matrix();
    private int mDuration = 500;
    private int mRockDuration = 300;
    private ValueAnimator mAnim;
    private ValueAnimator mRockAnim;
    private float mRatio = 1f;
    private int mRotation = 0;
    private Region mRegion;
    private RectF mRectF;
    private Paint mIconPaint;
    private Paint mPaint;
    private TextPaint mTextPaint;
    private OnClickListener onClickListener;
    private Handler mHandler = new Handler();
    private boolean mIsAuto = false;
    private boolean mIsCancel = false;

    public ExpandMenu(Context context) {
        this(context, null);
    }

    public ExpandMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ExpandMenu);
        mDuration = array.getInt(R.styleable.ExpandMenu_menu_expand_duration, mDuration);
        mRockDuration = array.getInt(R.styleable.ExpandMenu_menu_rock_duration, mRockDuration);
        mIconRes = array.getResourceId(R.styleable.ExpandMenu_menu_icon, R.mipmap.icon_menu);
        mRadius = (int) array.getDimension(R.styleable.ExpandMenu_menu_small_radius, Util.dp2Px(context, 20));
        mIconRadius = (int) array.getDimension(R.styleable.ExpandMenu_menu_radius, Util.dp2Px(context, 23));
        mTextSize = (int) array.getDimension(R.styleable.ExpandMenu_menu_radius, Util.dp2Px(context, 14));
        mText = array.getString(R.styleable.ExpandMenu_menu_text);
        mTextColor = array.getColor(R.styleable.ExpandMenu_menu_textColor, Color.WHITE);
        mBackGroundColor = array.getColor(R.styleable.ExpandMenu_menu_color, Color.RED);
        mIsAuto = array.getBoolean(R.styleable.ExpandMenu_menu_auto, false);
        array.recycle();
    }

    private void init() {
        mIcon = BitmapFactory.decodeResource(getResources(), mIconRes);
        mIcon = Util.imageScale(mIcon, mIconRadius * 2, mIconRadius * 2);
        mPadding = mIconRadius - mRadius;
        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        if (!TextUtils.isEmpty(mText)) {
            mTextWidth = mTextPaint.measureText(mText);
            mMaxMargin = (int) mTextWidth + mPadding * 2 - mRadius * 2;
            mMargin = mMaxMargin;
        }
        mRegion = new Region();
        mRectF = new RectF();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBackGroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        mAnim = ValueAnimator.ofFloat(1, 0);
        mRockAnim = ValueAnimator.ofInt(-30, 0, 30, 0);
        mAnim.addUpdateListener(animation -> {
            mRatio = (float) animation.getAnimatedValue();
            if (!mIsExpand) {
                mRatio = 1.0f - mRatio;
            }
            invalidate();
        });
        mAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mIsCancel) {//强制结束动画
                    if (!mIsExpand) {//关闭的
                        mRockAnim.start();//晃动动画开始
                    } else if (mIsAuto) {//自动模式
                        //这里最后做下延迟
                        mAnim.start();//关闭动画开始
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIsExpand = !mIsExpand; //展开｜关闭动画开始 - start
                mIsProgressing = true;
                mIsCancel = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsCancel = true;
            }
        });
        mRockAnim.addUpdateListener(animation -> {
            mRotation = (int) animation.getAnimatedValue();
            invalidate();
        });
        mRockAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsProgressing = false;//晃动动画结束 - end
                mIsRotating = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIsRotating = true;
            }
        });
        mAnim.setDuration(mDuration);
        mRockAnim.setDuration(mRockDuration);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = mMaxMargin + mIconRadius * 4;
        final int height = mIconRadius * 2;
        setMeasuredDimension(width, height);
        cancel();
        initAnimSet();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF.left = (mMargin + mPadding * 2 + mRadius * 2) * mRatio + mPadding;
        mRectF.top = mPadding;
        mRectF.right = mWidth - mPadding;
        mRectF.bottom = mHeight - mPadding;
        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
        //text
        if (!TextUtils.isEmpty(mText)
                && mRectF.width() > mTextWidth) {
            final int baseX = mIconRadius * 2 + mPadding;
            final int baseY = (int) ((mHeight / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
            canvas.drawText(mText, baseX, baseY, mTextPaint);
        }
        //icon
        if (mIsRotating) {
            canvas.save();
            canvas.translate(mWidth - mIconRadius * 2, 0);
            mMatrix.setRotate(mRotation, mIconRadius, mIconRadius);
            canvas.drawBitmap(mIcon, mMatrix, mIconPaint);
            canvas.restore();
        } else {
            canvas.drawBitmap(mIcon, mRectF.left - mPadding, mRectF.top - mPadding, mIconPaint);
        }
        mRegion.set((int) mRectF.left - mPadding,
                (int) mRectF.top - mPadding,
                (int) mRectF.right + mPadding,
                (int) mRectF.bottom + mPadding);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsProgressing) {//动画结束才可以点击
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //防止关闭时阻挡后面的控件的触摸事件
                if (mRegion.contains((int) event.getX(), (int) event.getY())) {
                    if (onClickListener != null) {
                        onClickListener.onClick(this);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void start() {
        if (!mIsProgressing) {
            mAnim.start();
        }
    }

    public void cancel() {
        if (mAnim != null && mAnim.isRunning()) {
            mAnim.cancel();
        }
        if (mRockAnim != null && mRockAnim.isRunning()) {
            mRockAnim.cancel();
        }
    }

    //重置到默认状态
    private void initAnimSet() {
        mRatio = 1;
        mRotation = 0;
        mIsExpand = false;
        mIsProgressing = false;
        mIsRotating = false;
    }

    //延迟开始动画 配合自动展开关闭使用
    public void showDelayed(int delay) {
        if (getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(this::start, delay);
        }
    }

    public void reset() {
        mHandler.removeCallbacksAndMessages(null);
        //动画运行中才需要重置
        if (mIsProgressing) {
            cancel();
            initAnimSet();
            invalidate();
        }
    }
}

final class Util {

    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        return Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);
    }

    public static int dp2Px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}