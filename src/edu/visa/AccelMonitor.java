package edu.visa; 

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class AccelMonitor implements SensorEventListener {
	private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private TextView t;
    
    private float ox, oy, oz; //old x y z
    private float x, y, z;
    
    private float r1, r2, r3; //combinations of 2-dir rotations (x^2+y^2)
    private float or1, or2, or3; //
    
    private float mx, my, mz; // max change from any poll to the next 
    
	public AccelMonitor(Activity a, TextView t) {
		mSensorManager = (SensorManager) a.getSystemService(a.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelerometer.getMaximumRange(); //TODO something with this
        this.t = t;
	}

	
	
	protected void onResume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		ox = x; oy = y; oz = z;
		x = event.values[0]; y = event.values[1]; z = event.values[2];
		
		or1 = r1; or2 = r2; or3 = r3;
		r1 = x*x + y*y; r2 = x*x+z*z; r3 = y*y+z*z;
		
		
		if ( Math.abs(x - ox) > mx) mx = Math.abs(x-ox);
		if (Math.abs(y-oy) > my) my = Math.abs(y-oy);
		if (Math.abs(z-oz) > mz) mz = Math.abs(z-oz);
		
		
		t.setText(
				ox +"\t" + oy + "\t" + oz + "\n" +
				x +"\t" + y + "\t" + z + "\n" +
				(x-ox) +"\t" + (y-oy) + "\t" + (z-oz) + "\n" +
				mx + "\t" + my + "\t" + mz + "\n" +
				r1 +"\t" + r2 + "\t" + r3 + "\n" +
				(r1-or1) +"\t" + (r2-or2) + "\t" + (r3-or3) + "\n"
		);
		
	}

}