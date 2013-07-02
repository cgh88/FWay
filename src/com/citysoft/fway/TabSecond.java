package com.citysoft.fway;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabSecond extends Activity {
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_second);
		
		TextView txt = (TextView) findViewById(R.id.txt01);
		txt.setSelected(true);
	}
}