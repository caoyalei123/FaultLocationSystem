package com.liubo.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.liubo.modal.Alert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AlertDao extends DBConnection {

	public AlertDao(Context context) {
		super(context);
	}

	/**
	 * 新建报警信息
	 * @param alert
	 */
	public void save(Alert alert){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", new Date().getTime()+"");
		values.put("tower_id", alert.getTowerId());
		values.put("substation_num", alert.getSubstationNum());
		values.put("circuit_num", alert.getCircuitNum());
		values.put("tower_num", alert.getTowerNum());
		values.put("date", alert.getDate());
		values.put("address_num", alert.getAddressNum());
		db.insert("alert", null, values);
	}
	
	/**
	 * 查询全部报警信息
	 * @return
	 */
	public List<Alert> getAllAlerts(){
		List<Alert> alertList = new ArrayList<Alert>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur = db.query("alert", null, null, null, null, null, "id desc");
		if (cur != null && cur.moveToFirst()) {
			do{
				Alert alert = this.fillAlert(cur);
				alertList.add(alert);
			}while(cur.moveToNext());
		}
		cur.close();
		return alertList;
	}
	
	public Alert getAlertById(String id){
		Alert alert = new Alert();
		Cursor cur = this.getDataBase().query("alert", null, "id = '"+id+"'", null, null, null, null);
		if(cur == null){
			return null;
		}
		if(cur != null){
			if(cur.getCount() > 1 ){
				return null;
			}
			if(cur.getCount() == 1 && cur.moveToFirst()){
				alert = this.fillAlert(cur);
			}
		}
		return alert;
	}
	
//	/**
//	 * 查询杆塔的告警记录
//	 * @param towerNum
//	 * @return
//	 */
//	public List<Alert> getAlertsByTowerNum(String towerNum){
//		List<Alert> alertList = new ArrayList<Alert>();
//		Cursor cur = getDataBase().query("alert", null, "tower_num = '"+towerNum+"'", null, null, null, "id desc");
//		if (cur != null && cur.moveToFirst()) {
//			do{
//				Alert alert = fillAlert(cur);
//				alertList.add(alert);
//			}while(cur.moveToNext());
//		}
//		cur.close();
//		return alertList;
//	}
	
	/**
	 * 查询杆塔的告警记录
	 * @param towerNum
	 * @return
	 */
	public List<Alert> getAlertsByTowerId(String towerId){
		List<Alert> alertList = new ArrayList<Alert>();
		Cursor cur = getDataBase().query("alert", null, "tower_id = '"+towerId +"'", null, null, null, "id desc");
		if (cur != null && cur.moveToFirst()) {
			do{
				Alert alert = fillAlert(cur);
				alertList.add(alert);
			}while(cur.moveToNext());
		}
		cur.close();
		return alertList;
	}
	
	/**
	 * 将数据库库中数据库填入到对象中
	 * @param cursor
	 * @return
	 */
	public Alert fillAlert(Cursor cur){
		Alert alert = new Alert();
		String id = cur.getString(0);
		String towerId = cur.getString(cur.getColumnIndex("tower_id"));
		String substationNum = cur.getString(cur
				.getColumnIndex("substation_num"));
		String circuitNum = cur.getString(cur
				.getColumnIndex("circuit_num"));
		String towerNum = cur
				.getString(cur.getColumnIndex("tower_num"));
		String date = cur.getString(cur.getColumnIndex("date"));
		String address = cur.getString(cur.getColumnIndex("address_num"));
		
		alert.setId(id);
		alert.setTowerId(towerId);
		alert.setSubstationNum(substationNum);
		alert.setCircuitNum(circuitNum);
		alert.setTowerNum(towerNum);
		alert.setDate(date);
		alert.setAddressNum(address);
		return alert;
	}
	
	public HashMap<String, String> parseAlert(Alert alert) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("towerId", alert.getTowerId());
		map.put("data", alert.toString());
		return map;
	}
}
