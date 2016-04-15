package org.openremote.android.console;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

public class ControllerDataHelper {
	 public static final String TAG = Constants.LOG_CATEGORY + "DataHelper";
	 
   private static final String DATABASE_NAME = "example.db";
   private static final int DATABASE_VERSION = 4;
   private static final String TABLE_NAME = "table1";
   private OpenHelper openHelper;
   private SQLiteDatabase db;
   private Context context;
   private static SQLiteStatement insertStmt;
   private static SQLiteStatement updateStmt;
   private SQLiteStatement deleteStmt;
   private SQLiteStatement selectStmt;
   private SQLiteStatement findStmt;
   
   private static final String INSERT = "insert into "
      + TABLE_NAME + "(name, info, defaultpanel, username, userpass, xscale, yscale) values (?,?,?,?,?,?,?)";
   private static final String UPDATE = "update " + TABLE_NAME + " set name=?, info=?, defaultpanel=?, username=?, userpass=?, xscale=?, yscale=?"
       + " where name = ?";
   private static final String DELETE = "delete from "
		      + TABLE_NAME + " where name = ?";
   private static final String SELECT = "select * from "
		      + TABLE_NAME ;
   private static final String FIND = "select * from "
		      + TABLE_NAME + " where name = ?" ;
   
   public ControllerDataHelper(Context context) {
	   this.context = context;
   }
 
//	 public boolean insert(String name, String info) {
//	   this.selectStmt.bindString(1, name);
//	   
//	   // if(this.selectStmt.e)
//	   String s[] = new String[1];
//	   s[0]=name;
//	   
//	   Cursor c=db.rawQuery(FIND, s);
//	   int colCount = c.getColumnCount();
//	   c.close();
//	   if(colCount > 0) return 0;	   
//	   
//	    this.insertStmt.bindString(1, name);
//	    this.insertStmt.bindString(2, info);
//	    return this.insertStmt.executeInsert();
//	 }

//   public long find(String name){
//	      String s[] = new String[1];
//	   s[0]=name;
//	   
//	   Cursor c = db.rawQuery(FIND, s);
//	   
//	   if(c.getCount() > 0) return 1 ;
//	   	
//	   return 0;	   
//   }
   
   private void closeConnection() {
	   openHelper.close();
	   openHelper = null;
   }
   
   private boolean openConnection() {
	   if (db == null || !db.isOpen()) {
		   openHelper = new OpenHelper(context);
		   db = openHelper.getWritableDatabase();
	       this.updateStmt = this.db.compileStatement(UPDATE);
	       this.insertStmt = this.db.compileStatement(INSERT);
	       this.deleteStmt = this.db.compileStatement(DELETE);
	       this.selectStmt = this.db.compileStatement(SELECT);
		   return true;
	   }
	   return false;
   }
   
	public boolean controllerExists(String name) {
		String s[] = new String[] {name};
		boolean exists = false;
		try {
			boolean opened = openConnection();
			Cursor c = db.rawQuery(FIND, s);
			exists = c.getCount() > 0;
			if (opened)
				closeConnection();
		} catch (Exception e) {
			Log.e("SQL Exception", "Failed to determine if controller exists '" +  name + "'", e);
		}
		return exists;
	}
	
	public void addController(ControllerObject controller) {
		try {
			boolean opened = openConnection();
			insertStmt.bindString(1, controller.getUrl());
			insertStmt.bindString(2, ""); // Not sure what this property is
			insertStmt.bindString(3, controller.getDefaultPanel());
			insertStmt.bindString(4, controller.getUsername());
			insertStmt.bindString(5, controller.getUserPass());
			insertStmt.bindString(6, Double.toString(controller.getXScale()));
			insertStmt.bindString(7, Double.toString(controller.getYScale()));
			insertStmt.executeInsert();
			if (opened)
				closeConnection();
		} catch (Exception e) {
			Log.e("SQL Exception", "Add Controller failed", e);
		}
		
	}
   
