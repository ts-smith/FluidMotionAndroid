package com.example.updatetest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class GravityListener implements SensorEventListener{
	static GravityListener instance;
	MainActivity mainActivity;
	
	public GravityListener(){
		instance = this;
		mainActivity = MainActivity.getSharedInstance();
	}
	public static GravityListener getSharedInstance(){
		if (instance ==  null){
			instance = new GravityListener();
		}
		return instance;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
				mainActivity.gravitySenseX = event.values[0];
				mainActivity.gravitySenseY = event.values[1];
			}
		}
		
	}

}
