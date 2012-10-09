package com.liubo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import com.liubo.db.DBConnection;
import com.liubo.db.TowerDao;
import com.liubo.exception.SDCardNotFoundException;
import com.liubo.exception.TowerNotUniqueException;
import com.liubo.exception.TransferDBIOException;
import com.liubo.map.Constant;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

public class FaultLocationSystemActivity extends TabActivity {

	private static final int MENU_ITEM_ID1 = 0;
	private static final int MENU_ITEM_ID2 = 1;
	private static final int MENU_ITEM_SEARCH_TOWER = 2;
	private static final int MENU_ITEM_EXPORT_DB = 3;
	private static final int MENU_ITEM_IMPORT_DB = 4;

	/** Called when the activity is first created. */

	private TabHost tabHost;

	ArrayList<HashMap<String, String>> alertInfoList = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> towerInfoList = new ArrayList<HashMap<String, String>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* 初始化界面 */
		tabHost = getTabHost();
		
		tabHost.addTab(tabHost.newTabSpec("tab_1").setIndicator("历史故障信息", getResources().getDrawable(R.drawable.ic_list_alert_sms_failed)).setContent(R.id.textview1));
		tabHost.addTab(tabHost.newTabSpec("tab_2").setIndicator("杆塔信息", getResources().getDrawable(R.drawable.tower)).setContent(R.id.textview2));
		tabHost.addTab(tabHost.newTabSpec("tab_3").setIndicator("地图信息", getResources().getDrawable(R.drawable.menu_viewmap)).setContent(R.id.textview1));

		tabHost.setCurrentTab(0);

		DBConnection dbConnection = new DBConnection(this);

		ListView alertView = (ListView) findViewById(R.id.textview1);
		ListView towerView = (ListView) findViewById(R.id.textview2);

