/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
*
* See the contributors.txt file in the distribution for a
* full listing of individual contributors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.openremote.android.console.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.InflateException;

/**
 * Image Utility class.
 * 
 * @author Dan Cong
 * @author Rich Turner
 */
public class ImageUtil {
  private static LinkedHashMap<String, List<Bitmap>> bitmapCache = new LinkedHashMap<String, List<Bitmap>>();
  
   private ImageUtil() {
   }
   
   /**
    * Calls native Drawable.createFromPath(pathName), but catch OutOfMemoryError and do nothing.
    * 
    * @param pathName
    *           path name
    * @return Drawable instance
    */
   public static BitmapDrawable createFromPathQuietly(Context ctx, String pathName, int reqWidth, int reqHeight) {
	   BitmapFactory.Options opts=new BitmapFactory.Options();
	   opts.inDither=false;                     //Disable Dithering mode
	   opts.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
	   opts.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
	   opts.inTempStorage=new byte[32 * 1024]; 
     BitmapDrawable ret = null;
      
	  Bitmap decodedBitmap = createBitmap(pathName, reqWidth, reqHeight, true, true);
	  if (decodedBitmap != null)
    		  ret = new BitmapDrawable(ctx.getResources(), decodedBitmap);
      
      return ret;
   }
   
   private static Bitmap createBitmap(String pathName, int reqWidth, int reqHeight, boolean shrinkOnly, boolean maintainAspect) {
      Bitmap bitmap = null;
      BitmapFactory.Options opts=new BitmapFactory.Options();
      opts.inDither=false;                     //Disable Dithering mode
      opts.inPurgeable=true;                   //Tell gc the Bitmap can be cleared
      opts.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
      opts.inTempStorage=new byte[32 * 1024]; 
	   
      try {
    	  // Determine required sample size of the image
    	  opts.inJustDecodeBounds = true;
    	  BitmapFactory.decodeFile(pathName, opts);
        opts.inJustDecodeBounds = false;
        
        if (maintainAspect) {
          double ar = (double)opts.outWidth / opts.outHeight;
          boolean wIsLimit = Math.abs(opts.outWidth - reqWidth) < Math.abs(opts.outHeight - reqHeight);
          if (wIsLimit) {
            reqWidth = shrinkOnly ? Math.min(reqWidth, opts.outWidth) : reqWidth;
            reqHeight = (int)Math.round((double)reqWidth / ar);
          } else {
            reqHeight = shrinkOnly ? Math.min(reqHeight, opts.outHeight) : reqHeight;
            reqWidth = (int)Math.round((double)reqHeight * ar);
          }
        } else {
          reqWidth = shrinkOnly ? Math.min(reqWidth, opts.outWidth) : reqWidth;
          reqHeight = shrinkOnly ? Math.min(reqHeight, opts.outHeight) : reqHeight;
        }
        
        // Find image that matches this size
    	  List<Bitmap> bitmaps = bitmapCache.get(pathName.toLowerCase());
    	  if (bitmaps != null) {
    	    for (Bitmap b : bitmaps) {
            double bw = b.getWidth();
            double bh = b.getHeight();
    	      if (bw == reqWidth && bh == reqHeight) {
  	          bitmap = b;
  	          break;
    	      }
//    	      if (bitmapWrapper.sampleSize <= opts.inSampleSize) {
//    	        bitmap = bitmapWrapper.bitmap;
//    	        break;
//    	      }
    	    }
    	  }
    	  
    	  // Generate the bitmap if not found in the cache 
    	  if (bitmap == null)
    	  {
          // Calculate sample size
          opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
    	    bitmap = BitmapFactory.decodeFile(pathName, opts);
    	    if (bitmap.getWidth() != reqWidth || bitmap.getHeight() != reqHeight) {
    	      Bitmap oldBitmap = bitmap;
    	      bitmap = Bitmap.createScaledBitmap(oldBitmap, reqWidth, reqHeight, false);
    	      if (bitmap != oldBitmap) {
    	        oldBitmap.recycle();
    	      }
    	    }
    	    
    	    if (bitmaps == null) {
    	      bitmaps = new ArrayList<Bitmap>();
    	      bitmapCache.put(pathName.toLowerCase(), bitmaps);
    	    }
    	    bitmaps.add(bitmap);
    	  }
      } catch (OutOfMemoryError e) {
          Log.e("Out of Memory error: ", pathName);
      }

      return bitmap;
   }
   
