package com.exolvetechnologies.hidoctor.ui.gl;

import android.opengl.GLES20;

import com.oovoo.core.Utils.LogSdk;

/**
 * Created by oovoo on 7/29/15.
 */
public class GLESUtils {

    public static String    TAG	= GLESUtils.class.getSimpleName();

    public static void cleanGlError()
    {
        int error = GLES20.glGetError();
        while (error != GLES20.GL_NO_ERROR) {
            error = GLES20.glGetError();
        }
    }

    public static boolean checkGlError(String op)
    {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            LogSdk.e(TAG, op + ": glError " + error);
            return true;
        }
        return false;
    }

    private static int loadShader(int shaderType, String source)
    {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                LogSdk.e(TAG, "Could not compile shader " + shaderType + ":");
                LogSdk.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource)
    {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            GLES20.glDeleteShader(vertexShader);
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                LogSdk.e(TAG, "Could not link program: ");
                LogSdk.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDetachShader(program, vertexShader);
                GLES20.glDeleteShader(vertexShader);
                vertexShader = 0;
                GLES20.glDetachShader(program, pixelShader);
                GLES20.glDeleteShader(pixelShader);
                pixelShader = 0;
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle,
                                           final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                LogSdk.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }
}
