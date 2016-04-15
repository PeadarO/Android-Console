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
import org.openremote.android.console.R;
import org.openremote.android.console.bindings.Switch;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.OREvent;
import org.openremote.android.console.model.OREventListener;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.PollingStatusParser;
import org.openremote.android.console.util.ImageUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

/**
 * This class is responsible for rendering the switch in screen with the switch data.
 * It has control command and sensory.
 *
 * RT: Changed layout to use a Relative Layout View Group and Image Views to prevent switch image scaling
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class SwitchView extends SensoryControlView {

   private Button button;
   private ImageView imageView;
   private BitmapDrawable onImage;
   private BitmapDrawable offImage;
   private boolean isOn;
   private boolean canUseImage;
   public SwitchView(Context context, Switch switchComponent) {
      super(context);

      if (switchComponent != null) {
      	 setComponent(switchComponent);
         initSwitch(switchComponent);
         
         if (switchComponent.getSensor() != null) {
            addPollingSensoryListener();
         }
      }
      setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
   }

   /**
    * Sets switch's on/off state(image or text).
    * Sets touch listener on the switch view for change switch state and send command.
    * 
    * @param switchComponent the switch component
    */
   private void initSwitch(Switch switchComponent) {
      int width = switchComponent.getFrameWidth();
      int height = switchComponent.getFrameHeight();
      
  		RelativeLayout switchLayout = new RelativeLayout(this.getContext());
  		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
  		switchLayout.setLayoutParams(params);
      
      button = new Button(context, null, android.R.attr.buttonStyleSmall);
      button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Constants.DEFAULT_FONT_SIZE_DIP);
      button.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
      
      if (switchComponent.getOnImage() != null) {
         onImage = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + switchComponent.getOnImage().getSrc(), switchComponent.getFrameWidth(), switchComponent.getFrameHeight(), true, true);
         onImage.setGravity(Gravity.TOP | Gravity.LEFT);
      }
      if (switchComponent.getOffImage() != null) {
         offImage = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + switchComponent.getOffImage().getSrc(), switchComponent.getFrameWidth(), switchComponent.getFrameHeight(), true, true);
         offImage.setGravity(Gravity.TOP | Gravity.LEFT);
      }
      
      if (onImage != null && offImage != null) {
         canUseImage = true;
         button.setBackgroundColor(Color.TRANSPARENT);
         button.setText(null);
         
         imageView = new ImageView(context);
         imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
         imageView.setScaleType(ScaleType.MATRIX); // Prevent scaling
         switchLayout.addView(imageView);
      }
      
      updateState();
      switchLayout.addView(button);
      
      button.setOnTouchListener(new OnTouchListener() {
         public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
            	updatePressedState(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
            	updatePressedState(false);
              Log.i("SwitchView", "sendWriteCommand");
              if (isOn) {
              	sendCommandRequest(Switch.OFF);
              } else {
              	sendCommandRequest(Switch.ON);
              }
            }
            return false;
         }
        
     });
      
     addView(switchLayout);
   }
   
   @Override
   public void addPollingSensoryListener() {
      final Integer sensorId = ((Switch)getComponent()).getSensor().getSensorId();
      if (sensorId > 0) {
         ORListenerManager.getInstance().addOREventListener(ListenerConstant.ListenerPollingStatusIdFormat + sensorId, new OREventListener() {
            public void handleEvent(OREvent event) {
               String value = PollingStatusParser.statusMap.get(sensorId.toString()).toLowerCase();
               if (isOn && Switch.OFF.equals(value)) {
                  isOn = false;
               } else if (!isOn && Switch.ON.equals(value)) {
                  isOn = true;
               }
               handler.sendEmptyMessage(0);
            }
         });
      }
   }
   
   /** The handler is for updating switch state by polling result. */
   private Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
      	updateState();
      	super.handleMessage(msg);
      }
  };  
  
  private void updatePressedState(boolean isPressed) {
  	int opacity = isPressed ? 200 : 255;
  	
    if (canUseImage) {
    	imageView.setAlpha(opacity);
    }
  }
  
  private void updateState() {
		if (canUseImage) {
			if (isOn) {
				imageView.setImageDrawable(onImage);
			} else {
				imageView.setImageDrawable(offImage);
			}
		} else {
				if (isOn) {
					button.setText("ON");
				} else {
					button.setText("OFF");
			}
		}
  }
}
