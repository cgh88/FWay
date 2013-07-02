package com.citysoft.fway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TabFirst extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_first);
        
        // 버튼 이벤트 처리
        Button button01 = (Button) findViewById(R.id.button01);
        
        button01.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TabFirst.this, TabSecond.class);
				startActivity(intent);
			}
		});
    }
}