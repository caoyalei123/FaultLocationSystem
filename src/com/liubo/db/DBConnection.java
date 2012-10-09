package com.liubo.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;

import com.liubo.exception.TransferDBIOException;
import com.liubo.exception.SDCardNotFoundException;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

public class DBConnection extends SQLiteOpenHelper {

	public final static String DATABASE_PATH = Environment.getDataDirectory()
			+ "/data/com.liubo/databases/";// 数据库在手机里的路径
	public final static String DATABASE_NAME = "FLSDB";

	private static final String CREATE_ALERT_TABLE = "CREATE TABLE alert "
			+ "(id text primary key , " + "tower_id text , " + "address_num text , " + "info text ,"
			+ "substation_num text , " + "circuit_num text , "
			+ "tower_num text , " + "date text , " + "is_read text " + ");";

	private static final String CREATE_SUBSTATION_TABLE = "CREATE TABLE substation "
			+ "( id text primary key , "
			+ " substation_num text , "
			+ " name text );";
	private static final String CREATE_CIRCUIT_TABLE = "CREATE TABLE circuit "
			+ "( id text primary key , " + " circuit_num text , "
			+ " name text ," + " substation_num text );";
	private static final String CREATE_TOWER_TABLE = "CREATE TABLE tower "
			+ "( id text primary key , " + " name text ,"
			+ " tower_num text , " + " circuit_num text , "
			+ " longitude text , " + " latitude text , " + " status text );";

	private final static int DATABASE_VERSION = 1;

	private static Context applicationContext;

	public DBConnection(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		applicationContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_ALERT_TABLE);
		db.execSQL(CREATE_SUBSTATION_TABLE);
		db.execSQL(CREATE_CIRCUIT_TABLE);
		db.execSQL(CREATE_TOWER_TABLE);
		System.out.println("OnCreate applicationContext!" + applicationContext);
		initSQL(applicationContext.getResources(), db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("drop table if exists alert ;");
		db.execSQL("drop table if exists substation ;");
		db.execSQL("drop table if exists circuit ;");
		db.execSQL("drop table if exists tower ;");
		System.out.println("OnUpgrade!");
		onCreate(db);
	}

	/**
	 * 把assets中的数据库文件复制到数据库路径
	 * 
	 * @param resources
	 * @throws IOException
	 */
	private static boolean copyDatabase(Resources resources) {
		String databaseFilepath = DATABASE_PATH + DATABASE_NAME;
		File file = new File(DATABASE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		AssetManager assets = resources.getAssets();
		InputStream inputStream;
		try {
			inputStream = assets.open("FLSDB");
			if (inputStream != null) {
				FileOutputStream fileOutputStream = new FileOutputStream(
						databaseFilepath);
				int sum_byte = 0;
				int read_byte = 0;
				byte buffer[] = new byte[1024];
				while ((read_byte = inputStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, read_byte);
					sum_byte += read_byte;
					fileOutputStream.flush();
				}
				inputStream.close();
				fileOutputStream.close();
			} else {
				System.out.println("inputStream is null");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
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
			"huodi.db");
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
				"huodi.db");
		try {
			newFile.createNewFile();
			copyFile(dbFile, newFile);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new TransferDBIOException();
		}
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
	 * 执行初始SQL
	 * 
	 * @param resources
	 * @return
	 */
	private static boolean initSQL(Resources resources, SQLiteDatabase db) {
		AssetManager assets = resources.getAssets();
		try {
			InputStream in = assets.open("fls.sql");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in));
			executeSqlScript(db, bufferedReader);
			bufferedReader.close();
			in.close();
		} catch (SQLException e) {
			System.out.println("init sql exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("init io exception");
			e.printStackTrace();
		}
		return true;
	}

	private static List<String> tables = new ArrayList<String>();

	/**
	 * 执行sql脚本
	 * 
	 * @param db
	 * @param reader
	 * @throws IOException
	 */
	private static void executeSqlScript(SQLiteDatabase db,
			BufferedReader reader) throws IOException {
		StringBuilder sql = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			if (TextUtils.isEmpty(line) || line.startsWith("#")) {
				continue;
			}
			if (TextUtils.isEmpty(line) || line.startsWith("--")) {
				continue;
			}
			if (TextUtils.isEmpty(line) || line.startsWith("=")) {
				line = line.replaceAll("=", "");
				tables.add(line);
				continue;
			}
			line = line.trim();
			int index = line.indexOf(';');
			if (index >= 0) {
				String firstStr = line.substring(0, index + 1);
				sql.append(firstStr).append('\n');
				try {
					db.beginTransaction();
					db.execSQL(sql.toString()); // make database
					db.endTransaction();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				sql = new StringBuilder();
				if (index < line.length()) {
					String lastStr = line.substring(index + 1);
					if (!TextUtils.isEmpty(lastStr)) {
						sql.append(lastStr);
					}
				}
			} else {
				sql.append(line).append('\n');
			}
		}
		if (sql.length() > 0) {
			try {
				System.out.println(sql.toString());
				db.execSQL(sql.toString());
				db.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获得数据库对象
	 * 
	 * @return
	 */
	public SQLiteDatabase getDataBase() {
		return this.getWritableDatabase();
	}

}
