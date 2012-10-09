package com.liubo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.liubo.db.AlertDao;
import com.liubo.db.TowerDao;
import com.liubo.map.Constant;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

public class AlertListAct extends Activity {

	ArrayList<HashMap<String, String>> alertInfoList = new ArrayList<HashMap<String, String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_list);
		
		ListView alertView = (ListView) findViewById(R.id.alert_view);
		
		AlertDao alertDao = new AlertDao(this);
		List<Alert> alerts = new ArrayList<Alert>();
		String towerId = null;
		Intent intent = this.getIntent();
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				towerId = bundle.get(Constant.Common.TOWER_ID).toString();
				if(StringUtils.isNotEmpty(towerId))
				{
					alerts = alertDao.getAlertsByTowerId(towerId);
				}
			}
		}
		
		if(!StringUtils.isNotEmpty(towerId) ){
			alerts = alertDao.getAllAlerts();
		}
		
		for(Alert alert : alerts ){
			alertInfoList.add(alertDao.parseAlert(alert));
		}
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, alertInfoList,
				R.layout.alert_tower_adapter, new String[] { "towerId",
						"data" }, new int[] { R.id.alert_tower_title,
						R.id.alert_tower_context });
		alertView.setAdapter(listItemAdapter);
		
		alertDao.close();
		
		
		alertView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				HashMap<String, String> map = alertInfoList.get(arg2);
				String towerId = map.get("towerId");
				showMultiDia(towerId);
			}
		});
	}
	
	private void showMultiDia(final String id)
    {
        AlertDialog.Builder multiDia=new AlertDialog.Builder(AlertListAct.this);
        multiDia.setTitle("操作");
        multiDia.setPositiveButton("查看杆塔", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent();
            	String intentTowerId = id;
        		if(StringUtils.isNotEmpty(id) && id.indexOf(":")>0){
        			intentTowerId = id.split(":")[1];
        		}
				intent.putExtra(Constant.Common.TOWER_ID, intentTowerId);
				intent.setClass(AlertListAct.this,
						TowerInfoActivity.class);
				AlertListAct.this.startActivity(intent);
            }
        });
        multiDia.setNegativeButton("查看地图", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String intentTowreId = id;
        		if(StringUtils.isNotEmpty(id) && id.indexOf(":")>0){
        			intentTowreId = id.split(":")[1];
        		}
            	TowerDao towerDao = new TowerDao(AlertListAct.this.getApplicationContext());
            	Tower tower = towerDao.getTowerById(intentTowreId);
    			if(tower != null && StringUtils.isNotEmpty(tower.getLongitude())  && StringUtils.isNotEmpty(tower.getLatitude())){
    				Intent intent = new Intent();
    				intent.putExtra(Constant.Common.TOWER_ID, intentTowreId);
    				intent.setClass(AlertListAct.this, MapMainActivity.class);
    				startActivity(intent);
    			}else{
    				Toast.makeText(AlertListAct.this.getApplicationContext(), "杆塔坐标错误，无法显示地图", Toast.LENGTH_LONG).show();
    			}
            }
        });
        multiDia.create().show();
    }
}
