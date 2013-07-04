package com.citysoft.fway;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TabFirst extends Activity implements SensorEventListener{
    /** Called when the activity is first created. */
	private TextView text01;
	private long start_time = 0;
	private Handler handler = new Handler();
	private boolean bThread = false;
	private Button button01;
	private LocationManager lm;
	private TextView gpsTxt;
	private TextView dt;
	private TextView speedTxt;
	private TextView altitudeTxt;
	private TextView orienTxt;
	private float totDt = 0f; 
	private float gpsDt = 0f;
	private int gpsSpeed = 0; 
	private Double startLatitude=0d, startLongitude=0d;
	private float azimut;
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);
        
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        gpsTxt = (TextView) findViewById(R.id.gpstxt);
        speedTxt = (TextView) findViewById(R.id.speedTxt);
        altitudeTxt = (TextView) findViewById(R.id.altitudeTxt);
        orienTxt = (TextView) findViewById(R.id.oriTxt);
        
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			gpsTxt.setTextColor(Color.parseColor("#FF0000"));
			gpsTxt.setText("OFF");
		}else{
			gpsTxt.setTextColor(Color.parseColor("#0000FF"));
			gpsTxt.setText("ON");
		}
        
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
        button01 = (Button) findViewById(R.id.button01);
        text01 = (TextView) findViewById(R.id.txt01);
        button01.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
					createGpsDisableAlert();
					return;
				}
				
				bThread = !bThread;
				
				if(bThread){
					button01.setText("STOP");
					startLocationService();
				}else{
					button01.setText("START");					
				}
				
				start_time = System.currentTimeMillis()/1000;
        		Thread t = new Thread(new Runnable(){
        			public void run(){
        				while(bThread){
        					handler.post(new Runnable(){
        						public void run(){
        							timeChange(start_time);
        							
        							gpsSpeed = (int)((gpsDt/1000)*3600);
        							speedTxt.setText(gpsSpeed+" Km/h");
        						}
        					});
        					
        					try{
        						Thread.sleep(1000);
        					}catch(Exception e){
        						e.printStackTrace();
        					}
        				}
        			}
        		});
        		t.start();
			}
		});
    }
    
    private void timeChange(long startTime){
    	int t_sec = (int)((System.currentTimeMillis()/1000) - startTime);
		int sec = t_sec%60;
		int min = t_sec/60;
		int min2 = min%60;
		int hour = min/60;
		
		String txtHour;
		String txtMin;
		String txtSec;
		
		if(hour<10){
			txtHour = "0"+hour;
		}else{
			txtHour = ""+hour;
		}
		if(min2<10){
			txtMin = "0"+min2;
		}else{
			txtMin = ""+min2;
		}
		if(sec<10){
			txtSec = "0"+sec;
		}else{
			txtSec = ""+sec;
		}
		
		text01.setText(txtHour+":"+txtMin+":"+txtSec);
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			gpsTxt.setTextColor(Color.parseColor("#FF0000"));
			gpsTxt.setText("OFF");
		}else{
			gpsTxt.setTextColor(Color.parseColor("#0000FF"));
			gpsTxt.setText("ON");
		}
		
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
		//Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
	}
    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mSensorManager.unregisterListener(this);
		//lm.removeNmeaListener(listener);
	}

    private void startLocationService() {
    	LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    	GPSListener gpsListener = new GPSListener();
		long minTime = 0;
		float minDistance = 0;

		manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					minTime,
					minDistance,
					gpsListener);

		try {
			Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastLocation != null) {
				Double latitude = lastLocation.getLatitude();
				Double longitude = lastLocation.getLongitude();

				Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : "+ latitude + "\nLongitude:"+ longitude, Toast.LENGTH_LONG).show();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}


    }
    
	private class GPSListener implements LocationListener {
	    public void onLocationChanged(Location location) {
	    	
			Double latitude = location.getLatitude();
			Double longitude = location.getLongitude();
			Double Altitude = location.getAltitude();
			float Accuracy = location.getAccuracy();
			
			//float distance = location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)
			
			/*GpsStatus.NmeaListener ml = new GpsStatus.NmeaListener() {
				
				@Override
				public void onNmeaReceived(long timestamp, String nmea) {
					// TODO Auto-generated method stub
					String str_temp[] = nmea.split(",");
					if(str_temp[0].equals("GPGGA")){
						Log.d("GPS Number", str_temp[7]);
					}
				}
			}; */
			
			DecimalFormat format = new DecimalFormat(".######");
			DecimalFormat format2 = new DecimalFormat("#.#");
			
			String msg = "Latitude : "+ format.format(latitude) + "\nLongitude:"+ format.format(longitude)+"\nAltitude:"+format.format(Altitude);
			Log.i("GPSListener", msg);
			
			altitudeTxt.setText(""+Altitude.intValue()+" m");
			
			if(startLatitude==0 || startLongitude==0){
				startLatitude = Double.parseDouble(format.format(latitude));
				startLongitude = Double.parseDouble(format.format(longitude));
			}else{
				float[] results = new float[3];
				location.distanceBetween(startLatitude, startLongitude, latitude, longitude, results);
				dt = (TextView) findViewById(R.id.dtTxt);
				
				gpsDt = results[0];
				totDt += results[0];
				
				dt.setText(format2.format((totDt/1000))+" Km");
				
				startLatitude = latitude;
				startLongitude = longitude;
			}
			
//			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			//text02.setText(msg);
			
			//fileSave(msg);
		}

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO
        return true;
    }    
    
    public void fileSave(String text){
    	String path = Environment.getExternalStorageDirectory().getAbsolutePath(); // 저장소에 저장
    	String fileName = "/android.txt"; //파일명
    	File file = new File(path + fileName);
    	
    	FileOutputStream fos = null;
    	try {
    		fos = new FileOutputStream(file, true);
    		fos.write((text).getBytes());
    		fos.close();
    	} catch (IOException e) {
    		e.printStackTrace();
		}
   }
    
    private void createGpsDisableAlert(){
		//Log.i("fway", "createGpsDisableAlert");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("GPS를 켜야만 사용이 가능합니다.\nGPS를 켜시겠습니까?")
				 .setCancelable(false)
				 .setPositiveButton("GPS켜기",
						 new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// TODO Auto-generated method stub
								showGpsOption();
							}
						})
				 .setNegativeButton("취소", 
						 new DialogInterface.OnClickListener() {
						 	@Override
							public void onClick(DialogInterface dialog, int id) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
				 });
				 builder.show();
	}
    
    private void showGpsOption(){
		Intent gpsOptinIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptinIntent);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	float[] mGravity;
	float[] mGeomagnetic;
	@Override
	public void onSensorChanged(SensorEvent event) {
		float orientations = 0f;
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		      mGravity = event.values;
	    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		      mGeomagnetic = event.values;
	    if (mGravity != null && mGeomagnetic != null) {
        float R[] = new float[9];
	    float I[] = new float[9];
	    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		    if (success) {
		      float orientation[] = new float[3];
		      SensorManager.getOrientation(R, orientation);
		      azimut = orientation[0]; // orientation contains: azimut, pitch and roll
		    }
		}
	    DecimalFormat azimutformat = new DecimalFormat("###");
	    
	    orientations = azimut*360/(2*3.14159f);
	    //orienTxt.setText("azimut: "+azimutformat.format(orientations));
	    
	    if(Integer.parseInt(azimutformat.format(orientations)) < 0){
	    	//orienTxt.setText("azimut: "+azimutformat.format(orientations));
	    	if(Math.abs(orientations) >= 0 && Math.abs(orientations) < 10){
		    	orienTxt.setText("N");
		    }else if(Math.abs(orientations) >= 10 && Math.abs(orientations) < 80){
		    	orienTxt.setText("N E");
		    }else if(Math.abs(orientations) >= 80 && Math.abs(orientations) < 100){
		    	orienTxt.setText("E");
		    }else if(Math.abs(orientations) >= 100 && Math.abs(orientations) < 170){
		    	orienTxt.setText("S E");
		    }else if(Math.abs(orientations) >= 170 && Math.abs(orientations) < 180){
		    	orienTxt.setText("S");
		    }
	    }else{
	    	
	    	if(Math.abs(orientations) >= 0 && Math.abs(orientations) < 10){
		    	orienTxt.setText("N");
		    }else if(Math.abs(orientations) >= 10 && Math.abs(orientations) < 80){
		    	orienTxt.setText("N W");
		    }else if(Math.abs(orientations) >= 80 && Math.abs(orientations) < 100){
		    	orienTxt.setText("W");
		    }else if(Math.abs(orientations) >= 100 && Math.abs(orientations) < 170){
		    	orienTxt.setText("S W");
		    }else if(Math.abs(orientations) >= 170 && Math.abs(orientations) < 180){
		    	orienTxt.setText("S");
		    }
	    }
	    
	    /**/
	}
}