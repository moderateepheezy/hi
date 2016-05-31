package com.exolvetechnologies.hidoctor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.oovoo.core.Utils.LogSdk;
import com.oovoo.sdk.api.ooVooClient;
import com.oovoo.sdk.api.ui.VideoPanel;
import com.oovoo.sdk.interfaces.VideoFrame;
import com.oovoo.sdk.interfaces.VideoRender;
import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.ui.gl.GLESHelper;
import com.exolvetechnologies.hidoctor.ui.gl.GLESHelper.VideoAnimationListener;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by oovoo on 7/27/15.
 */
public class CustomVideoPanel extends GLSurfaceView implements GLSurfaceView.Renderer, VideoRender, VideoAnimationListener {

    public static String    TAG	= CustomVideoPanel.class.getSimpleName();
    private long	_nObj	= 0;    // accessed from native

    private static final int POST_DELAY = 1000;
    private GLESHelper glesHelper;
    private VideoPanel.VideoRenderStateChangeListener renderStateListener = null;
    private OrientationEventListener orientationListener = null;
    private int lastOrientation = - 1;
    private int lastActivityOrientation = -1;
    private boolean isCircleShape = false;
    private int deviceOrientation = 0;
    private int	activityOrientation = 0;
    private boolean isRenderingStarted = false;
    private boolean isPreview = false;
    private int mOrientation = 0;
    private int mActivityOrientation = 0;
    private Handler mainLooper = null ;
    private boolean isSurfaceChanged = false;
    private boolean restartCamera = false ;

    public CustomVideoPanel(Context context) {
        super(context);

        init(context);

        glInit();
    }

