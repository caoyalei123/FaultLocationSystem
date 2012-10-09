package com.liubo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.liubo.db.DBConnection;

/**
 * Init Database
 * @author bo.liu-1
 *
 */
public class InitActivity extends Activity {

	private static String dbName = DBConnection.DATABASE_NAME;//database name
	private static String DATABASE_PATH="/data/data/com.liubo/databases/";//数据库在手机里的路径
	private int alpha = 255;//???
	private int b = 0 ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(null);
		//判断数据库是否存在
		boolean dbExist = checkDataBase();
		if(dbExist){//不存在就吧raw里面的数据库写入手机
			try{
				copyDataBase();
			}catch(IOException e){
				throw new Error("Error copying database");
			}
		}
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				initApp();//初始化程序
			}
		});
	}
	public void initApp() {
		// TODO Auto-generated method stub
	}
	private void copyDataBase()throws IOException {
		String databaseFileName = DATABASE_PATH + dbName;
		File dir = new File(DATABASE_PATH);
		if(!dir.exists()){
			dir.mkdirs();
			FileOutputStream fos = null;
			try{
				fos = new FileOutputStream(databaseFileName);
			}catch(Exception e){
				e.printStackTrace();
			}
			InputStream is = this.getResources().openRawResource((Integer) null);
			byte[] buffer = new byte[8192];
			int count =  0 ; 
			try{
				while((count = is.read(buffer))> 0){
					fos.write(buffer,0,count);
					fos.flush();
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				is.close();
				fos.close();
			}
		}
		
	}
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try{
			String dataBaseFileName = DATABASE_PATH + dbName;
			checkDB = SQLiteDatabase.openDatabase(dataBaseFileName, null, SQLiteDatabase.OPEN_READONLY);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(checkDB != null ){
				checkDB.close();
			}
		}
		return checkDB !=null;
	}
	
}
