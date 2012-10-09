package com.liubo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.liubo.db.TowerDao;
import com.liubo.exception.TowerNotUniqueException;
import com.liubo.map.Constant;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

/**
 * 有告警的杆塔列表
 * 
 * @author bo.liu-1
 * 
 */
public class AlertTowerListActivity extends Activity {

	ArrayList<HashMap<String, String>> towerInfoList = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_tower_list);
		ListView listView = (ListView) this
				.findViewById(R.id.alert_tower_list_view);
		TowerDao towerDao = new TowerDao(this.getApplicationContext());
		List<Tower> towerList = new ArrayList<Tower>();
		try {
			towerList = towerDao
					.getAlertTowerList(getApplicationContext());
		} catch (TowerNotUniqueException e) {
			Toast.makeText(getApplicationContext(), "系统提示：杆塔信息冲突，请检查数据！", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		towerDao.close();
		for (Tower tower : towerList) {
			towerInfoList.add(towerDao.parseAlertTower(tower,
					getApplicationContext()));
		}

		SimpleAdapter listTowerAdapter = new SimpleAdapter(this,
				this.towerInfoList, R.layout.alert_tower_adapter,
				new String[] { "towerId", "data" }, new int[] {
						R.id.alert_tower_title,R.id.alert_tower_context });
		
		listView.setAdapter(listTowerAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				HashMap<String, String> map = towerInfoList.get(arg2);
				String towerId = map.get("towerId");
				Intent intent = new Intent();
				String intentTowerId = towerId;
        		if(StringUtils.isNotEmpty(towerId) && towerId.indexOf(":")>0){
        			intentTowerId = towerId.split(":")[1];
        		}
        		TowerDao towerDao = new TowerDao(AlertTowerListActivity.this.getApplicationContext());
        		Tower tower = towerDao.getTowerById(intentTowerId);
        		if(tower.getStatus().equals(Tower.Status.ERROR) || tower.getStatus().equals("故障")){
        			showMultiDia(intentTowerId);
        		}else{
        			intent.putExtra(Constant.Common.TOWER_ID, intentTowerId);
        			intent.setClass(AlertTowerListActivity.this, AlertListAct.class);
        			startActivity(intent);
        		}
			}
		});
	}
	
	private void showMultiDia(final String towerId)
    {
        AlertDialog.Builder multiDia=new AlertDialog.Builder(AlertTowerListActivity.this);
        multiDia.setTitle("操作");
        multiDia.setPositiveButton("查看杆塔", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent();
            	String intentTowerId = towerId;
        		if(StringUtils.isNotEmpty(towerId) && towerId.indexOf(":")>0){
        			intentTowerId = towerId.split(":")[1];
        		}
				intent.putExtra(Constant.Common.TOWER_ID, intentTowerId);
				intent.setClass(AlertTowerListActivity.this,
						TowerInfoActivity.class);
				AlertTowerListActivity.this.startActivity(intent);
            }
        });
        multiDia.setNegativeButton("恢复状态", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String intentTowerId = towerId;
        		if(StringUtils.isNotEmpty(towerId) && towerId.indexOf(":")>0){
        			intentTowerId = towerId.split(":")[1];
        		}
            	TowerDao towerDao = new TowerDao(AlertTowerListActivity.this.getApplicationContext());
            	towerDao.updateTowerStatusById(intentTowerId, Tower.Status.NORMAL.value);
            	towerDao.close();
            	Toast.makeText(AlertTowerListActivity.this, "杆塔"+intentTowerId+"状态已恢复正常", Toast.LENGTH_SHORT).show();
            	Message msg = Message.obtain(ActivityGroupDemo.self.handler, ActivityGroupDemo.SWITCH_TAB);
            	msg.getData().putString(Constant.Common.ACTIVITY_INDEX, "2");
            	ActivityGroupDemo.self.handler.sendMessage(msg);
            	AlertTowerListActivity.this.finish();
            }
        });
        multiDia.create().show();
    }

}
