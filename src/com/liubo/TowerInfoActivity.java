package com.liubo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.liubo.db.TowerDao;
import com.liubo.map.Constant;
import com.liubo.modal.Tower;

public class TowerInfoActivity extends Activity {

	private static final int MENU_ITEM_EDIT = 0;
	private static final int MENU_ITEM_ALERT_RECORD = 1;
	private static final int MENU_ITEM_VIEW_MAP = 2;	
	private static final int MENU_ITEM_RECOVERY = 3;
	private static final int MENU_ITEM_DELETE = 4;
	
	Tower tower = null;
	private Bundle savedInstanceState = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tower_info);
		this.savedInstanceState = savedInstanceState;
		Intent intent = this.getIntent();
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				String towerId = bundle.getString(Constant.Common.TOWER_ID);
				if( towerId!= null){
					if(towerId.indexOf(":")>0){
						String[] strArray = towerId.split(":");
						if(strArray.length == 2){
							towerId = strArray[1];
						}
					}
					
					TowerDao towerDao = new TowerDao(this);
					tower = towerDao.getTowerById(towerId);
					towerDao.close();
					if(tower != null){
						TextView _towerID =  (TextView) findViewById(R.id.tower_id);
						TextView _towerNum =  (TextView) findViewById(R.id.tower_num);
						TextView _circuitNum =  (TextView) findViewById(R.id.circuit_num);
						TextView _stationNum =  (TextView) findViewById(R.id.station_num);
						TextView _preToewr =  (TextView) findViewById(R.id.pre_tower);
						TextView _sim =  (TextView) findViewById(R.id.sim);
						TextView _latitude =  (TextView) findViewById(R.id.latitude);
						TextView _longitude =  (TextView) findViewById(R.id.longitude);
						TextView _status = (TextView) findViewById(R.id.tower_status);
						TextView _comment = (TextView) findViewById(R.id.tower_comment);
						
						_towerID.setText(tower.getId());
						_towerNum.setText(tower.getTowerNum());
						_circuitNum.setText(tower.getCircuit());
						_stationNum.setText(tower.getStationNum());
						_preToewr.setText(tower.getPreTowerNum());
						_sim.setText(tower.getSimNum());
						_latitude.setText(tower.getDisplayLatitude());
						_longitude.setText(tower.getDisplayLogitude());
						_status.setText(tower.getStatus());
						_comment.setText(tower.getComment());
						
					}
					
				}
				else{
					Intent intent2 = new Intent();
					intent2.putExtra("error_info", "杆塔信息不存在！");
					intent2.setClass(TowerInfoActivity.this, ErrorAct.class);
					startActivity(intent2);
					this.finish();
				}
			}
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_EDIT, 0, "编辑").setIcon(R.drawable.menu_edit);
		menu.add(0, MENU_ITEM_ALERT_RECORD, 0, "历史故障信息").setIcon(R.drawable.menu_info_details);
		if (tower.isError()) {
			menu.add(0, MENU_ITEM_RECOVERY, 0, "修复状态").setIcon(R.drawable.menu_manage);
		}
		menu.add(0, MENU_ITEM_VIEW_MAP, 0, "查看地图").setIcon(R.drawable.menu_viewmap);
		menu.add(0, MENU_ITEM_DELETE, 0, "删除").setIcon(R.drawable.menu_delete);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		TextView view = (TextView) findViewById(R.id.tower_id);
		String towerId = view.getText().toString();
		final TowerDao towerDao  = new TowerDao(this.getApplicationContext());
		final Tower tower = towerDao.getTowerById(towerId);
		switch(item.getItemId()){
		case MENU_ITEM_EDIT:
			Intent intent = new Intent();
			intent.putExtra(Constant.Common.TOWER_ID, towerId);
			intent.setClass(TowerInfoActivity.this,
					EditTowerActivity.class);
			startActivity(intent);
			this.finish();
			return true;
		case MENU_ITEM_ALERT_RECORD :
			Intent intent2 = new Intent();
			intent2.putExtra(Constant.Common.TOWER_ID, towerId);
			intent2.setClass(TowerInfoActivity.this,
					AlertListAct.class);
			startActivity(intent2);
			return true;
		case MENU_ITEM_RECOVERY :
			if(tower != null ){
				towerDao.updateTowerStatusById(towerId, Tower.Status.NORMAL.value);
				towerDao.close();
			}
			onCreate(savedInstanceState);
			return true;
		case MENU_ITEM_VIEW_MAP : {
        	Message msg = Message.obtain(ActivityGroupDemo.self.handler, ActivityGroupDemo.SWITCH_TAB);
        	msg.getData().putString(Constant.Common.ACTIVITY_INDEX, "0");
        	msg.getData().putString(Constant.Common.TOWER_ID, tower.getId());
        	ActivityGroupDemo.self.handler.sendMessage(msg);
        	this.finish();
			return true;
		}
		case MENU_ITEM_DELETE : {
			AlertDialog.Builder multiDia=new AlertDialog.Builder(TowerInfoActivity.this);
	        multiDia.setTitle("确定删除");
	        multiDia.setPositiveButton("确定", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					towerDao.delete(tower.getId());
					Toast.makeText(getApplicationContext(), "杆塔"+tower.getTowerNum()+"已被删除！", Toast.LENGTH_LONG).show();
					Intent intent = new Intent();
					intent.setClass(TowerInfoActivity.this, ActivityGroupDemo.class);
					TowerInfoActivity.this.startActivity(intent);
					TowerInfoActivity.this.finish();
				}});
	        multiDia.setNegativeButton("取消", null);
	        multiDia.create().show();
	        return true;
		}
			default: {
				return false;
			}
		}
	}

}
