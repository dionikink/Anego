/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package org.g29.anego2.application.ar;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import org.g29.anego2.application.ApplicationSession;
import org.g29.anego2.data.models.CubeShaders;
import org.g29.anego2.data.models.LoadingDialogHandler;
import org.g29.anego2.data.models.MeshObject;
import org.g29.anego2.data.models.Texture;
import org.g29.anego2.data.utils.ParseUtils;
import org.g29.anego2.data.utils.RenderUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the ImageTargets sample. 
public class AnegoRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "AnegoRenderer";
    
    private ApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;
    
    private Vector<Texture> mTextures;
    
    private int shaderProgramID;
    
    private int vertexHandle;
    
    private int normalHandle;
    
    private int textureCoordHandle;
    
    private int mvpMatrixHandle;
    
    private int texSampler2DHandle;

    private Map<String, MeshObject> mObjects;

    private Renderer mRenderer;
    
    boolean mIsActive = false;
    
    private static final float OBJECT_SCALE_FLOAT = 3.0f;

    // View width and height used to take a screenshot
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    
    
    public AnegoRenderer(ImageTargets activity,
                         ApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        mObjects = new HashMap<>();
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();

        GLES20.glFinish();

        if (ImageTargets.SCREENSHOT) {
            ImageTargets.SCREENSHOT = false;

            String uniqueStamp = "" + System.nanoTime();
            String fileName = new SimpleDateFormat("'Anego-'yyyyMMddhhmm'" + uniqueStamp.substring(uniqueStamp.length() - 2) + ".jpg'").format(System.nanoTime());
            saveScreenShot(0, 0, mViewWidth, mViewHeight, fileName);
        }
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        mViewWidth = width;
        mViewHeight = height;

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        mRenderer = Renderer.getInstance();
        String filename = "";

        try {
            filename = "objects/house.anego";
            InputStream in = mActivity.getAssets().open(filename);
            mObjects.put("chips", ParseUtils.ParseObject(new MeshObject(), in));
        } catch (IOException e) {
            Log.e("ParseUtils", "Could not open file " + filename);
        }

        try {
            filename = "objects/tree.anego";
            InputStream in = mActivity.getAssets().open(filename);
            mObjects.put("stones", ParseUtils.ParseObject(new MeshObject(), in));
        } catch (IOException e) {
            Log.e("ParseUtils", "Could not open file " + filename);
        }

        
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = RenderUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

    }


    // The render function.
    private void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
            
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();

            MeshObject mObject = mObjects.get(trackable.getName());

            Log.e(LOGTAG, trackable.getName() + " " + tIdx);
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            
            int textureIndex = 0;
            
            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                    OBJECT_SCALE_FLOAT);

            float angle = 90.0f;
            Matrix.rotateM(modelViewMatrix, 0, angle, 1.0f, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, angle, 0, 1.0f, 0);

            Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT * 2,
                OBJECT_SCALE_FLOAT * 2, OBJECT_SCALE_FLOAT * 2);

            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);

            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, mObject.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, mObject.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, mObject.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            // activate texture 0, bind it, and pass to shader
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(textureIndex).mTextureID[0]);
            GLES20.glUniform1i(texSampler2DHandle, 0);

            // pass the model view matrix to the shader
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);

            // finally draw the teapot
            if(ImageTargets.MODELING) {
                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                        mObject.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                        mObject.getIndices());
            }

            // disable the enabled arrays
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);


            RenderUtils.checkGLError("Render Frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        mRenderer.end();
    }
    
    
    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retrieved User Data	\"" + userData + "\"");
    }
    
    
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
    }

    private void saveScreenShot(int x, int y, int w, int h, String filename) {
        Bitmap bmp = grabPixels(x, y, w, h);
        try {
            String directory = Environment.getExternalStorageDirectory() + "/Anego/Screenshots";
            File fileDirectory = new File(directory);

            if(!fileDirectory.exists()) {
                fileDirectory.mkdir();
            }

            String path = directory + "/" + filename;
            Log.v(LOGTAG, path);

            File file = new File(path);
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();

            fos.close();

        } catch (Exception e) {
            Log.e(LOGTAG, Log.getStackTraceString(e));
        }
    }

    private Bitmap grabPixels(int x, int y, int w, int h) {
        int b[] = new int[w * (y + h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);

        GLES20.glReadPixels(x, 0, w, y + h,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

        for (int i = 0, k = 0; i < h; i++, k++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }

        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
    }
    
}
