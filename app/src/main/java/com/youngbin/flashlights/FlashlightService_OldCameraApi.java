package com.youngbin.flashlights;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.IBinder;

public class FlashlightService_OldCameraApi extends Service {
    private Camera camera;
    private Camera.Parameters param;
    public FlashlightService_OldCameraApi() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            stopSelf();
        }

        camera = Camera.open();
        param = camera.getParameters();

        flashLightOn();
    }

    public void flashLightOn() {
        param = camera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(param);
        camera.startPreview();

    }

    public void flashLightOff() {
        param = camera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(param);
        camera.stopPreview();
//        camera.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flashLightOff();
    }
}
