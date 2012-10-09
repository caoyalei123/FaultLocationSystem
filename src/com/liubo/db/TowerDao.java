package com.liubo.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.liubo.exception.TowerNotUniqueException;
import com.liubo.exception.ZeroNumerException;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

public class TowerDao extends DBConnection {

	public TowerDao(Context context) {
		super(context);
	}
	
	/**
	 * ���������Ϣ
	 * @param tower
	 * @return
	 * @throws TowerNotUniqueException 
	 * @throws ZeroNumerException 
	 */
	public boolean save(Tower tower) throws TowerNotUniqueException, ZeroNumerException{
		SQLiteDatabase db = this.getWritableDatabase();
		
		Date date = new Date();
		String id = date.getTime()+"";
		
		ContentValues cv = new ContentValues();
		
		String gt = tower.getTowerNum();
		String pregt = tower.getPreTowerNum();
		String sim = tower.getSimNum();
		String line = tower.getCircuit();
		String station = tower.getStationNum();
		String longitude = tower.getLongitude();
		String latitude = tower.getLatitude();
		String status = tower.getStatus();
		String comment = tower.getComment();
		
		if(gt.equals("0000")){
			throw new ZeroNumerException("�����Ų���Ϊ0000");
		}
		
		Cursor cursor = db.query("PoleInfoTable", null,"gt = '"+gt+"' and line = '"+line+"' and station = '"+station+"'" , null, null, null, null, null);
		if(cursor.getCount() > 0){
			throw new TowerNotUniqueException("tower not unique");
		}
		
		cv.put("id", id);
		cv.put("gt", gt);
		cv.put("pregt", pregt);
		cv.put("sim", sim);
		cv.put("line", line);
		cv.put("station", station);
		cv.put("longitude", longitude);
		cv.put("latitude", latitude);
		cv.put("status", status);
		cv.put("comment", comment);
		db.insert("PoleInfoTable", null, cv);
		db.close();
		return true;
	}
	
	/**
	 * �޸ĸ�����Ϣ
	 * @param tower
	 * @return
	 * @throws TowerNotUniqueException 
	 * @throws ZeroNumerException 
	 */
	public boolean update(Tower tower) throws TowerNotUniqueException, ZeroNumerException{
		String id = tower.getId();
		
		ContentValues cv = new ContentValues();
		if(StringUtils.isNotEmpty(tower.getLatitude())){
			cv.put("latitude", tower.getLatitude());
		}
		if(StringUtils.isNotEmpty(tower.getLongitude())){
			cv.put("longitude", tower.getLongitude());
		}
		cv.put("gt", tower.getTowerNum());
		cv.put("line", tower.getCircuit());
		cv.put("pregt", tower.getPreTowerNum());
		cv.put("sim", tower.getSimNum());
		cv.put("station", tower.getStationNum());
		cv.put("status", tower.getStatus());
		cv.put("comment", tower.getComment());
		
		if(tower.getTowerNum().equals("0000")){
			throw new ZeroNumerException("�����Ų���Ϊ0000");
		}
		
		Tower origin = this.getTowerById(id);
		
		boolean flag = false;
		if(!origin.getCircuit().equals(tower.getCircuit())  || !origin.getStationNum().equals(tower.getStationNum())){
			flag = true;//��·�ͱ��վ���иĶ�
		}
		
		Cursor cursor = this.getDataBase().query("PoleInfoTable", null,"gt = '"+tower.getTowerNum()+"' and line = '"+tower.getCircuit()+"' and station = '"+tower.getStationNum()+"'" , null, null, null, null, null);
		if(flag && cursor.getCount() > 0){
			throw new TowerNotUniqueException("tower not unique");
		}
		return this.getDataBase().update("PoleInfoTable", cv, "id = '"+id+"'", null) > 0;
	}
	
	public Tower getTowerById(String id){
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query("PoleInfoTable", null,"id = '"+id+"' " , null, null, null, null, null);
		if(cursor != null && cursor.moveToFirst()){
			Tower tower = fillTowerInfo(cursor);
			cursor.close();
			return tower;
		}
		cursor.close();
		db.close();
		return null;
	}
	
