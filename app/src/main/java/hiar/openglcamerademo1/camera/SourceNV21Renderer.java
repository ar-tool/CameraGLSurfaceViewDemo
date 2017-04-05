package hiar.openglcamerademo1.camera;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by li on 2016/9/29.
 */

class SourceNV21Renderer extends SourceRenderer{
    /*
     * camera shader handles
     */
    private int cameraShaderID = 0;
    private int cameraVertexHandle = 0;
    private int cameraTexCoordHandle = 0;
    private int cameraYUniformHandle = 0;
    private int cameraUVUniformHandle = 0;
    private int cameraMVPMatrixHandle = 0;

    /*
     * Video background texture ids
     */
    private int cameraTextureYID;
    private int cameraTextureUVID;

    private final int NUM_QUAD_INDEX = 6;

    public ByteBuffer frameRenderBuffer = null;

    private String VERTEX_SHADER = "attribute vec4 vertexPosition;\n" +
            "attribute vec2 vertexTexCoord;\n" +
            "varying vec2 texCoord;\n" +
            "uniform mat4 modelViewProjectionMatrix;\n" +
            "void main() {\n" +
            "gl_Position = modelViewProjectionMatrix * vertexPosition;\n" +
            "texCoord = vertexTexCoord;\n" +
            "}";
    private String FRAGMENT_SHADER = "uniform sampler2D videoFrameY;\n" +
            "uniform sampler2D videoFrameUV;\n" +
            "varying lowp vec2 texCoord;\n" +
            "const lowp mat3 M = mat3( 1, 1, 1, 0, -.18732, 1.8556, 1.57481, -.46813, 0 );\n" +
            "void main() { \n" +
            "lowp vec3 yuv; \n" +
            "lowp vec3 rgb; \n" +
            "yuv.x = texture2D(videoFrameY, texCoord).r;\n" +
            "yuv.yz = texture2D(videoFrameUV, texCoord).ar - vec2(0.5, 0.5);\n" +
            "rgb = M * yuv;\n" +
            "gl_FragColor = vec4(rgb,1.0);\n" +
            "}";

    SourceNV21Renderer() {
        initCameraRendering();
    }

    private boolean textureInit = false;
    private void initializeTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, previewWidth, previewHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(4 * (previewWidth / 2) * (previewHeight / 2));
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, previewWidth / 2,
                previewHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE,
                frameRenderBuffer);

    }

    private void initFrameRenderBuffer(int size) {
        frameRenderBuffer = OpenglUtils.makeByteBuffer(size * 6);
        bwSize = size;
        bufferInit = true;
    }

    private int bwSize;
    private boolean bufferInit = false;
    private boolean isReady = false;
    private byte[] nv21Data;
    public ReentrantLock frameLock = new ReentrantLock();

    public void setNV21Data(byte[] data, int width, int height) {
        frameLock.lock();
            nv21Data = data.clone();
            if(!bufferInit){
                initFrameRenderBuffer((width/2)*(height/2));
            }
            isReady = true;
        frameLock.unlock();
    }

    public void putRenderBuffer(){
        frameLock.lock();
            frameRenderBuffer.position(0);
            if (nv21Data.length <= bwSize * 6) {
                frameRenderBuffer.put(nv21Data);
            }
            frameRenderBuffer.position(0);
        frameLock.unlock();
    }

    void initCameraRendering() {

        int[] textureNames = new int[2];

        GLES20.glGenTextures(1, textureNames, 0);
        cameraTextureYID = textureNames[0];

        GLES20.glGenTextures(1, textureNames, 1);
        cameraTextureUVID = textureNames[1];

        cameraShaderID = OpenglUtils.createProgramFromShaderSrc(VERTEX_SHADER, FRAGMENT_SHADER);
        cameraVertexHandle = GLES20.glGetAttribLocation(cameraShaderID, "vertexPosition");
        cameraTexCoordHandle = GLES20.glGetAttribLocation(cameraShaderID, "vertexTexCoord");
        cameraYUniformHandle = GLES20.glGetUniformLocation(cameraShaderID, "videoFrameY");
        cameraUVUniformHandle = GLES20.glGetUniformLocation(cameraShaderID, "videoFrameUV");
        cameraMVPMatrixHandle = GLES20.glGetUniformLocation(cameraShaderID, "modelViewProjectionMatrix");
    }

    /*
     *
     * To seperate video background drawing.
     */
    public final void draw() {
        if (!isReady) {
            return;
        }
        if(!textureInit){
            initializeTexture();
            textureInit = true;
        }
        putRenderBuffer();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(0);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, previewWidth, previewHeight,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(4 * (previewWidth / 2) * (previewHeight / 2));
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, previewWidth / 2, previewHeight / 2,
                GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);

        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        GLES20.glUseProgram(cameraShaderID);

        GLES20.glVertexAttribPointer(cameraVertexHandle, 3, GLES20.GL_FLOAT, false, 0, quadVertices);
        GLES20.glVertexAttribPointer(cameraTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, quadTexCoords);

        GLES20.glEnableVertexAttribArray(cameraVertexHandle);
        GLES20.glEnableVertexAttribArray(cameraTexCoordHandle);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);

        GLES20.glUniform1i(cameraYUniformHandle, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);

        GLES20.glUniform1i(cameraUVUniformHandle, 1);
        GLES20.glUniformMatrix4fv(cameraMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, NUM_QUAD_INDEX, GLES20.GL_UNSIGNED_SHORT, quadIndices);

        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(cameraVertexHandle);
        GLES20.glDisableVertexAttribArray(cameraTexCoordHandle);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

}
