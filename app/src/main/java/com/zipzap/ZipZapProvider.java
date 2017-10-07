package com.zipzap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.zipzap.util.DropBoxStorage;

public class ZipZapProvider extends ContentProvider {
	
	public static String BASE_FILE_PATH = "/data/data/com.zipzap/";
	public static String BASE_FILE_NAME = "file";
//	public static String APP_KEY = "1";
	
	public static final String PROVIDER_NAME = "com.zipzap.ZipZapProvider";
	public static final String URL = "content://" + PROVIDER_NAME + "/cte";
	public static final Uri CONTENT_URI = Uri.parse(URL);

	static final String id = "id";
	public static final String name = "name";
	static final int uriCode = 1;
	static final UriMatcher uriMatcher;
	private static HashMap<String, String> values;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "cte", uriCode);
		uriMatcher.addURI(PROVIDER_NAME, "cte/*", uriCode);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case uriCode:
			count = db.delete(TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case uriCode:
			return "vnd.android.cursor.dir/cte";

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = db.insert(TABLE_NAME, "", values);
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			syncLocalToCloud(values, new Long(rowID).toString());
			return _uri;
		}
		throw new SQLException("Failed to add a record into " + uri);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		if (db != null) {
			return true;
		}
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);

		switch (uriMatcher.match(uri)) {
		case uriCode:
			syncCloudToLocal();
			qb.setProjectionMap(values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (sortOrder == null || sortOrder == "") {
			sortOrder = name;
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case uriCode:
			count = db.update(TABLE_NAME, values, selection, selectionArgs);
			String rowId = null;
			if (selectionArgs != null && selectionArgs.length > 0)
				rowId = selectionArgs[0];
			syncLocalToCloud(values, rowId);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private Boolean syncLocalToCloud(ContentValues values, String rowId) {
		//store in a app specific file and put it in cloud
		byte[] blob = values.getAsByteArray("name");
		String app_key = "1"; //defaults is file 1
		if (rowId != null)
			app_key = rowId;

		String fileName = ZipZapProvider.BASE_FILE_PATH
				+ ZipZapProvider.BASE_FILE_NAME + app_key;
		
		File file = new File(ZipZapProvider.BASE_FILE_PATH
				+ ZipZapProvider.BASE_FILE_NAME + app_key); // ZipZapProvider.APP_KEY);
		
		if (blob != null) {
			FileOutputStream out = null;
			
	        try {
	        	out = new FileOutputStream(file);
	        	out.write(blob);
	        	out.close();
	        	
	        	DropBoxStorage dStorage = DropBoxStorage.getInstance();
				//this.getContext()
				dStorage.upload(getContext(), fileName);
	            return true;
	            
	        } catch (FileNotFoundException e) {
	        	e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();
			}			
		}
		else {
			//nothing there on content provider
		}
		return false;
	}
	
	private Boolean syncCloudToLocal() {
		
		DropBoxStorage dStorage = DropBoxStorage.getInstance();
		//this.getContext()
		dStorage.download(this.getContext(), ZipZapProvider.BASE_FILE_PATH
				+ ZipZapProvider.BASE_FILE_NAME + "1" //ZipZapProvider.APP_KEY,
				);
//		dStorage.download(ZipZapProvider.BASE_FILE_PATH
//				+ ZipZapProvider.BASE_FILE_NAME + "2", //ZipZapProvider.APP_KEY, 
//				this.getContext().getContentResolver());
		
		return true;
	}
	
	private SQLiteDatabase db;
	static final String DATABASE_NAME = "mydb";
	static final String TABLE_NAME = "names";
	static final int DATABASE_VERSION = 1;
	static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + " name BLOB NOT NULL);";

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}