package com.youngbin.flashlights;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    private boolean isFlashlightsOn = false;
    private ColorDrawable YellowDrawable;
    private ColorDrawable DarkBlueDrawable;
    private Camera camera;
    private Camera.Parameters param;
    Intent ServiceCam2;
    Intent ServiceOldCam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences Pref = getPreferences(MODE_PRIVATE);
        isFlashlightsOn = Pref.getBoolean("toggle",false);
        serviceControl(isFlashlightsOn);

        ServiceCam2 = new Intent(this, FlashlightService_Camera2Api.class);
        ServiceOldCam = new Intent(this, FlashlightService_OldCameraApi.class);

        YellowDrawable  = new ColorDrawable(Color.parseColor("#f1c40f"));
        DarkBlueDrawable = new ColorDrawable(Color.parseColor("#34495e"));


        final RelativeLayout Background = (RelativeLayout)findViewById(R.id.background);
        ImageView Flashlights = (ImageView)findViewById(R.id.flashlights);

        camera = Camera.open();
        param = camera.getParameters();

        Flashlights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFlashlightsOn){
                    isFlashlightsOn = false;
                    serviceControl(false);
                }else{
                    isFlashlightsOn = true;
                    serviceControl(true);
                }
            }
        });
    }


    private void serviceControl(boolean Toggle){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            if(Toggle){
//                stopService(ServiceCam2);
                startService(ServiceCam2);
            }else{
                try{
                    stopService(ServiceCam2);
                }catch (Exception e){}
            }
        }else{
            if(Toggle){
//                stopService(ServiceOldCam);
                startService(ServiceOldCam);
            }else{
                try {
                    stopService(ServiceOldCam);
                }catch (Exception e){}
            }
        }
    }

}
