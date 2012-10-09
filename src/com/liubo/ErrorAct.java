package com.liubo;

import com.liubo.util.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ErrorAct extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error);
		TextView view = (TextView) findViewById(R.id.error);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		String info = bundle.getString("error_info");
		if(StringUtils.isNotEmpty(info)){
			view.setText(info);
		}
		else{
			view.setText("–≈œ¢¥ÌŒÛ£°");
		}
	}
	
}
