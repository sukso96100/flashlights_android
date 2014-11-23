package com.youngbin.flashlights;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=21){
            startActivity(new Intent(this, MainActivity2.class));
        }else{}

        YellowDrawable  = new ColorDrawable(Color.parseColor("#f1c40f"));
        DarkBlueDrawable = new ColorDrawable(Color.parseColor("#34495e"));


        final RelativeLayout Background = (RelativeLayout)findViewById(R.id.background);
        ImageView Flashlights = (ImageView)findViewById(R.id.flashlights);

        camera = Camera.open();
        param = camera.getParameters();

        Flashlights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFlashlightsOn){
                    isFlashlightsOn = true;
                    if(Build.VERSION.SDK_INT>=21){
                        getWindow().setStatusBarColor(Color.parseColor("#f39c12"));
                    }else{}
                    getSupportActionBar().setBackgroundDrawable(YellowDrawable);
                    Background.setBackgroundColor(Color.parseColor("#f1c40f"));
                    flashLightOn();
                }else{
                    isFlashlightsOn = false;
                    if(Build.VERSION.SDK_INT>=21){
                        getWindow().setStatusBarColor(Color.parseColor("#ff213242"));
                    }else{}
                    getSupportActionBar().setBackgroundDrawable(DarkBlueDrawable);
                    Background.setBackgroundColor(Color.parseColor("#34495e"));
                    flashLightOff();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
