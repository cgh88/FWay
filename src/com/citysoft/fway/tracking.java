package com.citysoft.fway;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class tracking extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracker);
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			createGpsDisableAlert();
		}
	}
	
	private void createGpsDisableAlert(){
		Log.i("fway", "createGpsDisableAlert");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("GPS가 꺼져있습니다. 켜시겠습니까?")
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
