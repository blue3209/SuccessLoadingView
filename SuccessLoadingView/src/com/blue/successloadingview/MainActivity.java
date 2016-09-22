package com.blue.successloadingview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.blue.successloadingview.view.SuccessLoadingView;

public class MainActivity extends Activity {

	private SuccessLoadingView successView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		successView = (SuccessLoadingView) findViewById(R.id.success_loading_view);
	}

	public void onClick(View view) {
		successView.startAnim();
	}
}
