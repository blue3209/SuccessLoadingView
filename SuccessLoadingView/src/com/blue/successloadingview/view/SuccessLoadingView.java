package com.blue.successloadingview.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.blue.successloadingview.R;

/**
 * 加载完成或者成功的View视图
 * 
 * Author:chengli3209@gmail.com 2016/08/25 10:20
 */
public class SuccessLoadingView extends View {
	private static final int DEFAULT_WIDTH = 30;
	private static final int DEFAULT_HEIGHT = 30;
	private static final int DEFAULT_PAINT_WIDTH = 5;

	private static final int DEFAULT_PAINT_COLOR = Color.parseColor("#C1904B");
	private static final int DEFAULT_ANIM_DURATION = 500;
	private static final int ROTATE_OFFSET = 0;

	/** 线的颜色 */
	private int mStrokeColor;
	/** 动画时间 */
	private int mAnimDuration;
	/** 线的宽度 */
	private int mStrokeWidth;

	/** 起始角度和扇形弧度 */
	private int mStartAngle = 180;
	private int mSweepAngle = 0;

	/** 圆心和半径 */
	private RectF mRectF;
	private float[] mCenterPoint;
	private float mCircelRadius;

	/** 画笔工具 */
	private Paint mPaint;
	private Paint mLinePaint;

	/** Path类 */
	private Path mArcPath;
	private Path mCirclePath;
	private Path mLinePath;
	private PathMeasure mPathMeasure;

	/** 动画控制相关 */
	private float animatedValue = 0;
	private boolean mRunning = false;
	private boolean mStopped = false;

	private boolean mLineRunning = false;
	private boolean mLineStopped = false;

	private ValueAnimator mValueAnimator, mLineValueAnimator;

	public SuccessLoadingView(Context context) {
		this(context, null);
	}

