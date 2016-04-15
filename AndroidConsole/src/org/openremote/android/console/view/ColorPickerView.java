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
package org.openremote.android.console.view;

import org.openremote.android.console.Constants;
import org.openremote.android.console.bindings.ColorPicker;
import org.openremote.android.console.bindings.Image;
import org.openremote.android.console.util.ImageUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * The view display a image, it supports to detect point color on the image, then send the detected color to controller.
 * The touch up event or touch move event would trigger to send the color.
 */
public class ColorPickerView extends ControlView {

   /** The image view to display the colorpicker. */
   private ImageView imageView;
   private int lastXposition;
   private int lastYposition;
   public static final int MIN_VALID_MOVE_DISTANCE = 2;
   
   public ColorPickerView(Context context, ColorPicker colorPicker) {
      super(context);
      setComponent(colorPicker);
      int width = colorPicker.getFrameWidth();
      int height = colorPicker.getFrameHeight();
      Image image = colorPicker.getImage();
      if (image != null) {
         initView(context, width, height, image);
      }
   }

   /**
    * @param context
    * @param width the component's width
    * @param height the component's height
    * @param image
    */
   private void initView(Context context, int width, int height, Image image) {
      imageView = new ImageView(context);
      final BitmapDrawable bd = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + image.getSrc(), width, height);
      if (bd == null) {
         return;
      }
      imageView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
      imageView.setBackgroundDrawable(bd);
      imageView.setOnTouchListener(new OnTouchListener() {
         public boolean onTouch(View v, MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            /**
             * If touch move, and the move distance over 2 pixel, send the color to controller.
             * If touch up, send the color to controller.
             */
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
               if (Math.abs(lastXposition - x) > MIN_VALID_MOVE_DISTANCE || Math.abs(lastYposition - y) > MIN_VALID_MOVE_DISTANCE){
                  colorPicked(bd, x, y);
                  lastXposition = x;
                  lastYposition = y;
               }
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
               colorPicked(bd, x, y);
            }
            return true;
         }

         /**
          * @param bd the bitmapDrawable of the image.
          * @param x the X coordinate of the event.
          * @param y the Y coordinate of the event.
          */
         private void colorPicked(final BitmapDrawable bd, int x, int y) {
            if (x >= 0 && x < bd.getIntrinsicWidth() && y >= 0 && y < bd.getIntrinsicHeight()) {
               int color = bd.getBitmap().getPixel(x, y);
               String colorStrs = String.format("%02x%02x%02x", Color.red(color), Color.green(color), Color.blue(color));
               sendCommandRequest(colorStrs);
            }
         }
           
        });
      addView(imageView);
   }
   
}
