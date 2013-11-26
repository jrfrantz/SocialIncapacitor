package edu.visa;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import edu.visa.AudioBufferManager.BufferCallBack;

public class MainActivity extends Activity implements BufferCallBack {
	public static int AudioSessionID; //compatibility w github thing
	static boolean started = false;
	boolean mShowingBack = false;
	static int DELAY_TIME = 150;
	Audio a;
	AudioBufferManager audiosource;
	MediaPlayer drone; //plays drone audio
	AccelMonitor accelmon;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	audiosource = new AudioBufferManager(DELAY_TIME, this);
    	audiosource.start();
    	
    	TextView tv = (TextView) findViewById(R.id.hellobox);
    	
    	drone = MediaPlayer.create(getApplicationContext(), R.raw.black_juggernaut_black_mirror);
    	accelmon = new AccelMonitor(this, tv, drone);
    }
    
    public void onResume() {
    	super.onResume();
    	accelmon.onResume();
    }
    public void onPause() {
    	super.onPause();
    	audiosource.interrupt();
    	accelmon.onPause();
    	//a.close();
    }
	@Override
	public void onBufferUpdate(int[] b) {
		// TODO Auto-generated method stub
		
	}
}

//http://stackoverflow.com/questions/6959930/android-need-to-record-mic-input
class Audio extends Thread {
	private boolean stopped = false;
	
	// give high priority so it's not canceled unexpectedly
	public Audio() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		start();
	}
	
	public void run() {
		Log.i("Audio", "Running Audio Thread");
		AudioRecord recorder = null;
		AudioTrack track = null;
		short[][] buffers = new short[256][160];
		int ix = 0;
		
		final int samplerate = 16000;
		/*
         * Initialize buffer to hold continuously recorded audio data, start recording, and start
         * playback.
         */
		try {
			int N = AudioRecord.getMinBufferSize(
					samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
				);
			recorder = new AudioRecord(
					AudioSource.MIC, samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10
				);
			
			track = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate,AudioFormat.CHANNEL_OUT_MONO, 
					AudioFormat.ENCODING_PCM_16BIT, N*10, AudioTrack.MODE_STREAM);
			Log.i("Audio", "Starting recording");
			recorder.startRecording();
			Log.i("Audio", "Starting playback");
			track.play();
			/*
			 * Loops until something outside this thread stops it.
			 * Reads the data from the recorder and writes it to the audio trakc for playback.
			 */
			while(!stopped) {
				//Log.i("Map", "Writing new data to buffer");
				short[] buffer = buffers[ix++ % buffers.length];
				ix %= buffers.length;
				N = recorder.read(buffer, 0 , buffer.length);
				track.write(buffer, 0, buffer.length);
			}
		} catch(Throwable x) {
			Log.w("Audio", "Error reading voice audio", x);
		} finally {
			recorder.stop();
			recorder.release();
			track.stop();
			track.release();
		}
	}
	
	void close() {
		Log.d("Audio", "closing");
		stopped = true;
	}
}
