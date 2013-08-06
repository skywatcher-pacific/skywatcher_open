package com.skywatcher.PanoramaApp;

import java.util.ArrayList;
import java.util.List;

import com.skywatcher.PanoramaApp.ShootScenario.ShootScenarioException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class ScenariosDBAdapter {
	// 資料庫資訊
	private static final String DATABASE_NAME = "scenario.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME = "scenarios";
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_MinAz = "minaz";
	private static final String COLUMN_MaxAz = "maxaz";
	private static final String COLUMN_MinAlt = "minalt";
	private static final String COLUMN_MaxAlt = "maxalt";
	private static final String COLUMN_INTERLACE = "interlace";
	private static final String[] COLUMNS = { COLUMN_ID, COLUMN_NAME,
			COLUMN_MinAz, COLUMN_MaxAz, COLUMN_MinAlt, COLUMN_MaxAlt,
			COLUMN_INTERLACE };

	// 生成 Database 所需 class
	public class DatabaseHelper extends SQLiteOpenHelper {

		private static final String CREATE_SCENAROIS_TABLE_SQL = "create table "
				+ TABLE_NAME
				+ " ("
				+ COLUMN_ID
				+ " integer primary key, "
				+ COLUMN_NAME
				+ " text, "
				+ COLUMN_MinAz
				+ " integer, "
				+ COLUMN_MaxAz
				+ " integer, "
				+ COLUMN_MinAlt
				+ " integer, "
				+ COLUMN_MaxAlt + " integer, " + COLUMN_INTERLACE + " integer)";

		private static final String DROP_SCENAROIS_TABLE_SQL = "drop table if exists "
				+ TABLE_NAME;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_SCENAROIS_TABLE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(DROP_SCENAROIS_TABLE_SQL);
			onCreate(db);
		}
	}

	private Context context;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	public ScenariosDBAdapter(Context context) {
		this.context = context;
	}

	// 開啟資料庫
	public ScenariosDBAdapter open() throws SQLiteException {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	// 關閉資料庫
	public void close() {
		dbHelper.close();
	}

	// 插入新資料
	public long insert(ShootScenario scenario) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, scenario.getName());
		values.put(COLUMN_MinAz, scenario.getMinAz());
		values.put(COLUMN_MaxAz, scenario.getMaxAz());
		values.put(COLUMN_MinAlt, scenario.getMinAlt());
		values.put(COLUMN_MaxAlt, scenario.getMaxAlt());
		values.put(COLUMN_INTERLACE, scenario.isInterlace());

		return db.insert(TABLE_NAME, null, values);
	}

	// 更新資料
	public int update(ShootScenario scenario) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, scenario.getName());
		values.put(COLUMN_MinAz, scenario.getMinAz());
		values.put(COLUMN_MaxAz, scenario.getMaxAz());
		values.put(COLUMN_MinAlt, scenario.getMinAlt());
		values.put(COLUMN_MaxAlt, scenario.getMaxAlt());
		values.put(COLUMN_INTERLACE, scenario.isInterlace());

		String whereClause = "rowid = " + scenario.getRowid();
		return db.update(TABLE_NAME, values, whereClause, null);
	}

	// 取得所有資料
	public List<ShootScenario> getAll() {
		List<ShootScenario> scenarioList = new ArrayList<ShootScenario>();
		Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null,
				COLUMN_ID);
		try {
			while (cursor.moveToNext()) {
				ShootScenario scenario = new ShootScenario();
				scenario.setRowid(cursor.getLong(0));
				scenario.setName(cursor.getString(1));
				scenario.setMinAz(cursor.getInt(2));
				scenario.setMaxAz(cursor.getInt(3));
				scenario.setMinAlt(cursor.getInt(4));
				scenario.setMaxAlt(cursor.getInt(5));
				scenario.setInterlace(cursor.getInt(6) > 0);
				scenarioList.add(scenario);
			}
		} catch (ShootScenarioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return scenarioList;
	}

	// 取得指定資料
	public ShootScenario getById(long rowId) {
		String selection = "rowid = " + rowId;
		Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null,
				null, null);
		try {
			while (cursor.moveToNext()) {
				ShootScenario scenario = new ShootScenario();
				scenario.setRowid(cursor.getLong(0));
				scenario.setName(cursor.getString(1));
				scenario.setMinAz(cursor.getInt(2));
				scenario.setMaxAz(cursor.getInt(3));
				scenario.setMinAlt(cursor.getInt(4));
				scenario.setMaxAlt(cursor.getInt(5));
				scenario.setInterlace(cursor.getInt(6) > 0);

				return scenario;
			}
		} catch (ShootScenarioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// 刪除資料
	public int delete(long rowId) {
		return db.delete(TABLE_NAME, "rowid = " + rowId, null);
	}

	private static String DB_PATH = "/data/data/com.skywatchertelescope.PanoramaApp/databases/";

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	public boolean checkDataBase() {

		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
}
