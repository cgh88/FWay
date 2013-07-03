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

public class TabFirst extends Activity{
    /** Called when the activity is first created. */
	TextView text01;
	long start_time = 0;
	Handler handler = new Handler();
	boolean bThread = false;
	Button button01;
	LocationManager lm;
	TextView gpsTxt;
	TextView dt;
	float totDt = 0f; 
	Double startLatitude=0d, startLongitude=0d;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);
        
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        gpsTxt = (TextView) findViewById(R.id.gpstxt);
        
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			gpsTxt.setTextColor(Color.parseColor("#FF0000"));
			gpsTxt.setText("OFF");
		}else{
			gpsTxt.setTextColor(Color.parseColor("#0000FF"));
			gpsTxt.setText("ON");
		}
        
        // 버튼 이벤트 처리
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
        							//시간정보 표시
        							timeChange(start_time);
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
		//Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
	}


	/**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
    	// 위치 관리자 객체 참조
    	LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// 위치 정보를 받을 리스너 생성
    	GPSListener gpsListener = new GPSListener();
		long minTime = 0;
		float minDistance = 0;

		// GPS를 이용한 위치 요청
		manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					minTime,
					minDistance,
					gpsListener);

		// 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
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

		Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }
    
    /**
     * 리스너 클래스 정의
     */
	private class GPSListener implements LocationListener {
		/**
		 * 위치 정보가 확인될 때 자동 호출되는 메소드
		 */
	    public void onLocationChanged(Location location) {
	    	
			Double latitude = location.getLatitude();
			Double longitude = location.getLongitude();
			Double Altitude = location.getAltitude();
			
			//float distance = location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)
			
			DecimalFormat format = new DecimalFormat(".######");
			DecimalFormat format2 = new DecimalFormat("#.#");
			
			String msg = "Latitude : "+ format.format(latitude) + "\nLongitude:"+ format.format(longitude)+"\nAltitude:"+format.format(Altitude);
			Log.i("GPSListener", msg);
			
			if(startLatitude==0 || startLongitude==0){
				startLatitude = Double.parseDouble(format.format(latitude));
				startLongitude = Double.parseDouble(format.format(longitude));
			}else{
				float[] results = new float[3];
				location.distanceBetween(startLatitude, startLongitude, latitude, longitude, results);
				dt = (TextView) findViewById(R.id.dtTxt);
				
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
    	String path = Environment.getExternalStorageDirectory().getAbsolutePath(); // 파일이 저장될 경로
    	String fileName = "/android.txt"; // 파일 이름
    	File file = new File(path + fileName);
    	
    	//String text = "저장될 내용"; // 저장될 내용
    	
    	FileOutputStream fos = null;
    	try {
    		fos = new FileOutputStream(file, true); // 파일 생성
    		fos.write((text).getBytes()); // 파일에 내용 저장
    		fos.close(); // 파일 닫기
    	} catch (IOException e) {
    		e.printStackTrace();
		}
   }
    
    private void createGpsDisableAlert(){
		//Log.i("fway", "createGpsDisableAlert");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("GPS가 꺼져있습니다.\n해당 App을 사용하시려면 GPS를 켜야합니다.\n켜시겠습니까?")
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
}