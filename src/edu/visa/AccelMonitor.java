package edu.visa; 

import java.io.IOException;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class AccelMonitor implements SensorEventListener {
	private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private Activity _a;
    private TextView t;
    private MediaPlayer drone;
    
    private float ox, oy, oz; //old x y z
    private float x, y, z;
    
    private float r1, r2, r3; //combinations of 2-dir rotations (x^2+y^2)
    private float or1, or2, or3; //
    
    private float mx, my, mz; // max change from any poll to the next 
    
	public AccelMonitor(Activity a, TextView t, MediaPlayer drone) {
		mSensorManager = (SensorManager) a.getSystemService(a.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelerometer.getMaximumRange(); //TODO something with this
        this._a = a;
        this.t = t;
        this.drone = drone;
        drone.setLooping(true);
        drone.start();
	}

	
	
	protected void onResume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //drone.reset();
/*        try {
			drone.setDataSource("android.resource://" + _a.getPackageName() + "/"+ R.raw.black_juggernaut_black_mirror);
		} catch (IOException e) {
			Toast.makeText(_a.getApplicationContext(), "Failed to reinitialize drone!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}*/
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
        drone.release();
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
		
		
		// use new data to change the drone volume
		float max = event.sensor.getMaximumRange();
		drone.setVolume(8*Math.abs(x)/max, 8*Math.abs(y)/max);
		Log.d("Accel", ""+ (x/max) + ", " + (y/max)+"" );
		
		
	}

}