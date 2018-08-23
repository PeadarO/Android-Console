package org.openremote.android.console;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class ControllerDBHelper extends SQLiteOpenHelper {

		private static String DB_PATH = "/data/data//databases/";

		ContentValues map;
	    private static final int DATABASE_VERSION = 2;
	    private static final String CONTROLLER_TABLE_NAME = "ControllerInfo";
		private String URL ="URL";
		private String GROUP = "GROUP";
	    private String CONTROLLER_TABLE_CREATE =
	                "CREATE TABLE " + CONTROLLER_TABLE_NAME + " (" +
	                URL + " TEXT," +
	                GROUP + " TEXT);";
	    private SQLiteStatement insertStmt;
	    private String INSERT =
                "INSERT INTO "+DATABASE_NAME+"."+ CONTROLLER_TABLE_NAME +" (URL,GROUP) VALUES (?  , ?);";
	    
		private static final String DATABASE_NAME = "ControllerDatabase.db";

	    ControllerDBHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        //db.execSQL(CONTROLLER_TABLE_CREATE);
	        //this.insertRow(db, url, group)
	    	db.execSQL("CREATE TABLE " + CONTROLLER_TABLE_NAME + "(URL TEXT, GROUP TEXT)");
	    	      
	    }

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
	    public void insertRow(SQLiteDatabase db, String url, String group) {
	    	/*map.put(URL, url);
	    	map.put(GROUP, group);
	        db.insert(CONTROLLER_TABLE_NAME,"nonono",map);*/
	    	
	    	
	    	insertStmt = db.compileStatement(INSERT);
	        insertStmt.bindString(1, url);
	        insertStmt.bindString(2, group);
	        insertStmt.executeInsert();
	        
	    }

	}