		TowerDao towerDao = new TowerDao(this.getApplicationContext());
		List<Tower> towerList = new ArrayList<Tower>()		;
		try {
			towerList = towerDao.getAlertTowerList(getApplicationContext());
		} catch (TowerNotUniqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this.getApplicationContext(), "系统提示：杆塔信息有冲突，请检查数据。", Toast.LENGTH_LONG).show();
		}
		for (Tower tower : towerList) {
			HashMap<String, String> map = towerDao.parseAlertTower(tower, getApplicationContext());
			boolean flag = true;
			for (Map<String, String> exsitedMap : alertInfoList) {
				if (exsitedMap.get("tower") != null && exsitedMap.get("tower").equals(map.get("tower"))) {
					flag = false;
					break;
				}
			}
			if (flag) {
				alertInfoList.add(map);
			}
			flag = true;
		}

		List<Tower> towers = towerDao.getAllTowers();
		towerDao.close();

		for (Tower tower : towers) {
			towerInfoList.add(this.parseTower(tower));
		}

		SimpleAdapter listItemAdapter = new SimpleAdapter(this, alertInfoList, R.layout.alert_tower_adapter, new String[] { "tower", "data" }, new int[] {R.id.alert_tower_title,
				R.id.alert_tower_context });
		alertView.setAdapter(listItemAdapter);

		SimpleAdapter listTowerAdapter = new SimpleAdapter(this, this.towerInfoList, R.layout.alert_tower_adapter, new String[] { "tower", "data" }, new int[] { R.id.alert_tower_title,
				R.id.alert_tower_context });
		towerView.setAdapter(listTowerAdapter);

		alertView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				HashMap<String, String> map = alertInfoList.get(arg2);
				String towerNum = map.get("tower");
				Intent intent = new Intent();
				intent.putExtra(Constant.Common.TOWER_NUM, towerNum);
				intent.setClass(FaultLocationSystemActivity.this, AlertListAct.class);
				FaultLocationSystemActivity.this.startActivity(intent);
			}
		});

		towerView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				HashMap<String, String> map = towerInfoList.get(arg2);
				String towerNum = map.get("tower");
				Intent intent = new Intent();
				intent.putExtra(Constant.Common.TOWER_NUM, towerNum);
				intent.setClass(FaultLocationSystemActivity.this, TowerInfoActivity.class);
				startActivity(intent);
			}

		});

		dbConnection.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_ID1, 0, "查看地图").setIcon(R.drawable.menu_viewmap);
		menu.add(0, MENU_ITEM_ID2, 0, "添加杆塔").setIcon(R.drawable.menu_add);
		menu.add(0, MENU_ITEM_SEARCH_TOWER, 0, "查询杆塔").setIcon(R.drawable.menu_search);
		menu.add(0, MENU_ITEM_IMPORT_DB, 0, "导入数据文件");
		menu.add(0, MENU_ITEM_EXPORT_DB, 0, "导出数据文件");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_ID1:
			Intent intent1 = new Intent();
			intent1.setClass(FaultLocationSystemActivity.this, MapMainActivity.class);
			startActivity(intent1);
			return true;
		case MENU_ITEM_ID2:
			Intent intent2 = new Intent();
			intent2.setClass(FaultLocationSystemActivity.this, EditTowerActivity.class);
			startActivity(intent2);
			return true;
		case MENU_ITEM_SEARCH_TOWER:
			Intent intent3 = new Intent();
			intent3.setClass(FaultLocationSystemActivity.this, TowerListAct.class);
			startActivity(intent3);
			return true;
		case MENU_ITEM_EXPORT_DB :
			try {
				DBConnection.exportDatabaseToSDCard();
				showMessage("导出成功","数据文件已保存至SD卡huodi文件夹！");
			} catch (SDCardNotFoundException e) {
				showMessage("导出失败","SD卡不存在！");
			} catch (FileNotFoundException e) {
				showMessage("导出失败","数据文件没有找到！");
			} catch (TransferDBIOException e) {
				showMessage("导出失败","传输错误，请重新执行此操作！");
			}
			return true;
		case MENU_ITEM_IMPORT_DB:
			try {
				DBConnection.importDatabaseFromSDCard();
				showMessage("导入成功","数据已导入，重启此程序生效！");
			} catch (SDCardNotFoundException e) {
				showMessage("导入失败","SD卡不存在！");
			} catch (FileNotFoundException e) {
				showMessage("导入失败","文件不存在！请确定SD卡huodi文件夹下huodi.db文件是否存在。");
			} catch (TransferDBIOException e) {
				showMessage("导入失败","传输错误，请重新执行此操作！");
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				// Toast.makeText(getApplicationContext(), "result ok",
				// Toast.LENGTH_LONG).show();
			}
		}
	}

	private HashMap<String, String> parseAlert(final Alert alert) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("tower", alert.getTowerNum());

		StringBuilder dataBuilder = new StringBuilder().append("手机号:").append(alert.getAddressNum()).append("\n").append("变电站:").append(alert.getSubstationNum()).append("\n").append("线路:")
				.append(alert.getCircuitNum()).append("\n").append("报警时间：").append(alert.getDate()).append("\n").append("ID:").append(alert.getId());
		map.put("data", dataBuilder.toString());
		return map;
	}

	private HashMap<String, String> parseTower(Tower tower) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("tower", tower.getTowerNum());
		StringBuilder sb = new StringBuilder().append("线路:").append(tower.getCircuit()).append("\n").append("变电站号:").append(tower.getStationNum()).append("\n").append("手机号:")
				.append(tower.getSimNum()).append("\n").append("前杆塔:").append(tower.getPreTowerNum()).append("\n").append("杆塔状态:").append(tower.getStatus());
		map.put("data", sb.toString());
		return map;
	}
	
	private void showMessage(String title ,String message)
    {
        AlertDialog.Builder multiDia=new AlertDialog.Builder(FaultLocationSystemActivity.this);
        multiDia.setTitle(title);
        multiDia.setMessage(message);
        multiDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        multiDia.show();
    }
}