package com.liubo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.liubo.db.AlertDao;
import com.liubo.db.DBConnection;
import com.liubo.db.TowerDao;
import com.liubo.exception.SDCardNotFoundException;
import com.liubo.exception.TransferDBIOException;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;
import com.liubo.util.MessageUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DashboardActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.dashboard);
		ListView listView = (ListView) this.findViewById(R.id.ListView01);
		ArrayList<HashMap<String,Object>> listItem = new ArrayList<HashMap<String ,Object>>();
		
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
		
		for(int i = 0 ; i < 3 ; i++ ){
			/*if(i == 0){
				HashMap<String ,Object > map = new HashMap<String, Object>();
				map.put("ItemImage", R.drawable.mark_error);
				map.put("ItemTitle", "历史故障信息["+alertSize+"]");
				map.put("LastImage", R.drawable.btn_right);
				listItem.add(map);
			}else if(i == 1){
				HashMap<String ,Object > map = new HashMap<String ,Object>();
				map.put("ItemImage", R.drawable.tower);
				map.put("ItemTitle", "杆塔信息["+towerSize+"]");
				map.put("LastImage", R.drawable.btn_right);
				listItem.add(map);
			}
			else */ 
			if(i == 0){
				HashMap<String ,Object> map = new HashMap<String ,Object>();
				map.put("ItemImage", R.drawable.menu_add);
				map.put("ItemTitle", "添加杆塔");
				map.put("LastImage", R.drawable.btn_right);
				listItem.add(map);
			}
			/*else if(i == 3){
				HashMap<String ,Object> map = new HashMap<String ,Object>();
				map.put("ItemImage", R.drawable.menu_viewmap);
				map.put("ItemTitle", "查看地图");
				map.put("LastImage", R.drawable.btn_right);
				listItem.add(map);
			}*/
			else if(i == 1){
				HashMap<String ,Object> map = new HashMap<String ,Object>();
				map.put("ItemImage", R.drawable.menu_export);
				map.put("ItemTitle", "导出数据");
				map.put("LastImage", R.drawable.btn_right);
				listItem.add(map);
			}
			else if(i == 2){
				HashMap<String ,Object> map = new HashMap<String ,Object>();
				map.put("ItemImage", R.drawable.menu_import);
				map.put("ItemTitle", "导入数据");
				map.put("LastImage", R.drawable.btn_right);
				listItem.add(map);
			}
		}
		
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, 
				listItem, R.layout.list_item, 
				new String[]{"ItemImage","ItemTitle","LastImage"}, 
				new int[]{R.id.ItemImage,R.id.ItemTitle,R.id.last});
		listView.setAdapter(listItemAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/*if(arg2 == 0){//告警
					Intent intent  = new Intent();
					intent.setClass(DashboardActivity.this, AlertTowerListActivity.class);
					startActivity(intent);
				}
				else if(arg2 == 1){//杆塔信息
					Intent intent = new Intent();
					intent.setClass(DashboardActivity.this, TowerListAct.class);
					startActivity(intent);
				}
				else
				*/
				if(arg2 == 0){//添加杆塔(杆塔设置)
					Intent intent2 = new Intent();
					intent2.setClass(DashboardActivity.this, EditTowerActivity.class);
					startActivity(intent2);
				}
				/*else if(arg2 == 3){//地图
					Intent intent = new Intent();
					intent.setClass(DashboardActivity.this
							, MapMainActivity.class);
					startActivity(intent);
				}*/
				else if(arg2 == 1){
					try {
						DBConnection.exportDatabaseToSDCard();
						MessageUtil.showMessage(DashboardActivity.this, "导出成功","数据文件已保存至SD卡huodi文件夹！");
					} catch (SDCardNotFoundException e) {
						MessageUtil.showMessage(DashboardActivity.this, "导出失败","SD卡不存在！");
					} catch (FileNotFoundException e) {
						MessageUtil.showMessage(DashboardActivity.this, "导出失败","数据文件没有找到！");
					} catch (TransferDBIOException e) {
						MessageUtil.showMessage(DashboardActivity.this, "导出失败","传输错误，请重新执行此操作！");
					}
				}
				else if(arg2 == 2){
					try {
						DBConnection.importDatabaseFromSDCard();
						MessageUtil.showMessage(DashboardActivity.this,"导入成功","数据已导入，重启此程序生效！");
					} catch (SDCardNotFoundException e) {
						MessageUtil.showMessage(DashboardActivity.this,"导入失败","SD卡不存在！");
					} catch (FileNotFoundException e) {
						MessageUtil.showMessage(DashboardActivity.this,"导入失败","文件不存在！请确定SD卡huodi文件夹下huodi.db文件是否存在。");
					} catch (TransferDBIOException e) {
						MessageUtil.showMessage(DashboardActivity.this,"导入失败","传输错误，请重新执行此操作！");
					}
				}
			}
		});
	}
}
