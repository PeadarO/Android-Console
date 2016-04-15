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
import org.openremote.android.console.bindings.Image;
import org.openremote.android.console.bindings.Label;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.OREvent;
import org.openremote.android.console.model.OREventListener;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.PollingStatusParser;
import org.openremote.android.console.util.ImageUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The ORImageView contains image component.
 */
public class ORImageView extends ComponentView implements SensoryDelegate {

   private ImageView imageView;
   private TextView textView;
   private String newStatus;
   private int imageWidth;
   private int imageHeight;
   
   public ORImageView(Context context, Image image) {
      super(context);
      setComponent(image);
      
      textView = new TextView(getContext());
      //textView.setLines(1);
      textView.setHorizontallyScrolling(false);
      textView.setGravity(Gravity.CENTER);
      textView.setIncludeFontPadding(false);
       
      if (image != null) {
    	 imageWidth = image.getFrameWidth();
    	 imageHeight = image.getFrameHeight();
         image.setLinkedLabel();// read label from cache.
         imageView = new ImageView(context);
         addImageView(image.getSrc());
         if (image.getSensor() != null) {
            addPollingSensoryListener();
         }
      }
      setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
   }

   private void addImageView(String imageSrc) {
      Drawable bd = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + imageSrc, imageWidth, imageHeight);
      if (bd == null) {
         return;
      }
      imageView.setLayoutParams(new FrameLayout.LayoutParams(bd.getIntrinsicWidth(), bd.getIntrinsicHeight()));
      imageView.setImageDrawable(bd);
      addView(imageView);
   }
   
   @Override
   public void addPollingSensoryListener() {
      Image image = (Image)getComponent();
      Integer id = image.getSensor().getSensorId();
      if (id <= 0) {
         if (image.getLabel() != null && image.getLabel().getSensor() != null) {
            id = image.getLabel().getSensor().getSensorId();
         }
      }
      final Integer sensorId = id;
      if (sensorId > 0) {
         ORListenerManager.getInstance().addOREventListener(ListenerConstant.ListenerPollingStatusIdFormat + sensorId, new OREventListener() {
            public void handleEvent(OREvent event) {
               newStatus = PollingStatusParser.statusMap.get(sensorId.toString());
               handler.sendEmptyMessage(0);
            }
         });
      }

   }
   
   /** The handler is for update image view by polling result. */
   private Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         String newValue = ((Image)getComponent()).getSensor().getStateValue(newStatus);
         if (newValue != null) {
            removeAllViews();
            addImageView(newValue);
         } else if (((Image)getComponent()).getLabel() != null) {
            Label label = ((Image)getComponent()).getLabel();
            if (label.getSensor() != null) {
               if (label.getText() != null) {
                  textView.setText(label.getText());
               }
               if (label.getFontSize() > 0) {
                  textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, label.getFontSize());
               } else {
                 textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Constants.DEFAULT_FONT_SIZE_DIP);
               }
               if (label.getColor() != null) {
                  textView.setTextColor(Color.parseColor(label.getColor()));
               }
               
               newValue = label.getSensor().getStateValue(newStatus);
               if (newValue != null) {
                  removeAllViews();
                  addView(textView);
               }
            }
            
         }
         super.handleMessage(msg);
      }
  };

}
