package com.liubo.modal;

/**
 * ±‰µÁ’æ
 * @author bo.liu-1
 *
 */
public class Substation {
	
	private String id;
	private String substationNum;
	private String name;
	
	public static final String TABLE_NAME = "d_substation";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_SUBSTATION_NUM = "substation_num";
	public static final String COLUMN_NAME = "name";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSubstationNum() {
		return substationNum;
	}
	public void setSubstationNum(String substationNum) {
		this.substationNum = substationNum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	

}