   public static BitmapDrawable createClipedDrawableFromPath(Context ctx, String pathName, int reqWidth, int reqHeight, int width, int height) {
     BitmapDrawable croppedBitmap = null;
     croppedBitmap = createFromPathQuietly(ctx, pathName, reqWidth, reqHeight);
     if (croppedBitmap != null) {
	     croppedBitmap.setBounds(0, 0, width, height);
	     croppedBitmap.setGravity(Gravity.LEFT|Gravity.TOP);
     }
     return croppedBitmap;
   }
   
  public static BitmapDrawable createScaledDrawableFromPath(Context ctx, String pathName, int width, int height) {
    return createScaledDrawableFromPath(ctx, pathName, width, height, false, false);
  }
   
  public static BitmapDrawable createScaledDrawableFromPath(Context ctx, String pathName, int width, int height, boolean shrinkOnly, boolean maintainAspect) {
    BitmapDrawable drawable = null;
    Bitmap bitmap = createBitmap(pathName, width, height, shrinkOnly, maintainAspect);
   
    if (bitmap != null) {
      drawable = new BitmapDrawable(ctx.getResources(), bitmap);
    }
     
    return drawable;
  }
   
  public static Pair<Integer, Integer> getNativeImageSize(String pathName) {
    Pair<Integer, Integer> size = null;
    BitmapFactory.Options opts=new BitmapFactory.Options();
    opts.inDither=false;                     //Disable Dithering mode
    opts.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
    opts.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
    opts.inTempStorage=new byte[32 * 1024]; 

    try {
      // Determine required sample size of the image
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(pathName, opts);
      size = new Pair<Integer, Integer>(opts.outWidth, opts.outHeight);
    } catch (OutOfMemoryError e) {
      Log.e("Out of Memory error: ", pathName);
    }
    return size;
  }
   
   /**
    * Calls native Activity.setContentView(int layoutResID), but catch OutOfMemoryError and do nothing.
    * 
    * @param activity
    *           an activity
    * @param layoutResID
    *           Resource ID to be inflated.
    */
   public static void setContentViewQuietly(Activity activity, int layoutResID) {
      if (activity == null) {
         return;
      }
      try {
         activity.setContentView(layoutResID);
      } catch (InflateException e) {
         Log.e("OpenRemote-OutOfMemoryError", "unable to setContentView, bitmap size exceeds VM budget");
      } catch (OutOfMemoryError e) {
         Log.e("OpenRemote-OutOfMemoryError", "unable to setContentView, bitmap size exceeds VM budget");
      }
   }
   
   public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	   // Raw height and width of image
	   final int height = options.outHeight;
	   final int width = options.outWidth;
	   int inSampleSize = 1;
	
	   if (height > reqHeight || width > reqWidth) {
	
	       final int halfHeight = height / 2;
	       final int halfWidth = width / 2;
	
	       // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	       // height and width larger than the requested height and width.
	       while ((halfHeight / inSampleSize) > reqHeight
	               && (halfWidth / inSampleSize) > reqWidth) {
	           inSampleSize *= 2;
	       }
	   }
	
	   return inSampleSize;
   }
   
   public static void clearBitmaps() {
     for (Entry<String, List<Bitmap>> bList : bitmapCache.entrySet()) {
       for (Bitmap b : bList.getValue()) {
         b.recycle();
         b = null;
       }
     }
     bitmapCache.clear();
   }
}
