package com.citysoft.fway;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.TimerTask;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.util.Xml;
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
	private LocationManager manager;
	private TextView gpsTxt;
	private TextView gpsstatusTxt;
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
	private GPSListener gpsListener;
	private PowerManager pm;
	private long olddt_time = 0;
	private long newdt_time = 0;
	private float   speed = 0;
	//private Timer        UITimer=null;
	private int count = 0;
	
	//Back Button Process
	private final long FINSH_INTERVAL_TIME = 2000;
	private long backPressedTime = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);       
        
        //파워 ON/OFF Check
        //pm= (PowerManager)getSystemService(Context.POWER_SERVICE);        
        
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        gpsTxt = (TextView) findViewById(R.id.gpstxt);
        gpsstatusTxt = (TextView) findViewById(R.id.gpsstutustxt);
        speedTxt = (TextView) findViewById(R.id.speedTxt);
        altitudeTxt = (TextView) findViewById(R.id.altitudeTxt);
        orienTxt = (TextView) findViewById(R.id.oriTxt);
        
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
        button01 = (Button) findViewById(R.id.button01);
        Button xml = (Button) findViewById(R.id.xml);
        
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			gpsTxt.setTextColor(Color.parseColor("#FF0000"));
			gpsTxt.setText("OFF");
			button01.setText("GPS켜기");
		}else{
			gpsTxt.setTextColor(Color.parseColor("#0000FF"));
			gpsTxt.setText("ON");
		}
        
        text01 = (TextView) findViewById(R.id.txt01);
        
        xml.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				fileSave();
			}
        	
        });
        
        button01.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
					createGpsDisableAlert();
					return;
				}
				
				bThread = !bThread;
				
				if(bThread){
					button01.setText("STOP");
					startLocationService();
					
					//변수값 초기화
					gpsSpeed = 0;
					gpsDt= 0;
					startLatitude=0d;
					startLongitude=0d;
				}else{
					button01.setText("START");	
					stopLocationService();
					return;
				}
				
				start_time = System.currentTimeMillis()/1000;
				
        		Thread t = new Thread(new Runnable(){
        			public void run(){
        				while(bThread){
        					handler.post(new Runnable(){
        						public void run(){
        							timeChange(start_time);
        							
        							//스피드구하기
        							//gpsSpeed = (int)((gpsDt/1000)*3600);
        							//speedTxt.setText(gpsSpeed+" Km/h"); 
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
    
    // gps 의 상태를 처리하는 콜백함수
    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener()
    {
        public  void  onGpsStatusChanged (int event ) 
        {
            switch(event)
            {
            case GpsStatus.GPS_EVENT_STARTED:
                // gps 연결 시도를 시작하면 발생하는 이벤트
                // mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); 를 호출하면 발생한다.
            	gpsstatusTxt.setTextColor(Color.parseColor("#00FF00"));
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                // gps 연결이 끝났을때 발생하는 이벤트
                // mLocationManager.removeUpdates(ms_Instance); 를 호출하면 발생한다.
            	//Toast.makeText(getApplicationContext(), "GPS 끊겼습니다.", Toast.LENGTH_SHORT).show();
            	gpsstatusTxt.setTextColor(Color.parseColor("#FF0000"));
            	//Toast.makeText(getApplicationContext(), "GPS수신이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                // gps 연결이 되면 발생하는 이벤트
            	//gpsstatusTxt.setTextColor(Color.parseColor("#00FCFF"));
            	//Toast.makeText(getApplicationContext(), "GPS연결되었습니다.", Toast.LENGTH_SHORT).show();
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                // gps 와 연결이 되어 있는 위성의 상태를 넘겨받는 이벤트
                // gps 수신상태를 체크할 수 있다.
            	//Toast.makeText(getApplicationContext(), "GPS_EVENT_SATELLITE_STATUS!!!", 1000).show();
            	
            	GpsStatus status = manager.getGpsStatus(null);
            	   
        	    Iterable sats = status.getSatellites(); 
            	Iterator satI = sats.iterator(); 
            	int count = 0;
            	         
            	while(satI.hasNext()){ 
            		GpsSatellite gpssatellite = (GpsSatellite) satI.next(); 
            	    if (gpssatellite.usedInFix()){ 
            	    	count++; 
            	     } 
            	 }
            	 if(count > 5){
            		 //Toast.makeText(getApplicationContext(), "GPS 잡혔습니다." + count, 1000).show(); //(this, "GPS 잡혔습니다." + count, 1000).show();
            		 gpsstatusTxt.setTextColor(Color.parseColor("#0000FF"));
            	 }else{
            		 gpsstatusTxt.setTextColor(Color.parseColor("#FF0000"));
            		 //Toast.makeText(getApplicationContext(), "GPS수신상태가 안좋습니다. 위성수: " + count, 1000).show();
            	 }
                break;
            }
        }
    };
    
    @Override
	public void onBackPressed() {
		// TODO Back Button Two Time Close
    	long tempTime        = System.currentTimeMillis();
    	long intervalTime    = tempTime - backPressedTime;

    	if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
    		super.onBackPressed(); 
    	} 
    	else { 
	    	backPressedTime = tempTime; 
	    	Toast.makeText(getApplicationContext(),"'뒤로'버튼을한번더누르시면종료됩니다.",Toast.LENGTH_SHORT).show(); 
    	} 
	}

	private void timeChange(long startTime){
    	int t_sec = (int)((System.currentTimeMillis()/1000) - startTime);
		int sec = t_sec%60;
		int min = t_sec/60;
		int min2 = min%60;
		int hour = min/60;
		
		
		String display_string = String.format("%02d:%02d:%02d", hour, min2, sec);
		
		text01.setText(display_string);
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			gpsTxt.setTextColor(Color.parseColor("#FF0000"));
			gpsTxt.setText("OFF");
			
		}else{
			gpsTxt.setTextColor(Color.parseColor("#0000FF"));
			gpsTxt.setText("ON");
			if(!bThread){
				button01.setText("Start");
			}else{
				button01.setText("Stop");
			}
		}
		
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
		//Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
	    //System.out.println("Screen Status: "+pm.isScreenOn());
	}
    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mSensorManager.unregisterListener(this);
		//System.out.println("Screen Status: "+pm.isScreenOn());
	}

    private void startLocationService() {
    	//manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	
    	/*Criteria criteria = new Criteria();
   	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	criteria.setAltitudeRequired(false);
    	criteria.setBearingRequired(false);
    	criteria.setCostAllowed(true);
    	criteria.setPowerRequirement(Criteria.POWER_LOW);    
    	
    	String provider = manager.getBestProvider(criteria, true);*/
    	manager.addGpsStatusListener(gpsStatusListener);
    	
    	gpsListener = new GPSListener();
    	
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
    
    private void stopLocationService() {
    	manager.removeGpsStatusListener(gpsStatusListener);
    	manager.removeUpdates(gpsListener);
    }
    
	private class GPSListener implements LocationListener {
	    public void onLocationChanged(Location location) {
	    	
			Double latitude = location.getLatitude();
			Double longitude = location.getLongitude();
			speed = location.getSpeed();
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
			speedTxt.setText(""+(int)((speed*3600)/1000)+"Km/h");
			
			if(startLatitude==0 || startLongitude==0){
				startLatitude = Double.parseDouble(format.format(latitude));
				startLongitude = Double.parseDouble(format.format(longitude));
				
				newdt_time = System.currentTimeMillis()/1000;
			}else{
				float[] results = new float[3];
				
				olddt_time = newdt_time;
				
				location.distanceBetween(startLatitude, startLongitude, latitude, longitude, results);
				
				gpsDt = results[0];
				totDt += results[0];
				
				dt = (TextView) findViewById(R.id.dtTxt);
				dt.setText(format2.format((totDt/1000))+" Km");
				
				startLatitude = latitude;
				startLongitude = longitude;
				newdt_time = System.currentTimeMillis()/1000;
			}
			
            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			//text02.setText(msg);
			
			//fileSave(msg);
		}

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    	Toast.makeText(getApplicationContext(), "onStatusChanged", Toast.LENGTH_SHORT).show();
	    	switch (status) {
			    case LocationProvider.AVAILABLE:  //서비스 가능지역
			    	Toast.makeText(getApplicationContext(), "서비스 가능지역", Toast.LENGTH_SHORT).show();
			        break;     
			    case LocationProvider.OUT_OF_SERVICE:  //서비스 지역이 아닙니다
			    	Toast.makeText(getApplicationContext(), "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
			        break;
			    case LocationProvider.TEMPORARILY_UNAVAILABLE:  //일시적으로 위치 정보를 사용할 수 없습니다
			    	Toast.makeText(getApplicationContext(), "일시적으로 위치 정보를 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
			        break;
			}
	    }

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO
        return true;
    }    
	
	public void progressSpin(){
		ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("");
		progress.setMessage("로딩중...");
		progress.show();
		//progress.dismiss();
	}
    
    public void fileSave(){
    	
    	
    	String path = Environment.getExternalStorageDirectory().getAbsolutePath(); // 저장소에 저장
    	String fileName = "/android.xml"; //파일명
    	File file = new File(path + fileName);
    	
    	FileOutputStream fos = null;
    	try {
    		fos = new FileOutputStream(file, true);
    		//fos.write((text).getBytes());
    		//fos.close();
    	} catch (IOException e) {
    		e.printStackTrace();
		}
    	//GPX XML OUTPUT
    	XmlSerializer serializer = Xml.newSerializer();
    	try{
    		serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "root");
            serializer.startTag(null, "Child1");
            serializer.endTag(null, "Child1");
            serializer.startTag(null, "Child2");
            serializer.attribute(null, "attribute", "value");
            serializer.attribute(null, "test", "asdf");
            serializer.endTag(null, "Child2");
            serializer.startTag(null, "Child3");
            serializer.text("Some text inside child 3");
            serializer.endTag(null,"Child3");
            serializer.endTag(null,"root");
            serializer.endDocument();
            serializer.flush();
            fos.close();
    	}catch(Exception e){
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
    	//GPS 환경설정 열기
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
	    //orienTxt.setText("azimut: "+Integer.parseInt(azimutformat.format(orientations)));
	    
	    if(Integer.parseInt(azimutformat.format(orientations)) < 0){
	    	//orienTxt.setText("azimut: "+azimutformat.format(orientations));
	    	if(Math.abs(orientations) >= 0 && Math.abs(orientations) < 23){
		    	orienTxt.setText("N");
		    }else if(Math.abs(orientations) >= 23 && Math.abs(orientations) < 68){
		    	orienTxt.setText("N W");
		    }else if(Math.abs(orientations) >= 68 && Math.abs(orientations) < 113){
		    	orienTxt.setText("W");
		    }else if(Math.abs(orientations) >= 113 && Math.abs(orientations) < 158){
		    	orienTxt.setText("S W");
		    }else if(Math.abs(orientations) >= 158 && Math.abs(orientations) < 180){
		    	orienTxt.setText("S");
		    }
	    	//orienTxt.append(": "+azimutformat.format(orientations));
	    }else{
	    	
	    	if(Math.abs(orientations) >= 0 && Math.abs(orientations) < 23){
		    	orienTxt.setText("N");
		    }else if(Math.abs(orientations) >= 23 && Math.abs(orientations) < 68){
		    	orienTxt.setText("N E");
		    }else if(Math.abs(orientations) >= 68 && Math.abs(orientations) < 113){
		    	orienTxt.setText("E");
		    }else if(Math.abs(orientations) >= 113 && Math.abs(orientations) < 158){
		    	orienTxt.setText("S E");
		    }else if(Math.abs(orientations) >= 158 && Math.abs(orientations) < 180){
		    	orienTxt.setText("S");
		    }
	    	//orienTxt.append(": "+azimutformat.format(orientations));
	    }
	    
	    /**/
	}
	
	//TimeTask Test
	//Run UITimer(RepeatTask, 1000, 3000); 첫번째 인자: TimeTask, 두번째 인자: 1초후 실행, 세번째 인자: 3초간 반복 
	private class RepeatTask extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i("Timer: ", ""+count);
			count++;
		}
    	
    }
}