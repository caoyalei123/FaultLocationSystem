package com.liubo.modal;

/**
 * 告警
 * @author bo.liu-1
 *
 */
public class Alert {

	private String id ;
	private String towerId;
	private String addressNum;
	private String info;
	private String substationNum;
	private String circuitNum;
	private String towerNum;
	private String date;
	private boolean isOK;
	
//	public static final String TABLE_NAME 				= "d_alert";
//	public static final String COLUMN_ID 				= "id";
//	public static final String COLUMN_ADDR 				= "address";
//	public static final String COLUMN_MSG_CONTENT 		= "msg_content";
//	public static final String COLUMN_SUBSTATION_NUM 	= "substation_num";
//	public static final String COLUMN_CIRCUIT_NUM 		= "circuit_num";
//	public static final String COLUMN_TOWER_NUM 		= "tower_num";
//	public static final String COLUMN_DATE 				= "date";
//	public static final String COLUMN_ISOK 				= "is_ok";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTowerId() {
		return towerId;
	}
	public void setTowerId(String towerId) {
		this.towerId = towerId;
	}
	public String getAddressNum() {
		return addressNum;
	}
	public void setAddressNum(String addressNum) {
		this.addressNum = addressNum;
	}
	public String getCircuitNum() {
		return circuitNum;
	}
	public void setCircuitNum(String circuitNum) {
		this.circuitNum = circuitNum;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getSubstationNum() {
		return substationNum;
	}
	public void setSubstationNum(String substationNum) {
		this.substationNum = substationNum;
	}
	public String getTowerNum() {
		return towerNum;
	}
	public void setTowerNum(String towerNum) {
		this.towerNum = towerNum;
	}
	
	public boolean isOK() {
		return isOK;
	}
	public void setOK(boolean isOK) {
		this.isOK = isOK;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb
			  .append("杆塔号：").append(towerNum).append("\n")
			  .append("线路号：").append(circuitNum).append("\n")
			  .append("变电站号：").append(substationNum).append("\n")
			  .append("手机号：").append(addressNum).append("\n")
			  .append("报警时间：").append(date).toString();
	}
	public static void main(String[] args) {
		Alert alert = new Alert();
			alert.setAddressNum("13100003333");
			alert.setInfo("alert");
			alert.setCircuitNum("circirt10001");
			alert.setTowerNum("Tower1001");
			alert.setSubstationNum("sbsttn1001");
			System.out.println(alert.toString());
	}
}
