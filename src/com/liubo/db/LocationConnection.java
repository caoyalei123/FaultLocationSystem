package com.liubo.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.liubo.exception.SDCardNotFoundException;
import com.liubo.exception.TransferDBIOException;

public class LocationConnection{

	public static final String DATABASE_PATH = Environment.getDataDirectory() + File.separator + "data" + File.separator + "com.liubo" + File.separator + "databases" + File.separator;
	public final static String DATABASE_NAME = "location";

	private static final String CREATE_ADDRESS_TABLE = "CREATE TABLE " + LocationDao.TABLE_NAME
	+ " (id text primary key , " + LocationDao.ColumnName.Name.getName() + " text , " + LocationDao.ColumnName.Code.getName() + " text , " + LocationDao.ColumnName.Latitude.getName() + " text , " + LocationDao.ColumnName.Longitude.getName()
	+ " text );";
	
	private static final String CREATE_PROVINCE_TABLE = "CREATE TABLE " + ProvinceDao.TABLE_NAME
	+ " (id text primary key , " + ProvinceDao.ColumnName.Name.getName() + " text , " + ProvinceDao.ColumnName.Code.getName() + " text );";
	
	private static final String CREATE_CITY_TABLE = "CREATE TABLE " + CityDao.TABLE_NAME
	+ " (id text primary key , " + CityDao.ColumnName.Name.getName() + " text , " + CityDao.ColumnName.Code.getName() + " text , " + CityDao.ColumnName.PCode.getName() +  " text );";
	private final static int DATABASE_VERSION = 1;
	protected Context context;
	SQLiteDatabase database;
	public LocationConnection(Context context) {
		this.context = context;
		openDatabase();
	}
    public void openDatabase() {
        this.database = this.openDatabase(DATABASE_PATH + DATABASE_NAME);
    }
    private SQLiteDatabase openDatabase(String dbfile) {
        try {
        	File file = new File(dbfile);
            if (!file.exists()) {
            	InputStream is = context.getAssets().open("huodi2.db");
            	FileOutputStream fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[1024 * 8];
                int count = 0;
                while ((count =is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
               	fos.flush();
                fos.close();
                is.close();
            }
            database = SQLiteDatabase.openOrCreateDatabase(dbfile,null);
            return database;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
        	e.printStackTrace();
        }
        return null;
    }
    public void closeDatabase() {
    	if(this.database!=null)
    		this.database.close();
    }
	// 拷贝文件的函数 src 为需要复制的文件，dst为目标文件(被src覆盖的文件)
	// 拷贝的过程其实就是把src文件里内容写入dst文件里的过程(从头写到尾)
	public static void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();

		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
	/**
	 * 将数据库导出到SD卡
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws SDCardNotFoundException 
	 * @throws TransferDBIOException 
	 */
	public static void exportDatabaseToSDCard() throws FileNotFoundException, SDCardNotFoundException, TransferDBIOException {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			throw new SDCardNotFoundException();
		}
		File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
		String dir = Environment.getExternalStorageDirectory()+"/huodi";
		File dirFile = new File(dir);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		File newFile = new File(dir,
				"huodi2.db");
		try {
			newFile.createNewFile();
			copyFile(dbFile, newFile);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new TransferDBIOException();
		}
	}
	/**
	 * 从SD卡导入数据库
	 * 
	 * @return
	 * @throws SDCardNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws TransferDBIOException 
	 */
	public static void importDatabaseFromSDCard() throws SDCardNotFoundException, FileNotFoundException, TransferDBIOException {

		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			throw new SDCardNotFoundException();
		}
		File appDbFile = new File(DATABASE_PATH + DATABASE_NAME);
		String dir = Environment.getExternalStorageDirectory()+"/huodi";
		File sdDbFile = new File(dir,
			"huodi2.db");
		if(sdDbFile == null || !sdDbFile.exists()){
			throw new FileNotFoundException();
		}
		try {
			copyFile(sdDbFile, appDbFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TransferDBIOException();
		}
	}
	/**
	 * 获得数据库对象
	 * 
	 * @return
	 */
	public SQLiteDatabase getDataBase() {
		this.openDatabase();
		return this.database;
	}
}