	public void updateController(ControllerObject oldController, ControllerObject newController) {
		String[] updateInfo = new String[] {
				newController.getUrl(),
				"",
				newController.getDefaultPanel(),
				newController.getUsername(),
				newController.getUserPass(),
				Double.toString(newController.getXScale()),
				Double.toString(newController.getYScale())
		};
		ContentValues cv = new ContentValues();
		cv.put("name", newController.getUrl());
		cv.put("defaultpanel", newController.getDefaultPanel());
		cv.put("username", newController.getUsername());
		cv.put("userpass", newController.getUserPass());
		cv.put("xScale", Double.toString(newController.getXScale()));
		cv.put("yScale", Double.toString(newController.getYScale()));

		try {
			boolean opened = openConnection();
			db.update(TABLE_NAME, cv, "name = ?", new String[] {oldController.getUrl()});
			if (opened)
				closeConnection();
		} catch (Exception e) {
			Log.e("SQL Exception", "Update Controller failed", e);
		}
	}
   
   public void deleteController(String url) {
	   try {
			boolean opened = openConnection();
			this.deleteStmt.bindString(1, url);
			this.deleteStmt.execute();
			if (opened)
				closeConnection();
		} catch (Exception e) {
			Log.e("SQL Exception", "Update Controller failed", e);
		}
   }
   
   public void deleteAll() {
	   try {
			boolean opened = openConnection();
			this.db.delete(TABLE_NAME, null, null);
			if (opened)
				closeConnection();
		} catch (Exception e) {
			Log.e("SQL Exception", "Update Controller failed", e);
		}     
   }
   
   public ArrayList<ControllerObject> getAllControllers() {
	   ArrayList<ControllerObject> list = new ArrayList<ControllerObject>();

	   try {
		   boolean opened = openConnection();
		   Cursor cursor = this.db.query(TABLE_NAME, null, null, null, null, null, "name desc");
			if (cursor.moveToFirst()) {
			   do {
			      list.add(getControllerFromCursor(cursor));
			   } while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
			   cursor.close();
			}
			if (opened)
				closeConnection();
	   } catch(Exception e) {
		   Log.e("SQL Exception", "Failed to get all controllers", e);
	   }
	   return list;
   }
   
   public ControllerObject getControllerByUrl(String url) {
			ControllerObject controller = null;
			if (url == null || url.equals(""))
				return controller;
			
			try {
				boolean opened = openConnection();
				Cursor cursor = db.rawQuery(FIND, new String[] {url});
				if (cursor.moveToFirst()) {
					controller = getControllerFromCursor(cursor);
				}
				if (opened)
					closeConnection();
			} catch (Exception e) {
				Log.e("Failed to get controller by URL", url);
			}
			
			return controller;
   }
   
   private ControllerObject getControllerFromCursor(Cursor cursor) {
  	 ControllerObject controller = null;
  	 try {
  		 controller = new ControllerObject(
  				 cursor.getString(0),
  				 cursor.getString(2),
  				 cursor.getString(3),
  				 cursor.getString(4), 
  				 Double.parseDouble(cursor.getString(5)),
			     Double.parseDouble(cursor.getString(6)));
  	 } catch (Exception e) {
  		 Log.e(TAG, "Failed to create ControllerObject from SQLLiteCursor");
  	 }
  	 return controller;
   }
   
//   public List<String> selectAll() {
//      List<String> list = new ArrayList<String>();
//      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name" },
//        null, null, null, null, "name desc");
//      if (cursor.moveToFirst()) {
//         do {
//            list.add(cursor.getString(0));
//         } while (cursor.moveToNext());
//      }
//      if (cursor != null && !cursor.isClosed()) {
//         cursor.close();
//      }
//      return list;
//   }
   
   private static class OpenHelper extends SQLiteOpenHelper {
      OpenHelper(Context ctx) {
         super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
      }
      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + TABLE_NAME + " (name TEXT PRIMARY KEY, info TEXT, defaultpanel TEXT, username TEXT, userpass TEXT, xscale TEXT, yscale TEXT)");
      }
      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }
}