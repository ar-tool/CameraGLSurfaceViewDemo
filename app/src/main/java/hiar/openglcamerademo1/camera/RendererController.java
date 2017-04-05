package hiar.openglcamerademo1.camera;

/**
 * Created by DIY on 2017/4/5.
 */

public class RendererController {
    private static RendererController mController;
    private SourceNV21Renderer mSourceNV21Renderer;
    private int deviceOrientation = -1;
    private int cameraOrientation = -1;

    private RendererController(){
    }
    public static RendererController getController(){
        if(null == mController){
            mController = new RendererController();
        }
        return mController;
    }

    public void init(){
        mSourceNV21Renderer = new SourceNV21Renderer();
        if (deviceOrientation == -1){
            mSourceNV21Renderer.setDeviceOrientation(deviceOrientation);
        }
        if(cameraOrientation == -1){
            mSourceNV21Renderer.setCameraOrientation(cameraOrientation);
        }
    }

    public void configScreen(int width,int height){
        synchronized (mSourceNV21Renderer){
            mSourceNV21Renderer.configScreen(width,height);
        }
    }
    public void setDeviceOrientation(int orientation){
        deviceOrientation = orientation;
        if (null !=mSourceNV21Renderer){
            mSourceNV21Renderer.setDeviceOrientation(orientation);
        }
    }
    public void setCameraOrientation(int orientation){
        cameraOrientation = orientation;
        if(null != mSourceNV21Renderer){
            mSourceNV21Renderer.setCameraOrientation(orientation);
        }
    }
    public void drawVideoBackground(){
        synchronized (mSourceNV21Renderer){
            mSourceNV21Renderer.draw();
        }
    }
    public void onFrame(byte[] data,int width,int height){
        synchronized (mSourceNV21Renderer){
            mSourceNV21Renderer.setPictureSize(width,height);
            mSourceNV21Renderer.setNV21Data(data,width,height);
        }
    }
}
