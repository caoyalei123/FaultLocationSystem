package com.liubo;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.liubo.db.AlertDao;
import com.liubo.db.TowerDao;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;

public class IndexAct extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		AlertDao alertDao = new AlertDao(this.getApplicationContext());
		List<Alert> alertList = alertDao.getAllAlerts();
		alertDao.close();
		int alertSize = 0 ; 
		if(alertList != null){
			alertSize = alertList.size();
		}
		TowerDao towerDao = new TowerDao(this.getApplicationContext());
		List<Tower> towerList = towerDao.getAllTowers();
		towerDao.close();
		int towerSize = 0;
		if(towerList != null){
			towerSize = towerList.size();
		}
		Button alertListButton = (Button) this.findViewById(R.id.alert_list_button);
		alertListButton.setText(alertListButton.getText().toString() + "(" + alertSize + ")");
		alertListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent  = new Intent();
				intent.setClass(IndexAct.this, AlertTowerListActivity.class);
				startActivity(intent);
			}
		});
		
		Button towerListButton = (Button) this.findViewById(R.id.tower_list_button);
		towerListButton.setText(towerListButton.getText().toString() + "(" + towerSize + ")");
		towerListButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(IndexAct.this, TowerListAct.class);
				startActivity(intent);
			}
		});
		
		Button mapBtn = (Button) this.findViewById(R.id.map_button);
		mapBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(IndexAct.this
						, MapMainActivity.class);
				startActivity(intent);
			}
		});
		
	}

	
}
