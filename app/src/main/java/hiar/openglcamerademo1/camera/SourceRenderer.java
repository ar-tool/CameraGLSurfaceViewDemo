package hiar.openglcamerademo1.camera;

import android.opengl.Matrix;


import java.nio.Buffer;

/**
 * Created by li on 2016/11/23.
 */

class SourceRenderer {

    protected static int screenWidth = -1;
    protected static int screenHeight = -1;
    protected int previewWidth = -1;
    protected int previewHeight = -1;
    protected int deviceOrientation = -1;// 0: Portrait , 1: LandLeft, 2: UpSideDown,3: LandRight
    protected int cameraOrientation = -1;
    protected float[] mvpMatrix = new float[16];

    protected Buffer quadVertices, quadTexCoords, quadIndices;

    private boolean texCoordsInit = false;
    SourceRenderer(){
        quadVertices = OpenglUtils.makeDoubleBuffer(quadVerticesArray);
        quadIndices = OpenglUtils.makeByteBuffer(quadIndicesArray);
        quadTexCoords = OpenglUtils.makeDoubleBuffer(quadTexCoordsArray);
    }

    public void setPictureSize(int preWidth, int preHeight) {
        previewWidth = preWidth;
        previewHeight = preHeight;
        configTexCoords();
    }

    public void configScreen(int sWidth, int sHeight) {
        texCoordsInit = false;
        screenWidth = sWidth;
        screenHeight = sHeight;
        configTexCoords();
    }

    public void setCameraOrientation(int orientation){
        cameraOrientation = orientation;
        configMVPMatrix();
    }

    private void configMVPMatrix(){
        int degree = 0;
        if(deviceOrientation < 0)return;
        degree = (deviceOrientation+3)%4 * 90;

        if(cameraOrientation > 0){
            degree += (cameraOrientation - 90);
        }

        Matrix.setIdentityM(mvpMatrix,0);
        Matrix.rotateM(mvpMatrix,0,degree,0,0,1);
    }

    public void setDeviceOrientation(int orientation){
        deviceOrientation = orientation;
        configMVPMatrix();
    };

    protected double quadVerticesArray[] = {-1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f};

    protected double quadTexCoordsArray[] = {0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f};

    protected short quadIndicesArray[] = {0, 1, 2, 2, 3, 0};

    protected void configTexCoords(){
        if(texCoordsInit)return;
        /*
		 * Since the ratio of screen and preview is different, we should clip
		 * some part of the preview in order to get right
		 */
        if(previewWidth == -1||previewHeight== -1||screenHeight==-1||screenWidth==-1)return;

        if(deviceOrientation<0){
            Matrix.setIdentityM(mvpMatrix,0);
            if(screenWidth < screenHeight){
                Matrix.rotateM(mvpMatrix,0,270,0,0,1);
            }
        }
        quadTexCoordsArray[0] = 0.0f;
        quadTexCoordsArray[1] = 1.0f;
        quadTexCoordsArray[2] = 1.0f;
        quadTexCoordsArray[3] = 1.0f;
        quadTexCoordsArray[4] = 1.0f;
        quadTexCoordsArray[5] = 0.0f;
        quadTexCoordsArray[6] = 0.0f;
        quadTexCoordsArray[7] = 0.0f;
        int sWidth = screenWidth > screenHeight ? screenWidth : screenHeight;
        int sHeight = screenWidth > screenHeight ? screenHeight : screenWidth;
        float screenRatio = sWidth * 1.0f / sHeight;
        float previewRatio = previewWidth * 1.0f / previewHeight;
        if (screenRatio > previewRatio) {
            //将屏幕高度与宽度的比例转为预览的比例
            float preHeight = sWidth * previewHeight * 1.0f / previewWidth;
            float ratio = sHeight / preHeight;
            quadTexCoordsArray[1] = ratio / 2 + 0.5f;
            quadTexCoordsArray[3] = ratio / 2 + 0.5f;
            quadTexCoordsArray[5] = (1 - ratio) / 2.0f;
            quadTexCoordsArray[7] = (1 - ratio) / 2.0f;
        } else if (screenRatio < previewRatio) {
            float preWidth = (sHeight * previewWidth * 1.0f) / previewHeight;
            float ratio = sWidth * 1.0f / preWidth;
                quadTexCoordsArray[0] = (1 - ratio) / 2.0f;
                quadTexCoordsArray[2] = ratio / 2 + 0.5f;
                quadTexCoordsArray[4] = ratio / 2 + 0.5f;
                quadTexCoordsArray[6] = (1 - ratio) / 2.0f;
        }
        quadTexCoords = OpenglUtils.makeDoubleBuffer(quadTexCoordsArray);
        texCoordsInit = true;
    }
}
