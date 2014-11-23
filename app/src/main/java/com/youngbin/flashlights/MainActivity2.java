package com.youngbin.flashlights;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity2 extends ActionBarActivity {
    /**
     * Manages the flashlight.
     */
    private static final String TAG = "FlashlightController";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final int DISPATCH_ERROR = 0;
    private static final int DISPATCH_OFF = 1;
    private static final int DISPATCH_AVAILABILITY_CHANGED = 2;
    private CameraManager mCameraManager;
    //        /** Call {@link #ensureHandler()} before using */
    private Handler mHandler;
    /** Lock on mListeners when accessing */
//        private final ArrayList<WeakReference<FlashlightListener>> mListeners = new ArrayList<>(1);
    /**
     * Lock on {@code this} when accessing
     */
    private boolean mFlashlightEnabled;
    private String mCameraId;
    private boolean mCameraAvailable;
    private CameraDevice mCameraDevice;
    private CaptureRequest mFlashlightRequest;
    private CameraCaptureSession mSession;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private Context mContext;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;


    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {

        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBackgroundThread();
        mContext = MainActivity2.this;


        ImageView Flashlights = (ImageView)findViewById(R.id.flashlights);
        Flashlights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFlashlightEnabled){
                    controlFlashlights(false);
                }else{
                    controlFlashlights(true);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //Method that returns id of camera that has flash
    private String getCameraId() throws CameraAccessException {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    //Get Camera and open it
    private void getCam() {

        try {
            mCameraId = getCameraId();
            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mCameraManager.registerAvailabilityCallback();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Method that controls flashlights
    private void controlFlashlights(boolean FlashlightSwitch){
        if(FlashlightSwitch) {
            getCam();
            CaptureRequest.Builder builder = null;
            try {
                builder = mCameraDevice.createCaptureRequest( //ERROR
                        CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            if (builder != null) {
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                builder.addTarget(mSurface);
                CaptureRequest request = builder.build();
                try {
                    mSession.capture(request, null, mBackgroundHandler);
                    mFlashlightRequest = request;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            mFlashlightEnabled = true;
        }else{
            mCameraDevice.close();
            teardown();
            mFlashlightEnabled = false;
        }
    }
    //make everything null
    private void teardown() {
        mCameraDevice = null;
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
    public void onStop(){
        super.onStop();
        stopBackgroundThread();
    }
}
