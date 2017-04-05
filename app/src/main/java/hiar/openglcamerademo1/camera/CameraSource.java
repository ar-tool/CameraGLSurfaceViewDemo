package hiar.openglcamerademo1.camera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.List;

/**
 * Created by li on 2016/9/29.
 */

public class CameraSource implements Camera.PreviewCallback {
    private Camera camera;
    private int preWidth = -1;
    private int preHeight = -1;
    private boolean isPreviewing;
    private String mFocusMode;
    private SurfaceTexture surfaceTexture;


    private static CameraSource _instance = new CameraSource();

    public static CameraSource Instance() {
        return _instance;
    }

    CameraSource() {
        preWidth = 1280;
        preHeight = 720;
        isPreviewing = false;
        mFocusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
    }


    private int getFrontCameraIndex() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx;
            }
        }
        return 0;
    }

    private int getBackCameraIndex() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return camIdx;
            }
        }
        return 0;
    }

    public final static int CAMERA_DIRECTION_DEFAULT = 0;
    public final static int CAMERA_DIRECTION_BACK = 1;
    public final static int CAMERA_DIRECTION_FRONT = 2;

    public int openCamera(int direction) {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        try {
            switch (direction) {
                case CAMERA_DIRECTION_DEFAULT:
                    camera = Camera.open();
                    break;
                case CAMERA_DIRECTION_BACK:
                    camera = Camera.open(getBackCameraIndex());
                    break;
                case CAMERA_DIRECTION_FRONT:
                    camera = Camera.open(getFrontCameraIndex());
                    break;
                default:
                    camera = Camera.open();
                    break;
            }


        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    public List<Size> getSupportsSizes() {
        if (camera == null) return null;
        return camera.getParameters().getSupportedPreviewSizes();
    }

    public int getPreviewWidth() {
        return preWidth;
    }

    public int getPreviewHeight() {
        return preHeight;
    }

    public int setPreviewSize(int width, int height) {
        preWidth = width;
        preHeight = height;
        return 0;
    }


    public boolean isPreviewing() {
        return isPreviewing;
    }

    public int startPreview() {
        if (camera == null) return -1;
        if (isPreviewing) return 0;
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(preWidth, preHeight);
            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.getSupportedPreviewFpsRange();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(mFocusMode)) {
                parameters.setFocusMode(mFocusMode);
            }
            camera.setParameters(parameters);
            surfaceTexture = new SurfaceTexture(-1);
            camera.setPreviewTexture(surfaceTexture);
            Camera.Parameters para = camera.getParameters();
            int pixelformat = para.getPreviewFormat();
            PixelFormat pixelinfo = new PixelFormat();
            PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo);
                int bufSize = preWidth * preHeight * pixelinfo.bitsPerPixel / 8;
                for (int i = 0; i < 5; i++) {
                    camera.addCallbackBuffer(new byte[bufSize]);
                }
            camera.setPreviewCallbackWithBuffer(this);
            System.gc();
            camera.startPreview();
            isPreviewing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            isPreviewing = false;
        }
        return 0;
    }

    public int closeCamera() {
        if (isPreviewing) {
            stopPreview();
        }
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.release();
            camera = null;
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        return 0;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        RendererController.getController().onFrame(data,preWidth,preHeight);
        camera.addCallbackBuffer(data);
    }

    public int setFlashTorchMode(boolean on) {
        if (camera == null) return -1;
        Camera.Parameters parameters = camera.getParameters();
        if (on) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//
        }
        camera.setParameters(parameters);
        return 0;
    }

    public static final int FOCUS_MODE_NORMAL = 0;           ///< Default focus mode
    public static final int FOCUS_MODE_TRIGGERAUTO = 1;      ///< Triggers a single autofocus operation
    public static final int FOCUS_MODE_CONTINUOUSAUTO = 2;   ///< Continuous autofocus mode
    public static final int FOCUS_MODE_INFINITY = 3;         ///< Focus set to infinity
    public static final int FOCUS_MODE_MACRO = 4;             ///< Macro mode for close-up focus

    public int setFocusMode(int mode) {
        if (camera == null) return -1;
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        switch (mode) {
            case 0:
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                break;
            case 1:
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                break;
            case 2:
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                break;
            case 3:
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                break;
            case 4:
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO))
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                break;
            default:
                break;
        }
        camera.setParameters(parameters);
        return 0;
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
