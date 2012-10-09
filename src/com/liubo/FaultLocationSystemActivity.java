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

		/* ��ʼ������ */
		tabHost = getTabHost();
		
		tabHost.addTab(tabHost.newTabSpec("tab_1").setIndicator("��ʷ������Ϣ", getResources().getDrawable(R.drawable.ic_list_alert_sms_failed)).setContent(R.id.textview1));
		tabHost.addTab(tabHost.newTabSpec("tab_2").setIndicator("������Ϣ", getResources().getDrawable(R.drawable.tower)).setContent(R.id.textview2));
		tabHost.addTab(tabHost.newTabSpec("tab_3").setIndicator("��ͼ��Ϣ", getResources().getDrawable(R.drawable.menu_viewmap)).setContent(R.id.textview1));

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
			Toast.makeText(this.getApplicationContext(), "ϵͳ��ʾ��������Ϣ�г�ͻ���������ݡ�", Toast.LENGTH_LONG).show();
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
		menu.add(0, MENU_ITEM_ID1, 0, "�鿴��ͼ").setIcon(R.drawable.menu_viewmap);
		menu.add(0, MENU_ITEM_ID2, 0, "��Ӹ���").setIcon(R.drawable.menu_add);
		menu.add(0, MENU_ITEM_SEARCH_TOWER, 0, "��ѯ����").setIcon(R.drawable.menu_search);
		menu.add(0, MENU_ITEM_IMPORT_DB, 0, "���������ļ�");
		menu.add(0, MENU_ITEM_EXPORT_DB, 0, "���������ļ�");
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
				showMessage("�����ɹ�","�����ļ��ѱ�����SD��huodi�ļ��У�");
			} catch (SDCardNotFoundException e) {
				showMessage("����ʧ��","SD�������ڣ�");
			} catch (FileNotFoundException e) {
				showMessage("����ʧ��","�����ļ�û���ҵ���");
			} catch (TransferDBIOException e) {
				showMessage("����ʧ��","�������������ִ�д˲�����");
			}
			return true;
		case MENU_ITEM_IMPORT_DB:
			try {
				DBConnection.importDatabaseFromSDCard();
				showMessage("����ɹ�","�����ѵ��룬�����˳�����Ч��");
			} catch (SDCardNotFoundException e) {
				showMessage("����ʧ��","SD�������ڣ�");
			} catch (FileNotFoundException e) {
				showMessage("����ʧ��","�ļ������ڣ���ȷ��SD��huodi�ļ�����huodi.db�ļ��Ƿ���ڡ�");
			} catch (TransferDBIOException e) {
				showMessage("����ʧ��","�������������ִ�д˲�����");
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

		StringBuilder dataBuilder = new StringBuilder().append("�ֻ���:").append(alert.getAddressNum()).append("\n").append("���վ:").append(alert.getSubstationNum()).append("\n").append("��·:")
				.append(alert.getCircuitNum()).append("\n").append("����ʱ�䣺").append(alert.getDate()).append("\n").append("ID:").append(alert.getId());
		map.put("data", dataBuilder.toString());
		return map;
	}

	private HashMap<String, String> parseTower(Tower tower) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("tower", tower.getTowerNum());
		StringBuilder sb = new StringBuilder().append("��·:").append(tower.getCircuit()).append("\n").append("���վ��:").append(tower.getStationNum()).append("\n").append("�ֻ���:")
				.append(tower.getSimNum()).append("\n").append("ǰ����:").append(tower.getPreTowerNum()).append("\n").append("����״̬:").append(tower.getStatus());
		map.put("data", sb.toString());
		return map;
	}
	
	private void showMessage(String title ,String message)
    {
        AlertDialog.Builder multiDia=new AlertDialog.Builder(FaultLocationSystemActivity.this);
        multiDia.setTitle(title);
        multiDia.setMessage(message);
        multiDia.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        multiDia.show();
    }
}