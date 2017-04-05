package hiar.openglcamerademo1.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

/**
 * Created by yi on 2017/4/06.
 */

public class Util {

    private static Util _instance = new Util();

    Util() {
    }

    public static Util Instance() {
        return _instance;
    }


    private int deviceOrientation = -1;
    private int cameraOrientation = -1;

    public void configOrientation(Activity activity) {
        deviceOrientation = activity.getWindowManager().getDefaultDisplay().getRotation();
        RendererController.getController().setDeviceOrientation(deviceOrientation);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            CameraManager manager = (CameraManager) activity
                    .getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics("0");
                //TODO add front camera config
                cameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                // Sensor orientation is 90 for most devices, or 270 for some
                // devices
                // (eg. Nexus 5X)
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            cameraOrientation = 90;
        }
        RendererController.getController().setCameraOrientation(cameraOrientation);
    }
}
