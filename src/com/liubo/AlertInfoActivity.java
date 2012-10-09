package com.liubo;

import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.liubo.db.DBConnection;
import com.liubo.map.Constant;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;

public class AlertInfoActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_info);
		
		Button  button = (Button) findViewById(R.id.button);
		
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if(bundle != null){
			String towerNum = bundle.getString(Constant.Common.TOWER_NUM);
			DBConnection dbConnection = new DBConnection(this.getApplicationContext());
			SQLiteDatabase db = dbConnection.getWritableDatabase();
			try{
				Cursor cur = db.query("alert", null, " tower_num = '" +towerNum +"'", null, null, null, "id desc");
				if(cur != null && cur.moveToFirst()){
						String id = cur.getString(0);
						String _towerNum = cur.getString(cur.getColumnIndex("tower_num"));
						String circuitNum = cur.getString(cur.getColumnIndex("circuit_num"));
						String substationNum = cur.getString(cur.getColumnIndex("substation_num"));
						Alert alert = new Alert();
						alert.setId(id);
						alert.setTowerNum(_towerNum);
						alert.setCircuitNum(circuitNum);
						alert.setSubstationNum(substationNum);
						
						Cursor towerCur = db.query("tower", null, " tower_num = '" +towerNum +"'", null, null, null, "id desc");
						if(towerCur != null && towerCur.moveToFirst()){
							String towerId = towerCur.getString(0);
							String name = towerCur.getString(1);
							String tower_num = towerCur.getString(2);
							String circuit_num = towerCur.getString(3);
							String longitude = towerCur.getString(4);
							String latitude = towerCur.getString(5);
							String status = towerCur.getString(6);
							Tower tower = new Tower();
							tower.setId(towerId);
							tower.setName(name);
							tower.setTowerNum(tower_num);
							tower.setCircuit(circuit_num);
							tower.setLongitude(longitude);
							tower.setLatitude(latitude);
							tower.setStatus(status);
							System.out.println("tower.getTowerNum()"+tower.getTowerNum());
						}else {
							try {
								throw new Exception("¸Ã¸ËËþ²»´æÔÚ¡£");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				}
				cur.close();
			}finally{
				db.close();
			}
		}
		
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent in = new Intent();
				ListView lv = (ListView) findViewById(R.id.textview1);
				setResult(RESULT_OK, in);
				finish();
			}
		});
	}

}
