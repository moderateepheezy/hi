package com.exolvetechnologies.hidoctor.ui.gl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.oovoo.core.Utils.LogSdk;
import com.oovoo.sdk.interfaces.VideoFrame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by oovoo on 7/29/15.
 */
public class GLESHelper {

    public static final String TAG = GLESHelper.class.getSimpleName();

    private VideoAnimationListener mAnimationListener = null;

    private VideoFrame          mVideoFrame;

    private int                 mProgramHandle;

    private int				    mTextureY;
    private int				    mTextureU;
    private int				    mTextureV;

    private FloatBuffer         mVertexPositions		= null;
    private FloatBuffer		    mTexturePositions		= null;
    private int				    mWidth					= 0;
    private int				    mHeight					= 0;
    private boolean             isFrameChanged          = false;
    private boolean             isSizeChanged           = false;
    private int				    isCircleShape           = 1;

    private float			    mVertexCoordinates[]	= new float[8];
    private float			    mTextureCoordinates[]	= new float[8];
    private float[]			    mTransformMatrix		= new float[16];
    private int				    mAttPos;														// vertex
    private int				    mAttTexcoord;
    private int				    mUniTexY;														// texture Y
    private int				    mUniTexU;														// texture U
    private int				    mUniTexV;														// texture V
    private int				    mUniTransform;													// transform
    private int				    mUniCircleShape;
    private int				    mPrevRotation			= -1;
    private boolean			    mPrevMirror				= false;
    private boolean			    mFitVideo				= true;
    private float			    mOwnerAspect			= 1;
    private float			    mPrevOwnerAspect		= 1;
    private int				    mPrevDeviceRotation		= -1;
    private byte[]              mBufY                   = null;
    private byte[]              mBufU                   = null;
    private byte[]              mBufV                   = null;
    private AnimatedRotation	mAnimation              = null;
    private boolean             isMirrorView            = true;
    private ReentrantLock       mFrameLock              = new ReentrantLock();

    public GLESHelper(boolean isCircleShape) {
        this.isCircleShape = isCircleShape ? 1 : 0;

        mAnimation = new AnimatedRotation();
        mAnimation.setAnimateRotation(true);
    }

    public void setMirrorView(boolean mirror) {
        isMirrorView = mirror;
    }

    public void setVideoFrame(VideoFrame videoFrame)
    {
        mFrameLock.lock();

        mVideoFrame = videoFrame;

        int bufLength = videoFrame.getWidth() * videoFrame.getHeight();
        int yOffset = 0;
        int uOffset = bufLength;
        int vOffset = uOffset + bufLength / 4;

        mBufY = new byte[bufLength];
        mBufU = new byte[bufLength / 4];
        mBufV = new byte[bufLength / 4];
        byte[] video_data_buffer = videoFrame.getData().getData();
        System.arraycopy(video_data_buffer, yOffset, mBufY, 0, bufLength);
        System.arraycopy(video_data_buffer, uOffset, mBufU, 0, bufLength / 4);
        System.arraycopy(video_data_buffer, vOffset, mBufV, 0, bufLength / 4);

        isFrameChanged = true;

        mFrameLock.unlock();
    }

