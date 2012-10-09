package com.liubo.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.liubo.modal.City;

public class CityDao  extends LocationConnection{
	public static final String TABLE_NAME = "city";
	public enum ColumnName {
		Name("name"), Code("code"),PCode("pcode");
		private final String name;
		ColumnName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	public CityDao(Context context) {
		super(context);
	}
	
	public void insert(City city) {
		ContentValues values = new ContentValues();
		values.put(ColumnName.Name.getName(), city.getName());
		values.put(ColumnName.Code.getName(), city.getCode());
		values.put(ColumnName.PCode.getName(), city.getPcode());
		SQLiteDatabase db = this.getDataBase();
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	public List<City> getAll() {
		List<City> cities = new ArrayList<City>();
		SQLiteDatabase db = this.getDataBase();
		Cursor cursor =  db.query(TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(ColumnName.Name.getName()));
			String code = cursor.getString(cursor.getColumnIndex(ColumnName.Code.getName()));
			String pcode = cursor.getString(cursor.getColumnIndex(ColumnName.PCode.getName()));
			cities.add(new City(name, code, pcode));
		}
		cursor.close();
		db.close();
		return cities;
	}
	public List<City> getCitiesByPCode(String pcode) {
		List<City> cities = new ArrayList<City>();
		SQLiteDatabase db = this.getDataBase();
		Cursor cursor =  db.query(TABLE_NAME, null, ColumnName.PCode.getName() + " = '" + pcode + "'", null, null, null, null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(ColumnName.Name.getName()));
			String code = cursor.getString(cursor.getColumnIndex(ColumnName.Code.getName()));
//			String pcode = cursor.getString(cursor.getColumnIndex(ColumnName.PCode.getName()));
			cities.add(new City(name, code, pcode));
		}
		cursor.close();
		db.close();
		return cities;
	}
}