	/**
	 * ͨ�������š���·�š����վ�� ��ѯΨһ����
	 * @param towerNum
	 * @param circuitNum
	 * @param stationNum
	 * @return
	 * @throws TowerNotUniqueException
	 */
	public Tower getTower(String towerNum ,String circuitNum , String stationNum) throws TowerNotUniqueException{
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query("PoleInfoTable", null,"gt = '"+towerNum+"' and line = '" +circuitNum+"' and station = '"+stationNum+"'" , null, null, null, null, null);
		if(cursor.getCount() > 1){
			throw new TowerNotUniqueException("the tower is not unique");
		}
		if(cursor != null && cursor.moveToFirst()){
			Tower tower = fillTowerInfo(cursor);
			cursor.close();
			return tower;
		}
		cursor.close();
		db.close();
		return null;
	}
	
	/**
	 * ͨ��idɾ������
	 * @param id
	 * @return
	 */
	public boolean delete(String id){
		return this.getDataBase().delete("PoleInfoTable", "id = '"+id+"'", null) > 0;
	}
	
	/**
	 * ��ѯ���и���
	 * @param db
	 * @return
	 */
	public List<Tower> getAllTowers(){
		List<Tower> towers = new ArrayList<Tower>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor poleCursor = db.query("PoleInfoTable", null, null, null, null, null, null);
		towers = fillTowerListInfo(poleCursor);
		poleCursor.close();
		return towers;
	}
	
//	/**
//	 * ���ݸ����Ų�ѯ����
//	 * @param towerNum
//	 * @return
//	 */
//	public Tower getTowerByNum(String towerNum ){
//		SQLiteDatabase db = this.getWritableDatabase();
//		Cursor cursor = db.query("PoleInfoTable", null,"gt = '"+towerNum+"'", null, null, null, null, null);
//		if(cursor != null && cursor.moveToFirst()){
//			Tower tower = fillTowerInfo(cursor);
//			cursor.close();
//			return tower;
//		}
//		cursor.close();
//		db.close();
//		return null;
//	}
	
