package com.liubo.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.liubo.modal.Location;
import com.liubo.modal.Location_;
public class LocationDao extends LocationConnection {
	public static final String TABLE_NAME = "location";
	public enum ColumnName {
		Name("name"), Code("code"),
		Latitude("latitude"), Longitude("longitude");
		private final String name;

		ColumnName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	public LocationDao(Context context) {
		super(context);
	}
	public void insert(Location_ location) {
		ContentValues values = toContentValues(location);
		insert(values);
	}
	public ContentValues toContentValues(Location_ location) {
		ContentValues values = new ContentValues();
		values.put(ColumnName.Name.getName(), location.getName());
		values.put(ColumnName.Code.getName(), location.getCode());
		values.put(ColumnName.Latitude.getName(), location.getLatitude());
		values.put(ColumnName.Longitude.getName(), location.getLongitude());
		return values;
	}
	public void update(Location_ location) {
		ContentValues values = new ContentValues();
		values.put(ColumnName.Latitude.getName(), location.getLatitude());
		values.put(ColumnName.Longitude.getName(), location.getLongitude());
		SQLiteDatabase db = this.getDataBase();
		int count  = db.update(TABLE_NAME, values, ColumnName.Code.getName() + " = '" + location.getCode() + "'", null);
		db.close();
		Log.e("update", "affected" + count + "row");
	}
	public void insert(ContentValues values) {
		SQLiteDatabase db = this.getDataBase();
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	public List<Location_> getAllLocations() {
		List<Location_> allLocations = new ArrayList<Location_>();
		SQLiteDatabase db = this.getDataBase();
		Cursor cursor =  db.query(TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(ColumnName.Name.getName()));
			String code = cursor.getString(cursor.getColumnIndex(ColumnName.Code.getName()));
			String latitude = cursor.getString(cursor.getColumnIndex(ColumnName.Latitude.getName()));
			String longitude = cursor.getString(cursor.getColumnIndex(ColumnName.Longitude.getName()));
			allLocations.add(new Location_(name, code, latitude, longitude));
		}
		cursor.close();
		db.close();
		return allLocations;
	}
	public Location_ getLocationByCode(String code) {
		SQLiteDatabase db = this.getDataBase();
		Cursor cursor = db.query(TABLE_NAME, null, ColumnName.Code.getName() + " = '" + code + "'", null, null, null, null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(ColumnName.Name.getName()));
			String pcode = cursor.getString(cursor.getColumnIndex(ColumnName.Code.getName()));
			String latitude = cursor.getString(cursor.getColumnIndex(ColumnName.Latitude.getName()));
			String longitude = cursor.getString(cursor.getColumnIndex(ColumnName.Longitude.getName()));
			return new Location_(name, pcode, latitude, longitude);
		}
		return null;
	}
	public Map<String, Location_> getAllLocationInMap() {
		Map<String, Location_> locationMap = new HashMap<String, Location_>();
		List<Location_> locations = this.getAllLocations();
		for (Location_ location: locations) {
			locationMap.put(location.getCode(), location);
		}
		return locationMap;
	}	
}
