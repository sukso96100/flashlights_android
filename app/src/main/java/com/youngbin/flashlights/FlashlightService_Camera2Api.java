package com.youngbin.flashlights;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
@TargetApi(Build.VERSION_CODES.LOLLIPOP)

public class FlashlightService_Camera2Api extends Service {

    private String TAG = "FlashlightService_Lollipop";
    private CameraManager mCamManager;
    private CameraDevice mCamDevice;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private CameraCaptureSession mSession;
    private boolean isFlashlightEnabled = false;
    private CaptureRequest mFlashlightRequest;

    public FlashlightService_Camera2Api() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            stopSelf();
        }
        try {
            openCamera();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() throws CameraAccessException {
        Log.d(TAG, "openCamera");
        //Get CameraManager
        mCamManager = (CameraManager)getSystemService(CAMERA_SERVICE);
        mCamManager.openCamera("0",new CameraDevice.StateCallback() { //Open Camera
            @Override
            public void onOpened(CameraDevice camera) {
                Log.d(TAG, "CameraDevice.StateCallback() - onOpened");
                mCamDevice = camera;

                try {
                    startCaptureSession();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                Log.d(TAG, "CameraDevice.StateCallback() - onDisconnected");
//                mCamDevice.close();
                mCamDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                Log.d(TAG, "CameraDevice.StateCallback() - onError");
//                mCamDevice.close();
                mCamDevice = null;
                stopSelf();

            }
        },null);
    }

    private  void startCaptureSession() throws CameraAccessException {
        Log.d(TAG, "startCaptureSession");

        CameraCharacteristics characteristics = mCamManager.getCameraCharacteristics("0");
        StreamConfigurationMap configs =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = configs.getOutputSizes(SurfaceTexture.class);

        if (sizes == null || sizes.length == 0) {
            throw new IllegalStateException(
                    "Camera " + "0" + "doesn't support any outputSize.");
        }
        Size chosen = sizes[0];
        for (Size s : sizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }

        mSurfaceTexture = new SurfaceTexture(0,false);
        mSurfaceTexture.setDefaultBufferSize(chosen.getWidth(), chosen.getHeight());
        mSurface = new Surface(mSurfaceTexture);
        ArrayList<Surface> outputs = new ArrayList<>(1);
        outputs.add(mSurface);

        if(mCamDevice==null){
//            openCamera();
        }else {
            mCamDevice.createCaptureSession(outputs,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Log.d(TAG, "CameraCaptureSession.StateCallback() - onConfigured");
                            mSession = session;

                            try {
                                isFlashlightEnabled = true;
                                controlFlashlight(true);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "CameraCaptureSession.StateCallback() - onConfigureFailed");
                        }
                    }, null);
        }
    }

    private void controlFlashlight(boolean flashlightSwitch) throws CameraAccessException {
        boolean enabled = flashlightSwitch && isFlashlightEnabled;
        if(mCamDevice == null | mSession == null){
            openCamera();
        }
        if(enabled){
            if (mFlashlightRequest == null) {
                CaptureRequest.Builder builder = mCamDevice.createCaptureRequest(
                        CameraDevice.TEMPLATE_PREVIEW);
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                builder.addTarget(mSurface);
                CaptureRequest request = builder.build();
                mSession.capture(request, null, null);
                mFlashlightRequest = request;
            }
        } else {
            if (mCamDevice != null) {
                mCamDevice.close();
                teardown();
            }

        }

    }

    private void teardown() {
        mCamDevice = null;
        mSession = null;
        mFlashlightRequest = null;
        if (mSurface != null) {
            mSurface.release();
            mSurfaceTexture.release();
        }
        mSurface = null;
        mSurfaceTexture = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            isFlashlightEnabled = false;
            controlFlashlight(false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
