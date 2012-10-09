package com.liubo.modal;

/**
 * ÏßÂ·
 * @author bo.liu-1
 *
 */
public class Circuit {

	private String id;
	private String circuitNum;
	private String name;
	private String substationNum;
	
	public static final String TABLE_NAME 	= "d_circuit";
	public static final String COLUMN_ID	= "id";
	public static final String COLUMN_NAME	= "name";
	public static final String COLUMN_SUBSTATION_NUM = "substation_num";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCircuitNum() {
		return circuitNum;
	}
	public void setCircuitNum(String circuitNum) {
		this.circuitNum = circuitNum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubstationNum() {
		return substationNum;
	}
	public void setSubstationNum(String substationNum) {
		this.substationNum = substationNum;
	}
	
}
