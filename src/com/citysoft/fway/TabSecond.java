package com.citysoft.fway;

import java.util.ArrayList;
import java.util.List;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TabSecond extends Activity
	implements MapView.OpenAPIKeyAuthenticationResultListener, MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{
		

	private static final int CURRENT_LOC = Menu.FIRST + 1;
	private static final int CURRENT_LOC_HEADING = Menu.FIRST + 2;
	private static final int MAP_HYBRID = Menu.FIRST + 3;
	private static final int TRACKING_ON = Menu.FIRST + 4;
	private static final int TRACKING_OFF = Menu.FIRST + 5;
	
	private static final String LOG_TAG = "Tag: ";
	
	private static final String api_key = "98695caa602cc3e7babefa65af2dc059c3f09e13";
	
	private MapView mapView;
	private MapPOIItem poiItem;
	private MapReverseGeoCoder reverseGeoCoder = null;
	
	//��ġ����
	private List<Double> lat_lst;
	private List<Double> long_lst;
	private boolean onStart = false;
	private double cur_lat=0, cur_long=0;
	private boolean bHybrid = false;
	private int        iAccuracy = 0;
	private LocationManager locManager = null;
	//private double old_cur_x=0, old_cur_y=0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle("[DaumMap Custom]");
        
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        
        TextView textView = new TextView(this);
        textView.setText("Press MENU button!");
        textView.setTextSize(18.0f);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.CYAN);
        textView.setTextColor(Color.WHITE);
        
        linearLayout.addView(textView);
        
        startLocationService();
        
        lat_lst = new ArrayList<Double>();
        long_lst = new ArrayList<Double>();
        
        MapView.setMapTilePersistentCacheEnabled(true);
        
        mapView = new MapView(this);
        
        mapView.setDaumMapApiKey(api_key);
        mapView.setOpenAPIKeyAuthenticationResultListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationEventListener(this);
        mapView.setPOIItemEventListener(this);
        
        //mapView.setMapType(MapView.MapType.Hybrid);
        mapView.setMapType(MapView.MapType.Standard);
        
        linearLayout.addView(mapView);
        
        setContentView(linearLayout);
        
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CURRENT_LOC, Menu.NONE, "������ġ");
		menu.add(0, CURRENT_LOC_HEADING, Menu.NONE, "��ħ���ѱ�");
		menu.add(0, MAP_HYBRID, Menu.NONE, "�� Hybrid");
		menu.add(0, TRACKING_ON, Menu.NONE, "Ʈ��ŷ����");
		menu.add(0, TRACKING_OFF, Menu.NONE, "Ʈ��ŷ��");
		Log.i(LOG_TAG, "onCreateOptionsMenu");
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {

		final int itemId = item.getItemId();
		
		switch (itemId) {
		case CURRENT_LOC:
		{
			mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
		}
			return true;
		
		case CURRENT_LOC_HEADING:
		{
			mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);		
		}
			return true;
		
		case MAP_HYBRID:
		{
			if(!bHybrid){
				bHybrid = !bHybrid;
				mapView.setMapType(MapView.MapType.Satellite);
			}else{
				mapView.setMapType(MapView.MapType.Standard);
			}
			
		}
			return true;
			
		case TRACKING_ON:
		{
			if(onStart){
				new AlertDialog.Builder(this).setTitle("Alert").setMessage("Ʈ��ŷ���Դϴ�!").setNeutralButton("�ݱ�", null).show();
				return true;
			}
			
			MapPOIItem poiItem = mapView.findPOIItemByTag(10001);
			if (poiItem != null) {
				mapView.removePOIItem(poiItem);	
			}
			onStart = true;
			
			lat_lst.add(cur_lat);
			long_lst.add(cur_long);
			
			MapPOIItem poiItemStart = new MapPOIItem();
			poiItemStart.setItemName("Start");
			poiItemStart.setTag(10001);
			poiItemStart.setMapPoint(MapPoint.mapPointWithGeoCoord(cur_lat,cur_long));
			poiItemStart.setMarkerType(MapPOIItem.MarkerType.CustomImage);
			poiItemStart.setShowAnimationType(MapPOIItem.ShowAnimationType.SpringFromGround);
			poiItemStart.setShowCalloutBalloonOnTouch(false);
			poiItemStart.setCustomImageResourceId(R.drawable.custom_poi_marker_start);
			poiItemStart.setCustomImageAnchorPointOffset(new MapPOIItem.ImageOffset(29,2));
			mapView.addPOIItem(poiItemStart);
		}
			return true;
		case TRACKING_OFF:
		{
			//Toast.makeText(MainActivity.this, (CharSequence) loc_lst, Toast.LENGTH_LONG).show();
			if(!onStart){
				new AlertDialog.Builder(this).setTitle("Alert").setMessage("Ʈ��ŷ�� �����ϼ���!").setNeutralButton("�ݱ�", null).show();
				return true;
			}
			
			MapPOIItem poiItem1 = mapView.findPOIItemByTag(10002);
			if (poiItem != null) {
				mapView.removePOIItem(poiItem1);	
			}
			
			onStart = false;
			
			lat_lst.add(cur_lat);
			long_lst.add(cur_long);
			
			MapPOIItem poiItemEnd = new MapPOIItem();
			poiItemEnd.setItemName("End");
			poiItemEnd.setTag(10002);
			poiItemEnd.setMapPoint(MapPoint.mapPointWithGeoCoord(cur_lat,cur_long));
			poiItemEnd.setMarkerType(MapPOIItem.MarkerType.CustomImage);
			poiItemEnd.setShowAnimationType(MapPOIItem.ShowAnimationType.SpringFromGround);
			poiItemEnd.setShowCalloutBalloonOnTouch(false);
			poiItemEnd.setCustomImageResourceId(R.drawable.custom_poi_marker_end);
			poiItemEnd.setCustomImageAnchorPointOffset(new MapPOIItem.ImageOffset(29,2));
			mapView.addPOIItem(poiItemEnd);
		}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
    //GPSüũ
    public boolean checkGPS() {
    	  LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    	  boolean isGPS = lm.isProviderEnabled (LocationManager.GPS_PROVIDER);
    	  if(isGPS) {
    	   return true;
    	  }
    	  else {
    	   Toast.makeText(TabSecond.this, "GPS ����� üũ���ּ��� .", Toast.LENGTH_LONG).show();
    	   startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
    	  }
    	  return false;
	 }

	@Override
	public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder arg0,
			String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalloutBalloonOfPOIItemTouched(MapView arg0, MapPOIItem arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDraggablePOIItemMoved(MapView arg0, MapPOIItem arg1,
			MapPoint arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPOIItemSelected(MapView arg0, MapPOIItem arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCurrentLocationDeviceHeadingUpdate(MapView arg0, float arg1) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onCurrentLocationDeviceHeadingUpdate");
	}

	@Override
	public void onCurrentLocationUpdate(MapView mapView, MapPoint mapCenterPoint, float accuracyInMeters) {
		//MapPoint.GeoCoordinate mapPointGeo = mapCenterPoint.getMapPointGeoCoord();
		//Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
		//Log.i("accuracyInMeters: ", ""+(int)accuracyInMeters);
		
		/*Toast toast = Toast.makeText(this, ""+(int)accuracyInMeters, 
				Toast.LENGTH_SHORT); 
		toast.show();*/ 
		
		/*cur_lat = mapPointGeo.latitude;
		cur_long = mapPointGeo.longitude;
		iAccuracy = (int)accuracyInMeters;
		
		if(onStart){
			if(iAccuracy < 5){
				lat_lst.add(cur_lat);
				long_lst.add(cur_long);
			}
			
			MapPolyline polyline = new MapPolyline();
			polyline.setTag(2000);
			polyline.setLineColor(Color.argb(128, 0, 0, 255));
			for(int i=0; i < lat_lst.size();i++){
				polyline.addPoint(MapPoint.mapPointWithGeoCoord(lat_lst.get(i),long_lst.get(i)));				
			}
			mapView.addPolyline(polyline);
			
			mapView.fitMapViewAreaToShowPolyline(polyline);
			
		}*/
		//old_cur_x = cur_x;

		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCurrentLocationUpdateCancelled(MapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCurrentLocationUpdateFailed(MapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
		//MapPoint.GeoCoordinate mapPointGeo = mapCenterPoint.getMapPointGeoCoord();
		//Log.i("LOG_TAG", String.format("MapView onMapViewCenterPointMoved (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapViewDoubleTapped(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapViewInitialized(MapView arg0) {		
		/*if(checkGPS()){
			Toast.makeText(MainActivity.this, "GPS�� �����ֽ��ϴ�", Toast.LENGTH_LONG).show();
			//TODO
		}else{
			Toast.makeText(MainActivity.this, "GPS�� �����ֽ��ϴ�", Toast.LENGTH_LONG).show();
		}*/
		checkGPS();
		
		//mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
		mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(cur_lat,cur_long), 2, true);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapViewLongPressed(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapViewSingleTapped(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapViewZoomLevelChanged(MapView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDaumMapOpenAPIKeyAuthenticationResult(MapView arg0, int arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}    
	
	/**
     * ��ġ ���� Ȯ���� ���� ������ �޼ҵ�
     */
    private void startLocationService() {
    	// ��ġ ������ ��ü ����
    	LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// ��ġ ������ ���� ������ ����
    	GPSListener gpsListener = new GPSListener();
		long minTime = 5000;
		float minDistance = 0;

		// GPS�� �̿��� ��ġ ��û
		manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					minTime,
					minDistance,
					gpsListener);

		// ��ġ Ȯ���� �ȵǴ� ��쿡�� �ֱٿ� Ȯ�ε� ��ġ ���� ���� Ȯ��
		try {
			Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastLocation != null) {
				Double latitude = lastLocation.getLatitude();
				Double longitude = lastLocation.getLongitude();

				//Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : "+ latitude + "\nLongitude:"+ longitude, Toast.LENGTH_LONG).show();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		Toast.makeText(getApplicationContext(), "��ġ Ȯ���� ���۵Ǿ����ϴ�. �α׸� Ȯ���ϼ���.", Toast.LENGTH_SHORT).show();

    }

    /**
     * ������ Ŭ���� ����
     */
	private class GPSListener implements LocationListener {
		/**
		 * ��ġ ������ Ȯ�ε� �� �ڵ� ȣ��Ǵ� �޼ҵ�
		 */
	    public void onLocationChanged(Location location) {
			//Double latitude = location.getLatitude();
			//Double longitude = location.getLongitude();
			
			cur_lat = location.getLatitude();
			cur_long = location.getLongitude();
			//iAccuracy = (int)accuracyInMeters;
			mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(cur_lat,cur_long), 2, true);
			String msg = "Latitude : "+ cur_lat + "\nLongitude: "+ cur_long;
			msg += "\nAltitude: "+location.getAltitude();
			msg += "\nBearing: "+location.getBearing();
			msg += "\nSpeed: "+location.getSpeed();
			//Log.i("GPSListener", msg);
			if(onStart){
				lat_lst.add(cur_lat);
				long_lst.add(cur_long);
				
				MapPolyline polyline = new MapPolyline();
				polyline.setTag(2000);
				polyline.setLineColor(Color.argb(128, 0, 0, 255));
				for(int i=0; i < lat_lst.size();i++){
					polyline.addPoint(MapPoint.mapPointWithGeoCoord(lat_lst.get(i),long_lst.get(i)));				
				}
				mapView.addPolyline(polyline);
				
				mapView.fitMapViewAreaToShowPolyline(polyline);
			}
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		}

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	}
}