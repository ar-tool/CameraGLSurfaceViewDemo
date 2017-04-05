package hiar.openglcamerademo1.camera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.util.List;

/**
 * Created by DIY on 2017/4/5.
 */

public class CameraInstance implements Camera.PreviewCallback {
    private Camera camera;
    private int preWidth = -1;
    private int preHeight = -1;
    private boolean isPreviewing;
    private String mFocusMode;
    private SurfaceTexture surfaceTexture;

    private static CameraInstance mInstance;
    private CameraInstance(){
        preWidth = 1280;
        preHeight = 720;
        isPreviewing = false;
        mFocusMode =Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
    }
    public static CameraInstance getInstance(){
        if (null == mInstance){
            mInstance = new CameraInstance();
        }
        return mInstance;
    }

    //获取前置相机
    private int getFrontCameraIndex(){
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();//获取摄像头数量
        for (int camIndex = 0;camIndex<cameraCount;camIndex++){
            Camera.getCameraInfo(camIndex,info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                return camIndex;
            }
        }
        return 0;
    }

    private int getBackCameraIndex(){
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int cameraIndex = 0 ;cameraIndex<cameraCount ;cameraIndex++){
            Camera.getCameraInfo(cameraIndex,info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                return cameraIndex;
            }
        }
        return 0;
    }

    public final static int CAMERA_DIRECTION_DEFAULT = 0;
    public final static int CAMERA_DIRECTION_BACK = 1;
    public final static int CAMERA_DIRECTION_FRONT = 2;

    public int openCamera(int direction){
        if(null != camera){
            camera.release();
            camera = null;
        }
        try {
            switch (direction){
                case CAMERA_DIRECTION_DEFAULT:
                    camera = Camera.open();
                    break;
                case CAMERA_DIRECTION_BACK :
                    camera = Camera.open(getBackCameraIndex());
                    break;
                case CAMERA_DIRECTION_FRONT:
                    camera = Camera.open(getFrontCameraIndex());
                    break;
                default:
                    camera = Camera.open();
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public List<Camera.Size> getSupportSizes(){
        if(null ==camera) return null;
        return camera.getParameters().getSupportedPreviewSizes();
    }

    public int getPreviewWidth(){
        return preWidth;
    }
    public int getPreHeight(){
        return preHeight;
    }

    public void setPreviewSize(int wid, int height){
        preHeight = height;
        preWidth = wid;
    }

    public boolean isPreviewing(){
        return isPreviewing;
    }

    public void startPreview(){
        if(camera == null||isPreviewing) return;
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(preWidth,preHeight);
            parameters.setPreviewFormat(ImageFormat.NV21);
            List<String> modes = parameters.getSupportedFocusModes();
            if(modes.contains(mFocusMode)){
                parameters.setFocusMode(mFocusMode);
            }
            camera.setParameters(parameters);
            surfaceTexture = new SurfaceTexture(-1);
            camera.setPreviewTexture(surfaceTexture);
            Camera.Parameters paras = camera.getParameters();
            int pixelformat = paras.getPreviewFormat();
            PixelFormat pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(pixelformat,pixelFormat);
            int buffSize = preWidth*preHeight*pixelFormat.bitsPerPixel / 8;
            for (int i =0;i<5;i++){
                camera.addCallbackBuffer(new byte[buffSize]);
            }
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
            isPreviewing = true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopPreview(){
        if(null !=camera){
            camera.stopPreview();
            isPreviewing = false;
        }
    }
    public void closeCamera(){
        if(isPreviewing == true){
            stopPreview();
        }
        if(null !=camera){
            camera.setPreviewCallbackWithBuffer(null);
            camera.release();
            camera = null;
        }
        if(surfaceTexture != null){
            surfaceTexture.release();
            surfaceTexture =null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
            camera.addCallbackBuffer(data);
    }


    public void setFlash() {
        if (camera == null) return;
        Camera.Parameters params = camera.getParameters();
        String flashMode = params.getFlashMode();
        if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            params.setFlashMode(Camera.Parameters.ANTIBANDING_OFF);
        }
        camera.setParameters(params);
    }
}