    public CustomVideoPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomVideoPanel, 0, 0);

        isCircleShape = a.getBoolean(R.styleable.CustomVideoPanel_isCircleShape, false);

        a.recycle();

        init(context);

        glInit();
    }

    private void init(final Context context) {

        mainLooper = new Handler(Looper.getMainLooper()) ;

        orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation)
            {
                if (orientation != ORIENTATION_UNKNOWN) {
                    activityOrientation = ((WindowManager) context
                            .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                            .getRotation();
                    switch (activityOrientation) {
                        case Surface.ROTATION_0 :
                            activityOrientation = 0;
                            break;
                        case Surface.ROTATION_90 :
                            activityOrientation = 90;
                            break;
                        case Surface.ROTATION_180 :
                            activityOrientation = 180;
                            break;
                        case Surface.ROTATION_270 :
                            activityOrientation = 270;
                            break;
                        default:
                            activityOrientation = 0;
                            break;
                    }

                    boolean update = activityOrientation != lastActivityOrientation;

                    if( orientation < 45 || orientation > 270 + 45 ) {
                        deviceOrientation = 0;
                        if( lastOrientation != deviceOrientation)
                            update = true ;
                    }
                    else if(orientation < 90 + 45 && deviceOrientation != 90){
                        deviceOrientation = 90;
                        if( lastOrientation != deviceOrientation)
                            update = true ;
                    }
                    else if(orientation < 180  + 45 && deviceOrientation != 180){
                        deviceOrientation = 180;
                        if( lastOrientation != deviceOrientation)
                            update = true ;
                    }
                    else if(orientation < 270 + 45 && deviceOrientation != 270){
                        deviceOrientation = 270;
                        if( lastOrientation != deviceOrientation)
                            update = true ;
                    }

                    if (update)
                    {
                        LogSdk.d(TAG, "orientation custom render changed to activity = " + activityOrientation + ", device orientation = " + orientation);
                        setOrientation(orientation, activityOrientation);
                        lastOrientation = deviceOrientation;
                        lastActivityOrientation = activityOrientation;
                    }
                }
            }
        };
        orientationListener.enable();
    }

    private void glInit() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setTopZOrder(true);

        glesHelper = new GLESHelper(isCircleShape);
        glesHelper.setVideoAnimationListener(this);
    }

    public void setOrientation(int orientation, int activityOrientation)
    {
        mOrientation = roundOrientation(orientation);
        mActivityOrientation = roundOrientation(activityOrientation);
    }

    public boolean isCircleShape() {
        return isCircleShape;
    }

    // accessed from native
    public void setNativeObj(long nObj) {
        this._nObj = nObj;
    }

    public long getNativeObj() {
        return _nObj;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        glesHelper.onSurfaceChanged(w, h);
        isSurfaceChanged = true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        int orientation = 0;

        if (!isPreview)
        {
            orientation = (360 - mOrientation) % 360;
            glesHelper.setMirrorView(false);
        }

        glesHelper.render((360 + orientation - mActivityOrientation) % 360);

        if (!isRenderingStarted) {
            if (isSurfaceChanged) {
                isSurfaceChanged = false;
                return;
            }
            mainLooper.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogSdk.d(TAG, "CustomVideoPanel onVideoRenderStart ");
                        if (renderStateListener != null) {
                            renderStateListener.onVideoRenderStart();
                            handler.removeCallbacks(r);
                            isRenderingStarted = true;
                        }
                    } catch (Exception err) {
                        LogSdk.e(TAG, "onVideoRenderingStarted" + err);
                    }
                }
            });
        }
    }

    private final Handler handler = new Handler();
    private final Runnable r = new Runnable() {
        public void run() {

            mainLooper.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogSdk.d(TAG, "CustomVideoPanel onVideoRenderStop ");
                        if (renderStateListener != null) {
                            renderStateListener.onVideoRenderStop();
                            isRenderingStarted = false;
                        }
                    } catch (Exception err) {
                        LogSdk.e(TAG, "onVideoRenderingStopped" + err);
                    }
                }
            });
        }
    };

    @Override
    public void onProcessVideoFrame(VideoFrame videoFrame) {
        try {
            handler.removeCallbacks(r);
            handler.postDelayed(r, POST_DELAY);
           // LogSdk.d(TAG,"ApplicationRenderWrap -> Java onProcessVideoFrame ->");
            glesHelper.setVideoFrame(videoFrame);

            requestRender();
          //  LogSdk.d(TAG, "ApplicationRenderWrap -> Java onProcessVideoFrame <-");
        }
        catch(Exception err){
            LogSdk.e(TAG,"onProcessVideoFrame "+err);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        if (isCircleShape) {
            height = getMeasuredWidth();
        } else {
            Point point = (Point) getTag();
            if (point != null) {
                height = point.y;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), height);
    }

    private void setTopZOrder(boolean onTop) {
        if (isCircleShape) {
            setZOrderOnTop(onTop);
        }
    }

    /***
     * Set event receiver for start/stop video rendering
     * @param listener
     */
    public void setVideoRenderStateChangeListener(VideoPanel.VideoRenderStateChangeListener listener) {
        LogSdk.d(TAG,"CustomVideoPanel setVideoRenderStateChangeListener " + listener);
        renderStateListener = listener;
    }

    @Override
    public void onVideoRotationAnimation() {
        requestRender();
    }

    public void setPreview(boolean isPreview) {
        this.isPreview = isPreview;
    }

    private int roundOrientation(int in)
    {
        in = in % 360;

        if (in < 0 * 90 + 45)
        {
            return 0;
        }

        if (in < 1 * 90 + 45)
        {
            return 90;
        }

        if (in < 2 * 90 + 45)
        {
            return 180 ;
        }

        if (in < 3 * 90 + 45)
        {
            return 270;
        }

        return 0;
    }

    public final void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        try{
            if(isPreview){
                if(restartCamera) {
                    restartCamera = false ;
                    ooVooClient.sharedInstance().getAVChat().getVideoController().openCamera();
                }
            }
        }
        catch(Exception err){
            LogSdk.e(TAG,"");
        }
    }

    public final void surfaceDestroyed(SurfaceHolder holder)
    {
        super.surfaceDestroyed(holder);
        try{
            if(isPreview){
                boolean state = ooVooClient.sharedInstance().getAVChat().getVideoController().isTransmited() ;
                if(state) {
                    restartCamera = true ;
                    ooVooClient.sharedInstance().getAVChat().getVideoController().closeCamera();
                }
            }
        }
        catch(Exception err){
            LogSdk.e(TAG,"");
        }
    }
}
