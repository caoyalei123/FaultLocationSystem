package com.liubo.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.liubo.modal.Province;

public class ProvinceDao extends LocationConnection {
	public static final String TABLE_NAME = "province";
	public enum ColumnName {
		Name("name"), Code("code");
		private final String name;

		ColumnName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	public ProvinceDao(Context context) {
		super(context);
	}
	
	public void insert(Province province) {
		ContentValues values = new ContentValues();
		values.put(ColumnName.Name.getName(), province.getName());
		values.put(ColumnName.Code.getName(), province.getCode());
		SQLiteDatabase db = this.getDataBase();
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	public List<Province> getAll() {
		List<Province> provinces = new ArrayList<Province>();
		SQLiteDatabase db = this.getDataBase();
		Cursor cursor =  db.query(TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(ColumnName.Name.getName()));
			String code = cursor.getString(cursor.getColumnIndex(ColumnName.Code.getName()));
			provinces.add(new Province(name, code));
		}
		cursor.close();
		db.close();
		return provinces;
	}
}
