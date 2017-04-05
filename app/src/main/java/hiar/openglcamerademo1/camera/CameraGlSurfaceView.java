package hiar.openglcamerademo1.camera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by DIY on 2017/4/5.
 */

public class CameraGlSurfaceView extends GLSurfaceView {
    private int preWidth ;
    private int preHeight;
    public CameraGlSurfaceView(Context context) {
        super(context);
        preWidth = 1280;
        preHeight = 720;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,24,8);
        setRenderer(new MyRenderer());
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); //主动渲染
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        CameraSource.Instance().openCamera(CameraInstance.CAMERA_DIRECTION_BACK);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        CameraSource.Instance().stopPreview();
        CameraSource.Instance().closeCamera();
    }

    private class MyRenderer implements Renderer{
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            RendererController.getController().init();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
           RendererController.getController().configScreen(width,height);
            //获取预览尺寸
            CameraSource.Instance().setPreviewSize(preWidth,preHeight);
            CameraSource.Instance().startPreview();
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClearColor(0,0,0,1);
            GLES20.glDepthMask(true);
            GLES20.glColorMask(true,true,true,true);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            RendererController.getController().drawVideoBackground();
            GLES20.glFinish();
        }
    }
}
