package com.droidsonroids.awesomeprogressbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class AwesomeProgressBar extends View {
    private static final float DEFAULT_STROKE_THICKNESS = 5.0f;
    private static final int DEFAULT_ANIMATION_DURATION = 800;
    private static final float DEFAULT_CIRCLE_RADIUS = 100f;
    private static final float DEFAULT_CROSS_SCALE = 0.9f;

    private Paint mBackgroundPaint;
    private Paint mProgressBarPaint;
    private ValueAnimator mProgressAnimation;
    private AnimatorSet mAnimatorSet;

    private boolean mSuccessState;
    private float mProgressValue;
    private float mSuccessValue;
    private int mAnimationDuration;
    private int mBackgroundColor;
    private int mProgressBarColor;

    private RectF mRectF;
    private float mCenterX;
    private float mCenterY;
    private float mCircleRadius;
    private float mStrokeThickness;
    private boolean isAnimationInitialized = false;

    private enum State {RUNNING_STATE, IDLE_STATE, SUCCESS_STATE}
    private State mState;

    public AwesomeProgressBar(Context context) {
        this(context, null);
    }

    public AwesomeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AwesomeProgressBar);
            try {
                mStrokeThickness = array.getDimension(R.styleable.AwesomeProgressBar_strokeThickness,
                        DEFAULT_STROKE_THICKNESS);
                mCircleRadius = array.getDimension(R.styleable.AwesomeProgressBar_circleRadius,
                        DEFAULT_CIRCLE_RADIUS);
                mAnimationDuration = array.getInteger(R.styleable.AwesomeProgressBar_animationDuration,
                        DEFAULT_ANIMATION_DURATION);
                mBackgroundColor = array.getColor(R.styleable.AwesomeProgressBar_backgroundColor,
                        getDefaultBackgroundColor());
                mProgressBarColor = array.getColor(R.styleable.AwesomeProgressBar_progressBarColor,
                        getDefaultProgressBarColor());
            } finally {
                array.recycle();
            }
        }
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mStrokeThickness);
        mBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mProgressBarPaint = new Paint();
        mProgressBarPaint.setColor(mProgressBarColor);
        mProgressBarPaint.setStyle(Paint.Style.STROKE);
        mProgressBarPaint.setStrokeWidth(mStrokeThickness);
        mProgressBarPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mState = State.IDLE_STATE;
    }

    private int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.default_background_color);
    }

    private int getDefaultProgressBarColor() {
        return getResources().getColor(R.color.default_progress_bar_color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        mRectF = new RectF(mCenterX - mCircleRadius, mCenterY - mCircleRadius,
                mCenterX + mCircleRadius, mCenterY + mCircleRadius);

        if (!isAnimationInitialized) {
            setupAnimations();
            isAnimationInitialized = true;
        }
    }

    private void setupAnimations() {
        mProgressAnimation = ValueAnimator.ofFloat(0, 360);
        mProgressAnimation.setDuration(mAnimationDuration);
        mProgressAnimation.setInterpolator(new LinearInterpolator());
        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { //
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgressValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mProgressAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mState = State.RUNNING_STATE;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ValueAnimator successAnimationOvershoot;
        successAnimationOvershoot = ValueAnimator.ofFloat(0, mCircleRadius / DEFAULT_CROSS_SCALE);
        successAnimationOvershoot.setDuration(mAnimationDuration);
        successAnimationOvershoot.setInterpolator(new OvershootInterpolator());
        successAnimationOvershoot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSuccessValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
        rotateAnimation.setDuration(mAnimationDuration / 2);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(rotateAnimation, successAnimationOvershoot);
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mState = State.SUCCESS_STATE;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgressCircle(canvas, mBackgroundPaint);

        if (mState == State.RUNNING_STATE) {
            canvas.drawArc(mRectF, 270, mProgressValue, false, mProgressBarPaint);
        } else if (mState == State.SUCCESS_STATE && mSuccessState) {
            drawSuccessElement(canvas);
        } else if (mState == State.SUCCESS_STATE && !mSuccessState) {
            drawFailureElement(canvas);
        }
    }

    public void play(boolean successFlag) {
        mSuccessState = successFlag;
        if (mProgressAnimation.isRunning()) {
            mState = State.IDLE_STATE;
        }
        mProgressAnimation.start();
    }

    private void drawProgressCircle(Canvas canvas, Paint circleColor) {
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, circleColor);
    }

    private void drawSuccessElement(Canvas canvas) {
        drawProgressCircle(canvas, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX + mSuccessValue / 2, mCenterY, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX - mSuccessValue / 2, mCenterY, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY + mSuccessValue / 2, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mSuccessValue / 2, mProgressBarPaint);
    }

    private void drawFailureElement(Canvas canvas) {
        drawProgressCircle(canvas, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX + mSuccessValue / 2, mCenterY - mSuccessValue / 2, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX - mSuccessValue / 2, mCenterY + mSuccessValue / 2, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX + mSuccessValue / 2, mCenterY + mSuccessValue / 2, mProgressBarPaint);
        canvas.drawLine(mCenterX, mCenterY, mCenterX - mSuccessValue / 2, mCenterY - mSuccessValue / 2, mProgressBarPaint);
    }
}