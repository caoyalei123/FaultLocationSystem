package com.liubo.modal;

import com.baidu.mapapi.CoordinateConvert;
import com.baidu.mapapi.GeoPoint;
import com.liubo.util.StringUtils;

/**
 * 杆塔
 * 
 * @author bo.liu-1
 * 
 */
public class Tower {
	public enum Status {
		NORMAL("正常"), ERROR("故障");
		public final String value;

		private Status(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private String id;
	private String name;
	private String towerNum;
	private String circuit;
	private String longitude;// 经度
	private String latitude;// 纬度
	private String status = Status.NORMAL.toString();

	private String preTowerNum;
	private String simNum;
	private String stationNum;
	
	private String comment;//备注信息

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTowerNum() {
		return towerNum;
	}

	public void setTowerNum(String towerNum) {
		this.towerNum = towerNum;
	}

	public String getCircuit() {
		return circuit;
	}

	public void setCircuit(String circuit) {
		this.circuit = circuit;
	}

	public String getStatus() {
		if (StringUtils.isNotEmpty(status)) {
			return status;
		}
		return "正常";
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getDisplayLogitude() {
		if (longitude == null) {
			return "";
		}
		String[] longitudeArray = getLongitudeArray();
		return longitudeArray.length > 0 ? (longitudeArray[0] + ":" + longitudeArray[1] + "." + longitudeArray[2] + longitudeArray[3]):"";
	}
	public String getLongitude() {
		if (longitude == null) {
			return "";
		}
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		if (latitude == null) {
			return "";
		}
		return latitude;
	}
	public String getDisplayLatitude() {
		if (latitude == null) {
			return "";
		}
		String[] latitudeArray =  this.getLatitudeArray();
		return latitudeArray.length > 0 ? (latitudeArray[0] + ":" + latitudeArray[1] + "." + latitudeArray[2] + latitudeArray[3]) : "";
	}
	// 得到纬度的数组
	// 南北纬度，度 分秒
	public String[] getLatitudeArray() {
		return getStringArrayBy(latitude, "纬");
	}
	private String[] getStringArrayBy(String data, String word) {
		String[] array = new String[0];
		if (data != null && data.length() > 0 && data.indexOf(word) > 0) {
			int i = 0;
			int indexStart = 0;
			int indexEnd = indexStart;
			array = new String[4];
			for (; i < array.length - 1; i++) {
				indexEnd = data.indexOf(',', indexStart);
				if (indexEnd <= 0) {
					break;
				}
				array[i] = data.substring(indexStart, indexEnd);
				indexStart = indexEnd + 1;
			}
			if (i == array.length - 1) {
				array[i] = data.substring(indexStart);
				return array;
			}
		}
		return array;
	}
	// 得到经度的数组
	public String[] getLongitudeArray() {
		return this.getStringArrayBy(longitude, "经");
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getPreTowerNum() {
		return preTowerNum;
	}

	public void setPreTowerNum(String preTowerNum) {
		this.preTowerNum = preTowerNum;
	}

	public String getSimNum() {
		return simNum;
	}

	public void setSimNum(String simNum) {
		this.simNum = simNum;
	}

	public String getStationNum() {
		return stationNum;
	}

	public void setStationNum(String stationNum) {
		this.stationNum = stationNum;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isError() {
		if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase(Status.ERROR.value)) {
			return true;
		}
		return false;
	}
	private float strTofloatSecond(String str) {
		return (float) (Integer.parseInt(str) * 60.00f / (Math.pow(10, str.length())));
	}
	public GeoPoint getGeoPoint() {
		do {
			String[] longArray = this.getLongitudeArray();
			String[] latArray = this.getLatitudeArray();
			if (longArray.length == 4 && latArray.length == 4) {
				// 经度
				String longDegree = longArray[1];
				String longMinute = longArray[2];
				String longSecond = longArray[3];
				if (!StringUtils.isNotEmpty(longDegree) || !StringUtils.isNotEmpty(longMinute) || !StringUtils.isNotEmpty(longSecond)) {
					break;
				}
				String latDegree = latArray[1];
				String latMinute = latArray[2];
				String latSecond = latArray[3];
				if (!StringUtils.isNotEmpty(latDegree) || !StringUtils.isNotEmpty(latMinute) || !StringUtils.isNotEmpty(latSecond)) {
					break;
				}
				int longE6 = (int) ((Integer.parseInt(longDegree) * 3600 + Integer.parseInt(longMinute) * 60 + strTofloatSecond(longSecond)) * 1000000.00d / 3600.00d);
				if (longArray[0].equals("西经")) {
					longE6 = 0 - longE6;
				}
				int latE6 = (int) ((Integer.parseInt(latDegree) * 3600 + Integer.parseInt(latMinute) * 60 + strTofloatSecond(latSecond)) * 1000000.00d / 3600.00d);
				if (latArray[0].equals("南纬")) {
					latE6 = 0 - latE6;
				}
				GeoPoint inPoint = new GeoPoint(latE6, longE6);
				return CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(inPoint));
			}
		} while (false);
		return null;
	}
}
