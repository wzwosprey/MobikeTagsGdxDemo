package com.wzwosprey.mobiketagsgdxdemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MobikeDemo extends AppCompatActivity {

    private MobikeView mobikeView;
    private SensorManager sensorManager;
    private Sensor defaultSensor;

    private int[] imgs = {
            R.mipmap.share_fb,
            R.mipmap.share_kongjian,
            R.mipmap.share_pyq,
            R.mipmap.share_qq,
            R.mipmap.share_tw,
            R.mipmap.share_wechat,
            R.mipmap.share_weibo,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobike_demo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("摩拜单车Demo");
        mobikeView = (MobikeView) findViewById(R.id.mobike_view);

        initViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        defaultSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initViews() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        for(int i = 0; i < imgs.length  ; i ++){
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(imgs[i]);
            imageView.setTag(R.id.mobike_view_circle_tag,true);
            mobikeView.addView(imageView,layoutParams);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mobikeView.getmMobike().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mobikeView.getmMobike().onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listerner, defaultSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listerner);
    }

    private SensorEventListener listerner = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float x =  event.values[0];
                float y =   event.values[1] * 2.0f;
                mobikeView.getmMobike().onSensorChanged(-x,y);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