	/**
	 * ���ݸ����Ų�ѯ����
	 * @param towerNum
	 * @return
	 */
	public List<Tower> getTowersByNum(String towerNum ){
		SQLiteDatabase db = this.getWritableDatabase();
		List<Tower> towerList=  new ArrayList<Tower>();
		Cursor cursor = db.query("PoleInfoTable", null,"gt = '"+towerNum+"'", null, null, null, null, null);
		if(cursor != null && cursor.moveToFirst()){
			do{
				Tower tower = fillTowerInfo(cursor);
				towerList.add(tower);
			}while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return towerList;
	}
	
	/**
	 * �޸ĸ���״̬
	 * @param towerNum
	 * @param status
	 */
	public void updateTowerStatusById(String id , String status){
		ContentValues cv = new ContentValues();
		cv.put("status", status);
		getDataBase().update("PoleInfoTable", cv, "id = '"+id+"'", null);
		getDataBase().close();
	}
	
	
	/**
	 * ͨ��alert ��� tower��Ϣ
	 * @param alert
	 * @return
	 * @throws TowerNotUniqueException 
	 */
	/*public Tower getTowerByAlert(Alert alert) throws TowerNotUniqueException{
		String towerNum = alert.getTowerNum();
		String circuitNum = alert.getCircuitNum();
		String stationNum = alert.getSubstationNum();
		return this.getTower(towerNum, circuitNum, stationNum);
	}*/
	
	/**
	 * ͨ���ֻ���ȷ������
	 * @param smsNumber
	 * @return
	 * @throws TowerNotUniqueException
	 */
	public Tower getTowerBySMSNumber(String smsNumber) throws TowerNotUniqueException{
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query("PoleInfoTable", null,"sim ='"+smsNumber+"'" , null, null, null, null, null);
		if(cursor.getCount() > 1){
			throw new TowerNotUniqueException("the tower is not unique");
		}
		if(cursor != null && cursor.moveToFirst()){
			Tower tower = fillTowerInfo(cursor);
			cursor.close();
			return tower;
		}
		cursor.close();
		db.close();
		return null;
	}
	
	/**
	 * �鿴�б����ĸ���
	 * @return
	 * @throws TowerNotUniqueException 
	 */
	public List<Tower> getAlertTowerList(Context context) throws TowerNotUniqueException{
		AlertDao alertDao = new AlertDao(context);
		List<Alert> alertList = alertDao.getAllAlerts();
		alertDao.close();
		List<Tower> alertTowerList = new ArrayList<Tower>();
		List<String> towerNumList = new ArrayList<String>();
		for(Alert alert : alertList){
			Tower tower = this.getTowerBySMSNumber(alert.getAddressNum());
			if(tower != null){
				if(!towerNumList.contains(tower.getTowerNum())){
					alertTowerList.add(tower);
					towerNumList.add(tower.getTowerNum());
				}
			}
			this.close();
		}
		return alertTowerList;
	}
	
	/**
	 * �������б���Ϣ
	 * @param poleCursor
	 * @return
	 */
	private List<Tower> fillTowerListInfo(Cursor poleCursor){
		List<Tower> towers = new ArrayList<Tower>();
		if(poleCursor != null && poleCursor.moveToFirst()){
			do{
				towers.add(fillTowerInfo(poleCursor));
			}while(poleCursor.moveToNext());
		}
		return towers;
	}
	
	private Tower fillTowerInfo(Cursor poleCursor){
		String id = poleCursor.getString(poleCursor.getColumnIndex("id"));
		String towerNum = poleCursor.getString(poleCursor.getColumnIndex("gt"));
		String preTower = poleCursor.getString(poleCursor.getColumnIndex("pregt"));
		String sim = poleCursor.getString(poleCursor.getColumnIndex("sim"));
		String line = poleCursor.getString(poleCursor.getColumnIndex("line"));
		String station = poleCursor.getString(poleCursor.getColumnIndex("station"));
		String longtitude = poleCursor.getString(poleCursor.getColumnIndex("longitude"));
		String latitude = poleCursor.getString(poleCursor.getColumnIndex("latitude"));
		String status = poleCursor.getString(poleCursor.getColumnIndex("status"));
		String comment = poleCursor.getString(poleCursor.getColumnIndex("comment"));
		
		Tower tower = new Tower();
		tower.setId(id);
		tower.setTowerNum(towerNum);
		tower.setPreTowerNum(preTower);
		tower.setSimNum(sim);
		tower.setCircuit(line);
		tower.setStationNum(station);
		tower.setLatitude(latitude);
		tower.setLongitude(longtitude);
		tower.setStatus(status);
		tower.setComment(comment);
		return tower;
	}

	public HashMap<String ,String> parseTower(Tower tower){
		HashMap<String ,String> map = new HashMap<String,String>();
		map.put("towerId", "����ID:"+tower.getId());
		StringBuilder sb = new StringBuilder()
								.append("����:").append(tower.getTowerNum()).append("\n")
								.append("��·:").append(tower.getCircuit()).append("\n")
								.append("���վ��:").append(tower.getStationNum()).append("\n")
								.append("����:").append(tower.getDisplayLogitude()).append("\n")
								.append("γ��:").append(tower.getDisplayLatitude()).append("\n")
								.append("�ֻ���:").append(tower.getSimNum()).append("\n")
								.append("ǰ����:").append(tower.getPreTowerNum()).append("\n");
		if(StringUtils.isNotEmpty(tower.getComment()))
		{
			sb.append("��ע:").append(tower.getComment()).append("\n");
		}
		sb.append("����״̬:").append(tower.getStatus());
		map.put("data", sb.toString());
		return map;
	}
	
	public HashMap<String ,String> parseAlertTower(Tower tower,Context context){
		HashMap<String ,String> map = new HashMap<String,String>();
		map.put("towerId", tower.getId());
		AlertDao alertDao = new AlertDao (context);
		List<Alert> alertList = alertDao.getAlertsByTowerId(tower.getId());
		int alertListSize = 0;
		if(alertList != null ){
			alertListSize = alertList.size();
		}
		String _status = tower.getStatus();
		StringBuilder sb = new StringBuilder()
								.append("������:").append(tower.getTowerNum()).append("\n")
								.append("����������").append(alertListSize).append("\n")
								.append("����״̬��").append(_status);
		map.put("data", sb.toString());
		return map;
	}
}
