package com.liubo.modal;

/**
 * ��ͼλ��
 * @author bo.liu-1
 *
 */
public class Location {

	private String id;
	private String longitude;//����
	private String latitude;//γ��
	
	public static final String TABLE_NAME = "d_location";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_longitude="longtitute";
	public static final String COLUMN_latitude = "latitude";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getlongitude() {
		return longitude;
	}
	public void setlongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
}
