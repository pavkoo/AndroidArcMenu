package com.pavkoo.androidarcmenu;

import java.util.ArrayList;
import java.util.List;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

enum MenuState {
	EXPAND, SHRINK
};

public class GFloatingMenu extends ViewGroup {
	public static final int GANIMATE_TIME = 400;
	private View parentBlurView;
	private MenuState mState;

	private GFloatingMenuContainer container;

	private Rect containerBounds;
	private GFloatingMenuControlView cView;

	private RotateAnimation rotate;
	private RotateAnimation rotate_anti;
	private ObjectAnimator scale;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.mState == MenuState.EXPAND) {
			closeMenu();
			return true;
		}
		return super.onTouchEvent(event);
	}

	public MenuState getmState() {
		return mState;
	}

	public void setmState(MenuState mState) {
		if (this.mState != mState) {
			this.mState = mState;
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void toggleState() {
		if (this.mState == MenuState.EXPAND) {
			if (Build.VERSION.SDK_INT >= 16) {
				setBackground(null);
			} else {
				setBackgroundDrawable(null);
			}
			this.setmState(MenuState.SHRINK);
			cView.startAnimation(rotate_anti);
			scale.start();
			container.replayAnim(true);
		} else {
			if (parentBlurView == null) {
				try {
					throw new Exception("请先设置parentBlurView");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Bitmap newImg = BitmapUtil.fastblur(parentBlurView);
			Drawable d = new BitmapDrawable(getResources(), newImg);
			// TODO 切换背景的时候加入setAlpha动画
			// d.setAlpha(100);
			if (Build.VERSION.SDK_INT >= 16) {
				setBackground(d);
			} else {
				setBackgroundDrawable(d);
			}
			this.setmState(MenuState.EXPAND);
			cView.startAnimation(rotate);
			scale.start();
			container.replayAnim(false);
		}
	}

	public void closeMenu() {
		if (this.mState == MenuState.EXPAND) {
			toggleState();
		}
	}

	public GFloatingMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		containerBounds = new Rect();
		container = new GFloatingMenuContainer(context, attrs);
		addView(container);
		mState = MenuState.SHRINK;
		cView = new GFloatingMenuControlView(context);
		addView(cView);
		TypedArray a = getContext().obtainStyledAttributes(attrs, com.pavkoo.androidarcmenu.R.styleable.GFloatingMenu, 0, 0);
		try {
			Bitmap mbmpControl = ((BitmapDrawable) a.getDrawable(com.pavkoo.androidarcmenu.R.styleable.GFloatingMenu_floatIcon)).getBitmap();
			cView.setMbmpControl(mbmpControl);
		} finally {
			a.recycle();
		}
		createRotate();
		container.setVisibility(View.INVISIBLE);
	}

	private void createRotate() {
		rotate = new RotateAnimation((float) 0, (float) 90, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		rotate.setFillAfter(true);
		rotate.setDuration(GANIMATE_TIME);
		rotate_anti = new RotateAnimation((float) 90, (float) 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		rotate_anti.setDuration(GANIMATE_TIME);
		rotate_anti.setFillAfter(true);

		scale = ObjectAnimator.ofPropertyValuesHolder(cView, PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
				PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f)).setDuration(GANIMATE_TIME);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		final int iconRight = w - getPaddingRight() - GFloatingMenuControlView.SHADOW_WIDHT;
		final int iconBottom = h - getPaddingBottom() - GFloatingMenuControlView.SHADOW_WIDHT;
		final int squre = Math.min(iconRight - getPaddingLeft(), iconBottom - getPaddingTop());
		cView.layout(iconRight - cView.getMbmpControl().getWidth() - GFloatingMenuControlView.SHADOW_WIDHT, iconBottom - cView.getMbmpControl().getHeight()
				- GFloatingMenuControlView.SHADOW_WIDHT, iconRight + GFloatingMenuControlView.SHADOW_WIDHT, iconBottom + GFloatingMenuControlView.SHADOW_WIDHT);
		containerBounds.set(iconRight - squre, iconBottom - squre, iconRight - cView.getWidth() / 2, iconBottom - cView.getHeight() / 2);
		container.layout(containerBounds.left, containerBounds.top, containerBounds.right, containerBounds.bottom);
	}

	public void AddMenuItem(Bitmap icon, String text, OnItemClickListener listener) {
		container.AddMenuItem(icon, text, listener);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}

	public View getParentBlurView() {
		return parentBlurView;
	}

	public void setParentBlurView(View parentBlurView) {
		this.parentBlurView = parentBlurView;
	}

	private class GFloatingMenuControlView extends View {
		public static final int SHADOW_WIDHT = 0;
		private Bitmap mbmpControl;
		private Rect controlBounds;
		private Paint mFloatIconPaint;
		private GestureDetector mDetector;

		public Bitmap getMbmpControl() {
			return mbmpControl;
		}

		public void setMbmpControl(Bitmap mbmpControl) {
			this.mbmpControl = mbmpControl;
			controlBounds = new Rect();
		}

		public GFloatingMenuControlView(Context context) {
			super(context);
			mFloatIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mFloatIconPaint.setColor(Color.WHITE);
			mDetector = new GestureDetector(getContext(), mListener);
		}

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return mDetector.onTouchEvent(event);
		}

		private SimpleOnGestureListener mListener = new SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				toggleState();
				super.onLongPress(e);
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				toggleState();
				return super.onSingleTapUp(e);
			}
		};

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			controlBounds.set(0, 0, w, h);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawCircle(controlBounds.exactCenterX(), controlBounds.exactCenterY(), (controlBounds.width() - SHADOW_WIDHT) / 2, mFloatIconPaint);
			canvas.drawBitmap(mbmpControl, controlBounds.left + SHADOW_WIDHT, controlBounds.top + SHADOW_WIDHT, mFloatIconPaint);
		}
	}

	private class GFloatingMenuContainer extends ViewGroup {
//		private static final float TEXT_PADDING_FACTOR = 0.6f;
		private static final float SPACE_WIDTH = 5;
		private static final float ARC_CORRECTION_COEFFICIENT = 10;
		private Rect mBounds;
		private List<GFloatingMenuItem> mViews;
		private float itemdegree;

		private float hintRadis;
		private float childLeft;
		private float childTop;

		public float getHintRadis() {
			return hintRadis;
		}

		private float hintMenuTextRadis;
		private float hintMenuIconRadis;

		public float getHintMenuIconRadis() {
			return hintMenuIconRadis;
		}

		private float iconWidth;

		public LayoutAnimationController controllAnim;
		public LayoutAnimationController controllAnimReverse;

		public float getIconWidth() {
			return iconWidth;
		}

		public float getTextWidth() {
			return textWidth;
		}

		private float textWidth;
		private GFloatingMenuHint hint;

		private boolean layoutini = false;

		public GFloatingMenuContainer(Context context, AttributeSet attrs) {
			super(context, attrs);
			mBounds = new Rect();
			// why the custom group view has a default padding????
			// we don't want ,so set to 0
			setPadding(0, 0, 0, 0);
			hint = new GFloatingMenuHint(context, hintRadis);
		}

		private void getControll() {
			AnimationSet set = new AnimationSet(false);
			Animation anim = new AlphaAnimation(0, 1);
			Animation scale = new ScaleAnimation(0, 1, 0, 1, mBounds.right, mBounds.bottom);
			anim.setDuration(GANIMATE_TIME);
			scale.setDuration(GANIMATE_TIME);
			scale.setInterpolator(new OvershootInterpolator());
			set.addAnimation(anim);
			set.addAnimation(scale);
			set.setFillAfter(true);
			controllAnim = new LayoutAnimationController(set, 0.1f);
			setLayoutAnimation(controllAnim);

			AnimationSet setR = new AnimationSet(false);
			Animation animR = new AlphaAnimation(1, 0);
			Animation scaleR = new ScaleAnimation(1, 0, 1, 0, mBounds.right, mBounds.bottom);
			animR.setDuration(GANIMATE_TIME);
			scaleR.setDuration(GANIMATE_TIME);
			scaleR.setInterpolator(new AnticipateOvershootInterpolator());
			setR.addAnimation(animR);
			setR.addAnimation(scaleR);
			setR.setFillAfter(true);
			controllAnimReverse = new LayoutAnimationController(setR, 0.1f);
		}

		public void replayAnim(boolean expanded) {
			clearAnimation();
			if (!expanded) {
				setLayoutAnimation(controllAnim);
				GFloatingMenuContainer.this.setVisibility(View.VISIBLE);
				setLayoutAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
					}
				});
			} else {
				setLayoutAnimation(controllAnimReverse);
				setLayoutAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						GFloatingMenuContainer.this.setVisibility(View.INVISIBLE);
					}
				});
			}
			startLayoutAnimation();
		}

		private void AddMenuItem(Bitmap icon, String text, OnItemClickListener listener) {
			if (mViews == null) {
				mViews = new ArrayList<GFloatingMenuItem>();
			}
			GFloatingMenuItemInfo item = new GFloatingMenuItemInfo(icon, text);
			GFloatingMenuItem view = new GFloatingMenuItem(getContext(), this);
			view.setItemInfo(item);
			view.setOnClickListener(listener);
			addView(view);
			mViews.add(view);
			onDataChanged();
			// keep hint at the font of all view
			removeView(hint);
			addView(hint);
		}

		private void onDataChanged() {
			// android draw arc start from 3 clock.
			// so we set 180 to start from 9 clock.
			if (!layoutini)
				return;
			float startD = GFloatingMenuItemInfo.START_ANGLE;
			itemdegree = 90f / mViews.size();
			float sweepAngle = itemdegree - GFloatingMenuItemInfo.SPACE_DEGREE;
			for (int i = 0; i < mViews.size(); i++) {
				GFloatingMenuItemInfo it = mViews.get(i).getItemInfo();
				it.startAngle = startD;
				startD += itemdegree;
				if (i == mViews.size() - 1) {
					it.sweepAngle = 270f - it.startAngle;
				} else {
					it.sweepAngle = sweepAngle;
				}
				it.preCaltextRotate();
				mViews.get(i).layout((int) (childLeft), (int) (childTop), mBounds.right, mBounds.bottom);
				mViews.get(i).setTextSize(GFloatingMenuItem.textSize);
			}
			hint.layout((int) (mBounds.right - hintRadis - ARC_CORRECTION_COEFFICIENT), (int) (mBounds.bottom - hintRadis - ARC_CORRECTION_COEFFICIENT),
					mBounds.right, mBounds.bottom);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			layoutini = true;
			mBounds.set(0, 0, w, h);
			float square = Math.min(w, h);
			hintRadis = square / 5 - SPACE_WIDTH;
			hintMenuTextRadis = square / 5 * 3;
			hintMenuIconRadis = square / 5 * 4;
			iconWidth = hintMenuIconRadis - hintMenuTextRadis;
			textWidth = hintMenuTextRadis - hintRadis;
			hint.setHintRadis(hintRadis + ARC_CORRECTION_COEFFICIENT);
			childLeft = w - hintMenuIconRadis;
			childTop = h - hintMenuIconRadis;
			onDataChanged();
			getControll();
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			// AddMenuItem had aleardy layout,no nesseary to layout again;
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			MotionEvent child_ev = MotionEvent.obtain(ev);
			if (mViews.size() > 0) {
				child_ev.offsetLocation(-mViews.get(0).getLeft(), -mViews.get(0).getTop());
			}
			for (int i = 0; i < mViews.size(); i++) {
				GFloatingMenuItem child = mViews.get(i);
				if (child.containsPoint(child_ev)) {
					return child.dispatchTouchEvent(child_ev);
				} else {
					child.setPressed(false);
				}
			}
			child_ev.recycle();
			return false;
		}
	}

	private class GFloatingMenuHint extends View {
		private Paint mHintBGPaint;
		private float hintRadis;
		private RectF mBounds;

		public void setHintRadis(float hintRadis) {
			this.hintRadis = hintRadis;
			this.invalidate();
		}

		public GFloatingMenuHint(Context context, float hintRadis) {
			super(context);
			mBounds = new RectF();
			mHintBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mHintBGPaint.setColor(Color.GREEN);
			mHintBGPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			this.hintRadis = hintRadis;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawCircle(mBounds.right, mBounds.bottom, hintRadis, mHintBGPaint);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mBounds.set(0, 0, w, h);
			setPivotX(w);
			setPivotY(h);
		}
	}

	public class GFloatingMenuItem extends View {
		private static final String GFloatingMenuItem_TAG = "GFloatingMenuItem";
		private static final int ICON_BG_ALPHA = 20;
		private static final int TEXT_BG_ALPHA = 100;
		private static final int ICON_BG_ALPHA_PRESS = 60;
		private static final int TEXT_BG_ALPHA_PRESS = 140;
		private static final float textSize = 14;
		private Paint mIconBgPaint;
		private Paint mTextPaint;

		private GestureDetector mMenuItemDetector;

		private GFloatingMenuItemInfo itemInfo;

		private GFloatingMenuContainer owner;
		private OnItemClickListener onClickListener;

		public OnItemClickListener getOnClickListener() {
			return onClickListener;
		}

		public void setOnClickListener(OnItemClickListener onClickListener) {
			this.onClickListener = onClickListener;
		}

		private RectF mBounds;
		private RectF mOrignBounds;
		private RectF mTextBGBounds;
		private Rect textBounds;

		private SimpleOnGestureListener mMenuItemListener = new SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				Log.i(GFloatingMenuItem_TAG, itemInfo.text + "onDown");
				GFloatingMenuItem.this.setPressed(true);
				return true;
			}
		};
		private boolean pressed;

		public boolean isPressed() {
			return pressed;
		}

		public void setPressed(boolean pressed) {
			if (this.pressed != pressed) {
				this.pressed = pressed;
				this.invalidate();
			}
		}

		public GFloatingMenuItemInfo getItemInfo() {
			return itemInfo;
		}

		public void setItemInfo(GFloatingMenuItemInfo itemInfo) {
			this.itemInfo = itemInfo;
			this.invalidate();
		}

		public GFloatingMenuItem(Context context, GFloatingMenuContainer parent) {
			super(context);
			owner = parent;
			mBounds = new RectF();
			mTextBGBounds = new RectF();
			mOrignBounds = new RectF();
			textBounds = new Rect();
			mIconBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mIconBgPaint.setColor(Color.WHITE);
			mIconBgPaint.setStyle(Paint.Style.STROKE);
			mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mTextPaint.setColor(Color.BLACK);
			mTextPaint.setTextAlign(Align.CENTER);
			// this code below is very importent,i take 8 hours to find
			// see detail
			// :http://stackoverflow.com/questions/25150598/canvas-drawarc-artefacts
			this.setLayerType(LAYER_TYPE_SOFTWARE, null);
			mMenuItemDetector = new GestureDetector(context, mMenuItemListener);
		}

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			boolean result = mMenuItemDetector.onTouchEvent(event);
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_UP:
				GFloatingMenuItem.this.setPressed(false);
				if (onClickListener != null) {
					onClickListener.onItemClick(GFloatingMenuItem.this);
				}
				return false;
			}
			return result;
		}

		public void setTextSize(float size) {
			size = sp2px(getContext(),size);
			mTextPaint.setTextSize(size);
			mTextPaint.getTextBounds(itemInfo.text, 0, itemInfo.text.length(), textBounds);
		}
		
		public int sp2px(Context context, float spValue) { 
            float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
            return (int) (spValue * fontScale + 0.5f); 
        } 

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			// we assume (right,bottom) will be the center of arc,
			// so we enlarge the area
			// final int radis = (int) owner.getHintMenuIconRadis();
			mOrignBounds.set(0, 0, w, h);
			final float iconCenter = owner.getIconWidth() / 2;
			final float textCenter = owner.getTextWidth() / 2 + owner.getIconWidth();
			// the paint can't set inside or outside ,so we precalculate the
			// bounds;
			// see detail
			// here:http://stackoverflow.com/questions/15309029/android-paint-stroke-width-positioning
			mBounds.set(iconCenter, iconCenter, 2 * w - iconCenter, 2 * h - iconCenter);
			mTextBGBounds.set(textCenter, textCenter, 2 * w - textCenter, 2 * h - textCenter);

			Point pcenter = new Point();
			float centerDegree = itemInfo.getCenterDegree();
			getCrossPoint(centerDegree, pcenter, w - iconCenter);
			itemInfo.iconCenter = pcenter;
			Point ptextCenter = new Point();
			getCrossPoint(centerDegree, ptextCenter, w - textCenter);
			itemInfo.textCenter = ptextCenter;
		}

		public void getCrossPoint(float degree, Point p, float radis) {
			degree = degree - GFloatingMenuItemInfo.START_ANGLE;
			double d = Math.toRadians(degree);
			final int dx = (int) (Math.cos(d) * radis);
			final int dy = (int) (Math.sin(d) * radis);
			p.x = (int) (mOrignBounds.right - dx);
			p.y = (int) (mOrignBounds.bottom - dy);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (itemInfo == null)
				return;
			if (pressed) {
				mIconBgPaint.setAlpha(ICON_BG_ALPHA_PRESS);
				mIconBgPaint.setStrokeWidth(owner.getIconWidth());
				canvas.drawArc(mBounds, itemInfo.startAngle, itemInfo.sweepAngle, false, mIconBgPaint);
				mIconBgPaint.setAlpha(TEXT_BG_ALPHA_PRESS);
				mIconBgPaint.setStrokeWidth(owner.getTextWidth());
			} else {
				mIconBgPaint.setAlpha(ICON_BG_ALPHA);
				mIconBgPaint.setStrokeWidth(owner.getIconWidth());
				canvas.drawArc(mBounds, itemInfo.startAngle, itemInfo.sweepAngle, false, mIconBgPaint);
				mIconBgPaint.setAlpha(TEXT_BG_ALPHA);
				mIconBgPaint.setStrokeWidth(owner.getTextWidth());
			}
			canvas.drawArc(mTextBGBounds, itemInfo.startAngle, itemInfo.sweepAngle, false, mIconBgPaint);
			canvas.drawBitmap(itemInfo.icon, itemInfo.iconCenter.x - itemInfo.icon.getWidth() / 2, itemInfo.iconCenter.y - itemInfo.icon.getHeight() / 2, null);
			canvas.save();
			canvas.translate(itemInfo.textCenter.x, itemInfo.textCenter.y);
			canvas.rotate(itemInfo.textRotate);
			canvas.drawText(itemInfo.text, 0, textBounds.height() / 2, mTextPaint);
			canvas.restore();
		}

		private boolean containsPoint(MotionEvent ev) {
			float x = ev.getX();
			float y = ev.getY();
			if (x < 0 || y < 0) {
				return false;
			}

			double distence = Math.sqrt(Math.pow(x - mOrignBounds.right, 2) + Math.pow(y - mOrignBounds.bottom, 2));
			Log.i("", "距离：" + String.valueOf(distence) + " owner.getHintMenuIconRadis() " + String.valueOf(owner.getHintMenuIconRadis())
					+ " owner.getHintRadis() " + String.valueOf(owner.getHintRadis()));
			if (distence < owner.getHintRadis() || distence > owner.getHintMenuIconRadis()) {
				return false;
			}
			x = mOrignBounds.right - x;
			y = mOrignBounds.bottom - y;
			double angleR = 0;
			if (x == 0) {
				angleR = 90;
			} else {
				angleR = Math.atan2(y, x);
				angleR = Math.toDegrees(angleR);
			}
			Log.i("", "角度：" + String.valueOf(angleR));
			return itemInfo.containsAngle((float) angleR);
		}
	}

	public class GFloatingMenuItemInfo {
		public static final int START_ANGLE = 180;
		public static final float SPACE_DEGREE = 0.5f;
		public Bitmap icon;
		public String text;
		public float startAngle;
		public float sweepAngle;
		public Point iconCenter;
		public Point textCenter;
		public float textRotate;

		public GFloatingMenuItemInfo(Bitmap d, String text) {
			this.icon = d;
			this.text = text;
		}

		public float getCenterDegree() {
			return startAngle + (sweepAngle - SPACE_DEGREE) / 2;
		}

		public void preCaltextRotate() {
			textRotate = startAngle + (sweepAngle - SPACE_DEGREE) / 2 - START_ANGLE;
		}

		public boolean containsAngle(float angle) {
			angle = angle + START_ANGLE;
			if (angle >= startAngle && (angle < (startAngle + sweepAngle))) {
				return true;
			}
			return false;
		}
	}

	public interface OnItemClickListener {
		void onItemClick(GFloatingMenuItem view);
	}

}
