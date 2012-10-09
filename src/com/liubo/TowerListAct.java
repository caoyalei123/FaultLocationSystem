package com.liubo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.liubo.db.TowerDao;
import com.liubo.map.Constant;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

public class TowerListAct extends Activity {
	private static final int MENU_ITEM_MAP = 0;
	private static final int MENU_ITEM_ADD_TOWER = 1;

	ArrayList<HashMap<String, String>> towerInfoList = new ArrayList<HashMap<String, String>>();
	private boolean isSearchPage = false;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tower_list);

		final ListView towerView = (ListView) findViewById(R.id.tower_list_view);
		
		TowerDao towerDao = new TowerDao(this);

		Intent in = this.getIntent();
		Bundle extras = in.getExtras();
		if(extras == null || extras.getString(Constant.Common.TOWER_NUM) == null){
			List<Tower> towers= towerDao.getAllTowers();
			towerDao.close();
			for(Tower tower : towers ){
				towerInfoList.add(towerDao.parseTower(tower));
			}
		}
		else{
			isSearchPage = true;
			String towerNum = extras.getString(Constant.Common.TOWER_NUM);
			List<Tower> towerList = towerDao.getTowersByNum(towerNum);
			for(Tower tower : towerList ){
				towerInfoList.add(towerDao.parseTower(tower));
			}
		}
		SimpleAdapter listTowerAdapter = new SimpleAdapter(this,this.towerInfoList,
				R.layout.alert_tower_adapter, new String[] { "towerId",
						"data" }, new int[] { R.id.alert_tower_title,
				R.id.alert_tower_context});
		towerView.setAdapter(listTowerAdapter);
		
		towerView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				HashMap<String, String> map = towerInfoList.get(arg2);
				String towerNum = map.get("towerId");
				Intent intent = new Intent();
				intent.putExtra(Constant.Common.TOWER_ID, towerNum);
				intent.setClass(TowerListAct.this,
						TowerInfoActivity.class);
				startActivity(intent);
			}
		});
		
		Button searchBtn = (Button) this.findViewById(R.id.search_btn);
		searchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText editText = (EditText) findViewById(R.id.search_tower_num);
				String towerNum = editText.getText().toString();
				if(StringUtils.isNotEmpty(towerNum)){
					Intent to = new Intent();
					to.putExtra(Constant.Common.TOWER_NUM, towerNum);
					to.setClass(TowerListAct.this, TowerListAct.class);
					startActivity(to);
					if (isSearchPage) {
						TowerListAct.this.finish();
					}
				}
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_MAP, 0, "查看地图").setIcon(R.drawable.menu_viewmap);
		menu.add(0, MENU_ITEM_ADD_TOWER, 0, "添加杆塔").setIcon(R.drawable.menu_add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENU_ITEM_MAP:
			Intent intent1 = new Intent();
			intent1.setClass(TowerListAct.this,
					ActivityGroupDemo.class);
			startActivity(intent1);
			return true;
		case MENU_ITEM_ADD_TOWER:
			Intent intent2 = new Intent();
			intent2.setClass(TowerListAct.this,
					EditTowerActivity.class);
			startActivity(intent2);
		return true;
		}
		return false;
	}
	
}