    private boolean createProgram()
    {
        GLESUtils.cleanGlError();

        String vertShaderTxt = "attribute vec4 position;\n" + "attribute vec4 texcoord;\n"
                + "varying vec4 v_position;\n"
                + "varying vec2 textureCoordinate;\n" + "uniform mat4 transformMatrix;\n" + "void main()\n" + "{\n"
                + "gl_Position = transformMatrix * position;\n" + "textureCoordinate = texcoord.xy;\n"
                + "v_position = transformMatrix * position;\n" + "}";

        String fragShaderTxt = "varying mediump vec2 textureCoordinate;\n" + "uniform sampler2D texY;\n"
                + "varying mediump vec4 v_position;\n" + "uniform int isCircleShape;\n"
                + "uniform sampler2D texU;\n" + "uniform sampler2D texV;\n" + "void main()\n" + "{\n"
                + "  mediump float y = texture2D(texY, textureCoordinate).r;\n"
                + "  mediump float u = texture2D(texU, textureCoordinate).r;\n"
                + "  mediump float v = texture2D(texV, textureCoordinate).r;\n"
                + "  mediump vec4 color = vec4(y,u,v,1.0);\n"
                + "  mediump vec3 convertedColor = vec3(-0.87075, 0.52975, -1.08175);\n"
                + "  convertedColor += 1.164 * color.rrr;                     // Y\n"
                + "  convertedColor += vec3(0.0, -0.391, 2.018) * color.ggg;  // U\n"
                + "  convertedColor += vec3(1.596, -0.813, 0.0) * color.bbb;  // V\n"
                + "  mediump vec2 pos = vec2(v_position.x, v_position.y);\n"
                + "  mediump vec2 center = vec2(0.0, 0.0);\n"
                + "  mediump float distance = distance(center, pos);\n"
                + "  mediump float radius = 1.0;\n"
                + "  if (distance < radius || isCircleShape == 0) {\n"
                + "  	gl_FragColor = vec4(convertedColor, 1.0);\n"
                + "  } else {\n"
                + "  	gl_FragColor = vec4(0.01, 0.0, 0.0, 0.0);\n"
                + "  }\n"
                + "}";

        mProgramHandle = GLESUtils.createProgram(vertShaderTxt, fragShaderTxt);

        if (mProgramHandle == 0)
        {
            GLESUtils.checkGlError("createProgram");
            return false;
        }

        // Get program parameters
        mUniTransform = GLES20.glGetUniformLocation(mProgramHandle, "transformMatrix");
        GLESUtils.checkGlError("glGetUniformLocation:transformMatrix");
        mUniTexY = GLES20.glGetUniformLocation(mProgramHandle, "texY");
        GLESUtils.checkGlError("glGetUniformLocation:texY");
        mUniTexU = GLES20.glGetUniformLocation(mProgramHandle, "texU");
        GLESUtils.checkGlError("glGetUniformLocation:texY");
        mUniTexV = GLES20.glGetUniformLocation(mProgramHandle, "texV");
        GLESUtils.checkGlError("glGetUniformLocation:texY");
        mUniCircleShape = GLES20.glGetUniformLocation(mProgramHandle, "isCircleShape");
        GLESUtils.checkGlError("glGetUniformLocation:isCircleShape");
        mAttPos = GLES20.glGetAttribLocation(mProgramHandle, "position");
        GLESUtils.checkGlError("glGetAttribLocation:position");
        mAttTexcoord = GLES20.glGetAttribLocation(mProgramHandle, "texcoord");
        GLESUtils.checkGlError("glGetAttribLocation:texcoord");
        return true;
    }

    public void deleteProgram()
    {
        if (mProgramHandle != 0) {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = 0;
        }
    }

    public void onSurfaceChanged(int w, int h)
    {
        GLES20.glViewport(0, 0, w, h);

        mPrevDeviceRotation = -1;
        mOwnerAspect = (float) w / (float) h;
        mPrevOwnerAspect = -1;
    }

    public void render(int rotation)
    {
        mFrameLock.lock();

        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT);

        if (updateTextures() && updateParameters(rotation)) {
            GLES20.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        }

        if (!mAnimation.isCompleted() && mAnimationListener != null) {
            //mAnimationListener.onVideoRotationAnimation();
        }

