package com.liubo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.liubo.db.AlertDao;
import com.liubo.map.Constant;
import com.liubo.modal.Alert;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

/**
 * ¸æ¾¯¼ÇÂ¼
 * @author bo.liu-1
 *
 */
public class AlertRecordActiviry extends Activity {

	ArrayList<HashMap<String, String>> alertInfoList = new ArrayList<HashMap<String, String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_record);
		
		ListView alertRecordView = (ListView) findViewById(R.id.alert_record_view);

		String towerId = null;
		
		Intent intent = this.getIntent();
		if(intent != null ){
			Bundle bundle = intent.getExtras();
			if(bundle != null) {
				towerId = bundle.getString(Constant.Common.TOWER_ID);
				AlertDao alertDao = new AlertDao(this);
				List<Alert> alertList = new ArrayList<Alert>();
				alertList = alertDao.getAlertsByTowerId(towerId);
				for(Alert alert : alertList ){
					alertInfoList.add(alertDao.parseAlert(alert));
				}
				
				SimpleAdapter listItemAdapter = new SimpleAdapter(this, alertInfoList,
						R.layout.alert_tower_adapter, new String[] { "tower",
								"data" }, new int[] { R.id.alert_tower_title,
								R.id.alert_tower_context });
				alertRecordView.setAdapter(listItemAdapter);
			}
		}
		
	}
}
