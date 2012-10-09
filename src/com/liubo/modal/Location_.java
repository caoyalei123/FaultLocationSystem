package com.liubo.modal;

public class Location_ {
	private final String name;
	private final String code;
	private final String latitude;
	private final String longitude;
	public Location_(String name, String code, String latitude, String longitude) {
		this.name = name;
		this.code = code;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public Location_ copy(Location_ location) {
		if (location == null) {
			return null;
		}
		return new Location_(location.getName(), location.getCode(), location.getLatitude(), location.getLongitude());
	}
	public String getName() {
		return name;
	}
	public String getCode() {
		return code;
	}
	public String getLatitude() {
		return latitude;
	}
	public int getLatE6() {
		return Integer.parseInt(latitude);
	}
	public int getLongE6() {
		return Integer.parseInt(longitude);
	}
	public String getLongitude() {
		return longitude;
	}
	@Override
	public String toString() {
		return "name[" + name + "],code[" + code +"],latitude["+ latitude + "],longitude[" + longitude+"]";
	}
	@Override
	public int  hashCode() {
		return toString().hashCode();
	}
}
