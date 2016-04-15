package org.openremote.android.test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.test.RenamingDelegatingContext;

public class TestMockContext extends RenamingDelegatingContext {

   private static final String MOCK_FILE_PREFIX = "test.";

   private TestMockContentResolver contentResolver;

   public TestMockContext(Context context) {
      super(context, MOCK_FILE_PREFIX);
      makeExistingFilesAndDbsAccessible();
   }

   public TestMockContext(Context context, String filePrefix) {
      super(context, filePrefix);
      makeExistingFilesAndDbsAccessible();
   }

   public void init() {
      if (null == contentResolver) {
         contentResolver = new TestMockContentResolver();
         contentResolver.setContext(this);
      }
   }

   @Override
   public SQLiteDatabase openOrCreateDatabase(String name, int mode,
         CursorFactory factory) {
      return super.openOrCreateDatabase(name, mode, factory);
   }

   @Override
   public TestMockContentResolver getContentResolver() {
      return contentResolver;
   }

}
