package com.vspace.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.vspace.util.MathUtil;

/**
 * A custom joystick/rocker widget implemented as a {@link SurfaceView}.
 *
 * <p>Renders a circular active area with a movable rocker (thumb) that follows
 * the user's touch. The rocker is constrained within the active area radius and
 * snaps back to the center when the user lifts their finger. This widget is
 * commonly used for directional input in virtual location or game control scenarios.</p>
 *
 * <p>The widget manages its own rendering thread via the {@link Runnable} interface
 * and responds to surface lifecycle events through {@link SurfaceHolder.Callback}.</p>
 *
 * @see SurfaceView
 * @see SurfaceHolder.Callback
 */
public class RockerView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    /** Default radius of the circular active area in pixels. */
    private static final int DEFAULT_AREA_RADIUS = 100;

    /** Default radius of the movable rocker thumb in pixels. */
    private static final int DEFAULT_ROCKER_RADIUS = 35;

    /** Default refresh cycle interval for the rendering thread in milliseconds. */
    private static final int DEFAULT_REFRESH_CYCLE = 30;

    /** Default callback cycle interval for position change notifications in milliseconds. */
    private static final int DEFAULT_CALLBACK_CYCLE = 300;

    /** The {@link SurfaceHolder} used to lock and draw on the surface canvas. */
    private SurfaceHolder mHolder;

    /** Flag controlling whether the drawing thread should continue running. */
    private static boolean mDrawOk = true;

    /** Flag controlling whether the callback thread should continue running. */
    private static boolean mCallbackOk = true;

    /**
     * The rocker active area center position.
     * usually, it is the center of this view.
     */
    private Point mAreaPosition;

    /**
     * The Rocker position.
     * usually, it as same asmAreaPosition .
     * if this view touched, it will follow the touch position.
     * <p/>
     * we get position information from this.
     */
    private Point mRockerPosition;

    /** The radius of the circular active area in pixels. */
    private int mAreaRadius = -1;

    /** The radius of the movable rocker thumb in pixels. */
    private int mRockerRadius = -1;

    /** Whether the rocker is allowed to move in response to touch events. */
    private boolean canMove = true;

    /** The interval in milliseconds between position callback notifications. */
    private final int mCallbackCycle = DEFAULT_CALLBACK_CYCLE;

    /**
     * Constructs a {@link RockerView} with the given context.
     *
     * @param context the {@link Context} for this view
     */
    public RockerView(Context context) {
        this(context, null);
    }

    /**
     * Constructs a {@link RockerView} with the given context and XML attributes.
     *
     * @param context the {@link Context} for this view
     * @param attrs   the {@link AttributeSet} from XML inflation, or {@code null}
     */
    public RockerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a {@link RockerView} with the given context, XML attributes, and
     * default style attribute.
     *
     * @param context      the {@link Context} for this view
     * @param attrs        the {@link AttributeSet} from XML inflation, or {@code null}
     * @param defStyleAttr the default style attribute resource
     */
    public RockerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // init attrs
        initAttrs();
        // set paint
        setPaint();

        if (isInEditMode()) {
            return;
        }

        // config surfaceView
        configSurfaceView();
        // config surfaceHolder
        configSurfaceHolder();
    }

    /**
     * Initializes the default attribute values for the area and rocker radii.
     */
    private void initAttrs() {
        mAreaRadius = DEFAULT_AREA_RADIUS;
        mRockerRadius = DEFAULT_ROCKER_RADIUS;
    }

    /**
     * Creates and configures the default {@link Paint} object for drawing.
     */
    private void setPaint() {
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    /**
     * Configures this {@link SurfaceView} with default display properties
     * (keep screen on, focusable, z-order on top).
     */
    private void configSurfaceView() {
        setKeepScreenOn(true); // do not lock screen when surfaceView is running.
        setFocusable(true); // make sure this surfaceView can get focus from keyboard.
        setFocusableInTouchMode(true); // make sure this surfaceView can get focus from touch.
        setZOrderOnTop(true); // make sure this surface is placed on top of the window
    }

    /**
     * Configures the {@link SurfaceHolder} with a transparent pixel format
     * and registers this view as the callback listener.
     */
    private void configSurfaceHolder() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT); // 设置背景透明
    }

    /**
     * Measures this view, using a calculated default size based on area and rocker
     * radii when the parent does not impose exact dimensions.
     *
     * @param widthMeasureSpec  the width requirements from the parent
     * @param heightMeasureSpec the height requirements from the parent
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth, measureHeight;
        int defaultWidth = (mAreaRadius + mRockerRadius) * 2;
        int defaultHeight = (mAreaRadius + mRockerRadius) / 2;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);      // 取出宽度的确切数值
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);      // 取出宽度的测量模式

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);    // 取出高度的确切数值
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);    // 取出高度的测量模式

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED || widthSize < 0) {
            measureWidth = defaultWidth;
        } else {
            measureWidth = widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED || heightSize < 0) {
            measureHeight = defaultHeight;
        } else {
            measureHeight = heightSize;
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    /**
     * Called when the size of this view changes. Recenters the area and rocker
     * positions and adjusts the radii if they have not been explicitly set.
     *
     * @param w       the new width of this view
     * @param h       the new height of this view
     * @param oldWidth  the old width of this view
     * @param oldHeight the old height of this view
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        mAreaPosition = new Point(w / 2, h / 2);
        mRockerPosition = new Point(mAreaPosition);

        // this need subtract the view padding
        int tempRadius = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom());
        tempRadius /= 2;
        if (mAreaRadius == -1) {
            mAreaRadius = (int) (tempRadius * 0.75);
        }

        if (mRockerRadius == -1) {
            mRockerRadius = (int) (tempRadius * 0.25);
        }
    }

    /**
     * Called when the surface is first created. Starts the drawing and callback threads.
     *
     * @param holder the {@link SurfaceHolder} whose surface was created
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Thread mDrawThread = new Thread(this);
            mDrawThread.start();
            // listener callback
            Thread mCallbackThread = new Thread(() -> {
                while (mCallbackOk) {
                    try {
                        Thread.sleep(mCallbackCycle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mCallbackThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the surface format or size changes. This implementation is a no-op.
     *
     * @param holder the {@link SurfaceHolder} whose surface changed
     * @param format the new {@link android.graphics.PixelFormat}
     * @param width  the new width of the surface
     * @param height the new height of the surface
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    /**
     * Called when the surface is destroyed. Stops the drawing and callback threads.
     *
     * @param holder the {@link SurfaceHolder} whose surface was destroyed
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawOk = false;
        mCallbackOk = false;
    }

    /**
     * Called when the visibility of this view changes. Starts or stops the drawing
     * and callback threads based on the new visibility state.
     *
     * @param changedView the view whose visibility changed
     * @param visibility  the new visibility value ({@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE})
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            mDrawOk = true;
            mCallbackOk = true;
        } else {
            mDrawOk = false;
            mCallbackOk = false;
        }
    }

    /*Event Response*******************************************************************************/
    /**
     * Handles touch events to move the rocker within its active area.
     *
     * <ul>
     *   <li>{@link MotionEvent#ACTION_DOWN}: Ignores the event if the touch is outside
     *       the active area radius.</li>
     *   <li>{@link MotionEvent#ACTION_MOVE}: Moves the rocker to the touch position if
     *       within the active area, or clamps it to the edge if the touch exceeds the radius.</li>
     *   <li>{@link MotionEvent#ACTION_UP}: Resets the rocker to the center position.</li>
     * </ul>
     *
     * @param event the {@link MotionEvent} describing the touch interaction
     * @return always returns {@code true} to consume the event
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        try {
            int len = MathUtil.Companion.getDistance(mAreaPosition.x, mAreaPosition.y, event.getX(), event.getY());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 如果屏幕接触点不在摇杆挥动范围内,则不处理
                if (len > mAreaRadius) {
                    return true;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (len <= mAreaRadius) {
                    // 如果手指在摇杆活动范围内，则摇杆处于手指触摸位置
                    mRockerPosition.set((int) event.getX(), (int) event.getY());
                } else {
                    // 设置摇杆位置，使其处于手指触摸方向的 摇杆活动范围边缘
                    mRockerPosition = MathUtil.Companion.getPointByCutLength(mAreaPosition,
                            new Point((int) event.getX(), (int) event.getY()), mAreaRadius);
                }
            }
            // 如果手指离开屏幕，则摇杆返回初始位置
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mRockerPosition = new Point(mAreaPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * The main rendering loop that continuously redraws the surface canvas.
     * Clears the canvas to transparent at each cycle and sleeps for the
     * configured refresh interval.
     */
    @Override
    public void run() {
        if (isInEditMode()) {
            return;
        }

        Canvas canvas = null;
        while (mDrawOk) {
            boolean canMove = this.canMove;
            try {
                if (canMove) {
                    canvas = mHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }
                Thread.sleep(DEFAULT_REFRESH_CYCLE); // 休眠
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null && canMove) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    /**
     * Draws a white background when the view is in the IDE preview (edit) mode.
     *
     * @param canvas the {@link Canvas} on which to draw
     */
    // for preview
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.WHITE);
        }
    }

    /**
     * Enables or disables the rocker movement in response to touch events.
     *
     * @param isMove {@code true} to allow rocker movement, {@code false} to freeze it
     */
    public void setCanMove(boolean isMove) {
        this.canMove = isMove;
    }
}
