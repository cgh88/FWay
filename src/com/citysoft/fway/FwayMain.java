package com.citysoft.fway;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class FwayMain extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fway_main);
        
        TabHost tabHost = getTabHost();
        
        tabHost.addTab(tabHost.newTabSpec("Tab01")
        		.setIndicator("First", getResources().getDrawable(R.drawable.slideshow))
        		.setContent(new Intent(this, TabFirst.class)));
        
        tabHost.addTab(tabHost.newTabSpec("Tab02")
        		.setIndicator("Second", getResources().getDrawable(R.drawable.icon))
        		.setContent(new Intent(this, NMapViewer.class)));
        
        tabHost.addTab(tabHost.newTabSpec("Tab03")
        		.setIndicator("Third", getResources().getDrawable(R.drawable.ic_action_locate))
        		.setContent(new Intent(this, TabThird.class)));       
        
        tabHost.setCurrentTab(0);
    }
}