	public SuccessLoadingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SuccessLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SuccessLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SuccessLoadingView);
		mStrokeColor = ta.getColor(R.styleable.SuccessLoadingView_strokeColor, DEFAULT_PAINT_COLOR);
		mStrokeWidth = ta.getDimensionPixelSize(R.styleable.SuccessLoadingView_strokeWidth,
				dp2px(DEFAULT_PAINT_WIDTH));
		mAnimDuration = ta.getInt(R.styleable.SuccessLoadingView_duration, DEFAULT_ANIM_DURATION);
		ta.recycle();

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeCap(Cap.ROUND);
		mPaint.setStrokeJoin(Join.ROUND);
		mPaint.setColor(mStrokeColor);
		mPaint.setStrokeWidth(mStrokeWidth);

		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setColor(mStrokeColor);
		mLinePaint.setStrokeWidth(mStrokeWidth);

		mArcPath = new Path();
		mLinePath = new Path();
		mCirclePath = new Path();

		mCenterPoint = new float[2];
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(measureWidthSize(widthMeasureSpec),
				measureHeightSize(heightMeasureSpec));
	}

	public void setStrokeColor(int strokeColor) {
		this.mStrokeColor = strokeColor;
	}

	public void setAnimDuration(int animDuration) {
		this.mAnimDuration = animDuration;
	}

	public void setStrokeWidth(int strokeWidth) {
		this.mStrokeWidth = strokeWidth;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(mStrokeColor);
		mPaint.setStrokeWidth(mStrokeWidth);

		mLinePaint.setColor(mStrokeColor);
		mLinePaint.setStrokeWidth(mStrokeWidth);

		if (mRunning) {
			mArcPath.reset();
			mSweepAngle = (int) animatedValue;
			mArcPath.addArc(mRectF, mStartAngle, mSweepAngle);
			canvas.drawPath(mArcPath, mPaint);
		} else {
			if (mStopped) {
				mCirclePath.reset();
				mCirclePath.addCircle(mCenterPoint[0], mCenterPoint[1], mCircelRadius,
						Path.Direction.CCW);
				canvas.drawPath(mCirclePath, mPaint);

				if (mLineRunning) {
					Path path = new Path();
					float length = mPathMeasure.getLength();
					mPathMeasure.getSegment(0, length * animatedValue, path, true);
					canvas.drawPath(path, mLinePaint);
				} else {
					if (mLineStopped) {
						canvas.drawPath(mLinePath, mLinePaint);
					}
				}
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();
		int paddingBottom = getPaddingBottom();

		int width = getWidth();
		int height = getHeight();
		mCenterPoint[0] = (width - paddingLeft - paddingRight) >> 1;
		mCenterPoint[1] = (height - paddingTop - paddingBottom) >> 1;

		float radiusX = (width - paddingRight - paddingLeft - mStrokeWidth * 2) >> 1;
		float radiusY = (height - paddingTop - paddingBottom - mStrokeWidth * 2) >> 1;
		mCircelRadius = Math.min(radiusX, radiusY);

		mRectF = new RectF(paddingLeft + mStrokeWidth, paddingTop + mStrokeWidth, width
				- mStrokeWidth - paddingRight, height - mStrokeWidth - paddingBottom);

		mArcPath.arcTo(mRectF, mStartAngle, mSweepAngle, true);

		mLinePath.reset();
		mLinePath.moveTo((int) (width * 0.2f), (int) (height * 0.5f));
		mLinePath.lineTo((int) (width * 0.4f), (int) (height * 0.7f));
		mLinePath.lineTo((int) (width * 0.8f), (int) (height * 0.3f));

		mPathMeasure = new PathMeasure(mLinePath, false);
	}

	/**
	 * 动画更新
	 */
	private ValueAnimator.AnimatorUpdateListener animUpdateListener = new ValueAnimator.AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			if (!animation.isRunning()) {
				return;
			}
			animatedValue = Float.parseFloat(String.valueOf(animation.getAnimatedValue()));
			invalidate();
		}
	};

	public void startAnim() {
		if (mRunning || mLineRunning) {
			return;
		}

		mValueAnimator = ValueAnimator.ofFloat(ROTATE_OFFSET, -360f);
		mValueAnimator.setDuration(mAnimDuration);
		mValueAnimator.addUpdateListener(animUpdateListener);
		mValueAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mRunning = true;
				mStopped = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mRunning = false;
				mStopped = true;
				startLineAnim();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mRunning = false;
				mStopped = true;
				mLineRunning = false;
				mLineStopped = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mValueAnimator.start();
	}

	private void startLineAnim() {
		if (mLineRunning || mRunning) {
			return;
		}
		mLineValueAnimator = ValueAnimator.ofFloat(0, 1);
		mLineValueAnimator.setDuration(mAnimDuration);
		mLineValueAnimator.addUpdateListener(animUpdateListener);
		mLineValueAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mLineRunning = true;
				mLineStopped = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mLineRunning = false;
				mLineStopped = true;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mLineRunning = false;
				mLineStopped = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mLineValueAnimator.start();
	}

	/**
	 * measure width
	 * 
	 * @param measureSpec
	 *            spec
	 * @return width
	 */
	private int measureWidthSize(int measureSpec) {
		int defSize = dp2px(DEFAULT_WIDTH);
		int specSize = MeasureSpec.getSize(measureSpec);
		int specMode = MeasureSpec.getMode(measureSpec);

		int result = 0;
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
		case MeasureSpec.AT_MOST:
			result = Math.min(defSize, specSize);
			break;
		case MeasureSpec.EXACTLY:
			result = specSize;
			break;
		}
		return result;
	}

	/**
	 * measure height
	 * 
	 * @param measureSpec
	 *            spec
	 * @return height
	 */
	private int measureHeightSize(int measureSpec) {
		int defSize = dp2px(DEFAULT_HEIGHT);
		int specSize = MeasureSpec.getSize(measureSpec);
		int specMode = MeasureSpec.getMode(measureSpec);

		int result = 0;
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
		case MeasureSpec.AT_MOST:
			result = Math.min(defSize, specSize);
			break;
		case MeasureSpec.EXACTLY:
			result = specSize;
			break;
		}
		return result;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (null != mValueAnimator) {
			if (mValueAnimator.isRunning()) {
				mValueAnimator.cancel();
			}
			mValueAnimator = null;
		}

		if (null != mLineValueAnimator) {
			if (mLineValueAnimator.isRunning()) {
				mLineValueAnimator.cancel();
			}
			mLineValueAnimator = null;
		}
	}

	/**
	 * dp to px
	 * 
	 * @param dpValue
	 *            dp
	 * @return px
	 */
	private int dp2px(float dpValue) {
		final float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