        mFrameLock.unlock();
    }

    protected boolean createTextures()
    {
        GLESUtils.cleanGlError();
        // Y
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        mTextureY = generateAndBindTexture();

        int texUVWidth = mWidth / 2;
        int texUVHeight = mHeight / 2;

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mWidth, mHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, null);
        GLESUtils.checkGlError("FBO:glTexImage2D:Y");

        // U
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        mTextureU = generateAndBindTexture();
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, texUVWidth, texUVHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, null);
        GLESUtils.checkGlError("FBO:glTexImage2" + "D:U");

        // V
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        mTextureV = generateAndBindTexture();

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, texUVWidth, texUVHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, null);
        GLESUtils.checkGlError("FBO:glTexImage2" + "D:V");

        return true;
    }

    protected boolean updateTextures()
    {
        if (!isFrameChanged)
            return true;

        int w = mVideoFrame.getWidth();
        int h = mVideoFrame.getHeight();

        isSizeChanged = (mWidth != w || mHeight != h);

        mWidth = w;
        mHeight = h;

        if (isSizeChanged)
        {
            destroyTextures();

            if (!createTextures())
            {
                return false;
            }
        }

        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
        GLESUtils.checkGlError("FBO::update::BindTexture:Y");
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(mBufY));
        GLESUtils.checkGlError("FBO::glTexSubImage2D:Y");

        // U
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU);
        GLESUtils.checkGlError("FBO::update::BindTexture:U");
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth / 2, mHeight / 2, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(mBufU));

        // V
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV);
        GLESUtils.checkGlError("FBO::update::BindTexture:V");
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth / 2, mHeight / 2, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(mBufV));

        GLESUtils.checkGlError("FBO::glTexSubImage2D:UV");

        return true;
    }

    /**
     * Generate a named texture and bind it to a texturing target
     *
     * @return the named texture
     */
    public int generateAndBindTexture()
    {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLESUtils.checkGlError("FBO::glGenTextures");
        if (texture[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

        GLESUtils.checkGlError("FBO::BindTexture 1");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLESUtils.checkGlError("FBO::BindTexture 2");

        return texture[0];
    }

    protected void destroyTextures()
    {

        if (mTextureY != 0)
        {
            int[] texture = new int[3];

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            texture[0] = mTextureY;
            texture[1] = mTextureU;
            texture[2] = mTextureV;
            GLES20.glDeleteTextures(3, texture, 0);
            GLESUtils.checkGlError("FBO::glDeleteTextures(1, mTextureY)");
            mTextureY = 0;
            mTextureU = 0;
            mTextureV = 0;
        }
    }

    protected void setVertexCoordinates(float x, float y, float u, float v, int vertex)
    {
        mVertexCoordinates[vertex * 2 + 0] = x;
        mVertexCoordinates[vertex * 2 + 1] = y;
        mTextureCoordinates[vertex * 2 + 0] = u;
        mTextureCoordinates[vertex * 2 + 1] = v;
    }

    private boolean updateParameters(int rotation)
    {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
        GLESUtils.checkGlError("FBO:bind Y texture");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU);
        GLESUtils.checkGlError("FBO:bind U texture");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV);
        GLESUtils.checkGlError("FBO:bind V texture");

        if (mVideoFrame != null)
        {
            int frameRotation = mVideoFrame.getRotationAngle();

            int angle = (360 - (rotation + frameRotation)) % 360;

            if (mAnimation._lastDevAngle < 0)
            {
                mAnimation._lastDevAngle = rotation;
            }
            else if (mAnimation._lastDevAngle != rotation)
            {
                mAnimation.startDeviceRotationAnimation(rotation);
                mAnimation._lastDevAngle = rotation;
            }

            if (mAnimation._lastVideoAngle < 0)
            {
                mAnimation._lastVideoAngle = frameRotation;
            }
            else if (mAnimation._lastVideoAngle != frameRotation)
            {
                mAnimation.startVideoRotationAnimation(frameRotation);
                mAnimation._lastVideoAngle = frameRotation;
            }

            return setProgramParams(angle);
        }

        return false;
    }

    private boolean setProgramParams(int angle)
    {
        if (mWidth == 0 || mHeight == 0) {
            return false;
        }

        if (mProgramHandle == 0 && !createProgram()) {
            return false;
        }

        GLES20.glUseProgram(mProgramHandle);
        if (GLESUtils.checkGlError("YUV:render:glUseProgram")) {
            deleteProgram();
            return false;
        }

        int videoRotation = mAnimation.getCurVideoRotation();
        int deviceRotation = mAnimation.getCurDeviceRotation();

        // create new matrix
        if (isOrientationChanged(videoRotation, mVideoFrame.isMirror(), deviceRotation) || isSizeChanged == true) {
            float[] RotationDevice = new float[16];
            float[] RotationVideo = new float[16];
            float[] Mirror = new float[16];
            float[] MirrorView = new float[16];

            Matrix.setIdentityM(RotationVideo, 0);
            Matrix.rotateM(RotationVideo, 0, -videoRotation, 0, 0, 1);

            Matrix.setIdentityM(RotationDevice, 0);
            Matrix.rotateM(RotationDevice, 0, -deviceRotation, 0, 0, 1);

            Matrix.setIdentityM(Mirror, 0);
            Matrix.setIdentityM(MirrorView, 0);

            if (mVideoFrame.isMirror()) {
                Mirror[0] = -1;
            }

            if (isMirrorView) {
                MirrorView[0] = -1;
            }

            mPrevRotation = videoRotation;
            mPrevMirror = mVideoFrame.isMirror();
            mPrevDeviceRotation = deviceRotation;
            mPrevOwnerAspect = mOwnerAspect;

            LogSdk.d(TAG, "orientation result angle " + angle + " video = " + videoRotation + " device = "
                    + deviceRotation + " mirror is " + mVideoFrame.isMirror()
                    + " viewMirror is " + isMirrorView);

            Scale scale = fitRect(angle, (float) mWidth / (float) mHeight, mOwnerAspect, mFitVideo);

            float[] tmpMatrix = multiply(Mirror, RotationVideo);
            float[] tmpMatrix1 = multiply(tmpMatrix, MirrorView);
            mTransformMatrix = multiply(tmpMatrix1, RotationDevice);

            setVertexCoordinates(-1.0f * scale.wScale, -1.0f * scale.hScale, 0.0f, 1.0f, 0);
            setVertexCoordinates(-1.0f * scale.wScale, 1.0f * scale.hScale, 0.0f, 0.0f, 1);
            setVertexCoordinates(1.0f * scale.wScale, -1.0f * scale.hScale, 1.0f, 1.0f, 2);
            setVertexCoordinates(1.0f * scale.wScale, 1.0f * scale.hScale, 1.0f, 0.0f, 3);

            mVertexPositions = null;
            mVertexPositions = ByteBuffer.allocateDirect(mVertexCoordinates.length * 4).order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mVertexPositions.put(mVertexCoordinates).position(0);

            mTexturePositions = null;
            mTexturePositions = ByteBuffer.allocateDirect(mTextureCoordinates.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTexturePositions.put(mTextureCoordinates).position(0);

            isSizeChanged = false;
        }

        GLES20.glEnableVertexAttribArray(mAttPos);
        GLESUtils.checkGlError("YUV:render:glEnableVertexAttribArray:pos");

        GLES20.glEnableVertexAttribArray(mAttTexcoord);
        GLESUtils.checkGlError("YUV:render:glEnableVertexAttribArray:texcoord");

        GLES20.glVertexAttribPointer(mAttPos, 2, GLES20.GL_FLOAT, false, 0, mVertexPositions);
        GLESUtils.checkGlError("YUV:render:glVertexAttribPointer:pos");

        GLES20.glVertexAttribPointer(mAttTexcoord, 2, GLES20.GL_FLOAT, false, 0, mTexturePositions);
        GLESUtils.checkGlError("Render:glVertexAttribPointer:texcoord");

        GLES20.glUniformMatrix4fv(mUniTransform, 1, false, mTransformMatrix, 0);
        GLESUtils.checkGlError("YUV:render:glUniformMatrix4fv");
        GLES20.glUniform1i(mUniTexY, 0);
        GLESUtils.checkGlError("Render:glUniform1i");
        GLES20.glUniform1i(mUniTexU, 1);
        GLESUtils.checkGlError("Render:glUniform1i");
        GLES20.glUniform1i(mUniTexV, 2);
        GLESUtils.checkGlError("Render:glUniform1i");
        GLES20.glUniform1i(mUniCircleShape, isCircleShape);
        GLESUtils.checkGlError("Render:glUniform1i");

        return true;
    }

    float[] multiply(float[] a, float[] b)
    {
        float[] m = new float[16];

        m[0] = a[0] * b[0] + a[1] * b[4] + a[2] * b[8] + a[3] * b[12];
        m[1] = a[0] * b[1] + a[1] * b[5] + a[2] * b[9] + a[3] * b[13];
        m[2] = a[0] * b[2] + a[1] * b[6] + a[2] * b[10] + a[3] * b[14];
        m[3] = a[0] * b[3] + a[1] * b[7] + a[2] * b[11] + a[3] * b[15];
        m[4] = a[4] * b[0] + a[5] * b[4] + a[6] * b[8] + a[7] * b[12];
        m[5] = a[4] * b[1] + a[5] * b[5] + a[6] * b[9] + a[7] * b[13];
        m[6] = a[4] * b[2] + a[5] * b[6] + a[6] * b[10] + a[7] * b[14];
        m[7] = a[4] * b[3] + a[5] * b[7] + a[6] * b[11] + a[7] * b[15];
        m[8] = a[8] * b[0] + a[9] * b[4] + a[10] * b[8] + a[11] * b[12];
        m[9] = a[8] * b[1] + a[9] * b[5] + a[10] * b[9] + a[11] * b[13];
        m[10] = a[8] * b[2] + a[9] * b[6] + a[10] * b[10] + a[11] * b[14];
        m[11] = a[8] * b[3] + a[9] * b[7] + a[10] * b[11] + a[11] * b[15];
        m[12] = a[12] * b[0] + a[13] * b[4] + a[14] * b[8] + a[15] * b[12];
        m[13] = a[12] * b[1] + a[13] * b[5] + a[14] * b[9] + a[15] * b[13];
        m[14] = a[12] * b[2] + a[13] * b[6] + a[14] * b[10] + a[15] * b[14];
        m[15] = a[12] * b[3] + a[13] * b[7] + a[14] * b[11] + a[15] * b[15];

        return m;
    }

    private boolean isOrientationChanged(int devRotation, boolean isMirror, int deviceRotation)
    {
        return ( (mPrevRotation != devRotation) || (mPrevDeviceRotation != deviceRotation) || (mPrevMirror != isMirror) ||
                (mPrevOwnerAspect != mOwnerAspect));
    }

    public static Scale fitRect(int angle, float videoAspect, float backAspect, boolean fitFrame) {
        angle = (360 + angle) % 360;

        Scale scale = new Scale();
        boolean swapWxH = false;
        if (angle == 90 || angle == 270) {
            videoAspect = 1.0f / videoAspect;
            swapWxH = !swapWxH;
        }
        if (videoAspect <= 1.0f && backAspect <= 1.0f) {
            videoAspect = 1.0f / videoAspect;
            backAspect = 1.0f / backAspect;
            swapWxH = !swapWxH;
        }
        boolean fitByHeigth = (backAspect > videoAspect);
        if (fitFrame) {
            fitByHeigth = !fitByHeigth;
        }

        if (fitByHeigth) {
            scale.wScale = videoAspect / backAspect;
        } else {
            scale.hScale = backAspect / videoAspect;
        }
        if (swapWxH) {
            float tmp = scale.wScale;
            scale.wScale = scale.hScale;
            scale.hScale = tmp;
        }

        return scale;
    }

    public static class Scale
    {
        public float	hScale	= 1.0f;
        public float	wScale	= 1.0f;
    }

    private class ParamAnimation
    {
        public ParamAnimation(int start, int end)
        {
            _start = start;
            _end = end;
            _duration = 300;
            _startTime = Calendar.getInstance().getTimeInMillis();
            _animate = true;
        }

        public int getCurValue()
        {
            int res = _end;
            if (_animate)
            {
                float t = (float) (Calendar.getInstance().getTimeInMillis() - _startTime) / _duration;
                if (t >= 1.0)
                {
                    _animate = false;
                    res = _end;
                }
                else
                {
                    int delta = _end - _start;
                    res = (int) (_start + delta * Math.sin(1.57 * t));
                }
            }

            return res;
        }

        public int getEndValue()
        {
            return _end;
        }

        public Boolean completed()
        {
            return !_animate;
        }

        protected int		_start;
        protected int		_end;
        protected int		_duration;
        protected Boolean	_animate;
        protected long		_startTime;
    };

    private class AnimatedRotation
    {
        public int		_lastDevAngle	= -1;
        public int		_lastVideoAngle	= -1;
        private Boolean	_animateRotation;
        ParamAnimation	_deviceRotationAnimation;
        ParamAnimation	_videoRotationAnimation;

        public void setAnimateRotation(Boolean animate)
        {
            _animateRotation = animate;
            if (!_animateRotation)
            {
                stopDeviceRotationAnimation();
                stopVideoRotationAnimation();
            }
        }

        public boolean isCompleted()
        {
            if ((_deviceRotationAnimation == null || _deviceRotationAnimation.completed())
                    && (_videoRotationAnimation == null || _videoRotationAnimation.completed()))
                return true;
            else
                return false;
        }

        public int[] getBestRotationAngles(int startangle, int endangle)
        {
            int[] arr = new int[2];
            int a1 = startangle;
            int a2 = endangle;

            int d1 = Math.abs(a2 - a1) % 360;
            int d2 = Math.abs((360 + a2) - a1) % 360;
            int d3 = Math.abs(a2 - (360 + a1)) % 360;

            int a11, a22;
            if (d2 < d1)
            {
                if (d3 < d2) // d3
                {
                    a11 = 360 + a1;
                    a22 = a2;
                }
                else
                // d2
                {
                    a11 = a1;
                    a22 = 360 + a2;
                }
            }
            else
            {
                if (d3 < d1) // d3
                {
                    a11 = 360 + a1;
                    a22 = a2;
                }
                else
                // d1
                {
                    a11 = a1;
                    a22 = a2;
                }
            }

            arr[0] = a11;
            arr[1] = a22;
            return arr;
        }

        public void startDeviceRotationAnimation(int newRotation)
        {
            if (!_animateRotation)
                return;

            int[] arr = getBestRotationAngles(_deviceRotationAnimation != null ? _deviceRotationAnimation.getCurValue()
                    : _lastDevAngle, newRotation);

            _deviceRotationAnimation = new ParamAnimation(arr[0], arr[1]);
        }

        public int getCurDeviceRotation()
        {
            if (_deviceRotationAnimation != null)
            {
                if (!_deviceRotationAnimation.completed())
                    return _deviceRotationAnimation.getCurValue();
                else
                    stopDeviceRotationAnimation();
            }

            return _lastDevAngle;
        }

        public void stopDeviceRotationAnimation()
        {
            if (_deviceRotationAnimation != null)
            {
                _lastDevAngle = (_deviceRotationAnimation.getEndValue() % 360);
                _deviceRotationAnimation = null;
            }
        }

        public void startVideoRotationAnimation(int newRotation)
        {
            if (!_animateRotation)
                return;

            int[] arr = getBestRotationAngles(_videoRotationAnimation != null ? _videoRotationAnimation.getCurValue()
                    : _lastVideoAngle, newRotation);

            _videoRotationAnimation = new ParamAnimation(arr[0], arr[1]);
        }

        public int getCurVideoRotation()
        {
            if (_videoRotationAnimation != null)
            {
                if (!_videoRotationAnimation.completed())
                    return _videoRotationAnimation.getCurValue();
                else
                    stopVideoRotationAnimation();
            }

            return _lastVideoAngle;
        }

        public void stopVideoRotationAnimation()
        {
            if (_videoRotationAnimation != null)
            {
                _lastVideoAngle = _videoRotationAnimation.getEndValue();
                _videoRotationAnimation = null;
            }
        }
    }

    public static interface VideoAnimationListener {
        void onVideoRotationAnimation();
    }

    public void setVideoAnimationListener(VideoAnimationListener animationListener) {
        mAnimationListener = animationListener;
    }
}
