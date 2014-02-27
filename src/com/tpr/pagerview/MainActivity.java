package com.tpr.pagerview;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView t1,t2;
	PagerView pager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		pager = (PagerView) findViewById(R.id.pager);
		t1 = new TextView(this);
		t1.setText("test1");
		pager.addView(t1);
		t2 = new TextView(this);
		t2.setText("test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2test2");
		pager.addView(t2);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
