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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.openremote.android.console.Constants;
import org.openremote.android.console.LoginDialog;
import org.openremote.android.console.R;
import org.openremote.android.console.bindings.ORButton;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ControllerException;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.util.ImageUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * 
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * @author ?
 */
public class ButtonView extends ControlView {

   private Button uiButton;
   private BitmapDrawable defaultImage;
   private BitmapDrawable pressedImage;
   private Timer longPressTimer;
   
   /** The Constant REPEAT_CMD_INTERVAL. */
   public final static long REPEAT_CMD_INTERVAL = 300;
   
   public ButtonView(Context context, ORButton button) {
      super(context);
      setComponent(button);
      if (button != null) {
         uiButton  = new Button(context, null, android.R.attr.buttonStyleSmall);
         initButton(button);
      }
   }
   
   /**
    * Inits the button.
    * Add listener to make the button send command and navigate to.
    * If pressed, send command; if up, navigate to. It also can 
    * repeat send command if pressed and not up.  
    * 
    * @param button the button
    */
   private void initButton(final ORButton button) {
      int width = button.getFrameWidth();
      int height = button.getFrameHeight();
      uiButton.setEllipsize(TruncateAt.END);
      uiButton.setLines(1);
      uiButton.setId(button.getComponentId());
      uiButton.setText(button.getName());
      uiButton.setPadding(0, 0, 0, 0);
      uiButton.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
      uiButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Constants.BUTTON_FONT_SIZE_DIP);
      uiButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
      FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
      uiButton.setLayoutParams(params);

      if (button.getDefaultImage() != null) {
         defaultImage = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + button.getDefaultImage().getSrc(), width, height);
         if (defaultImage != null) {
            uiButton.setBackgroundDrawable(defaultImage);
         }
      }
      
      if (button.getPressedImage() != null) {
         pressedImage = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + button.getPressedImage().getSrc(), width, height);
      }
      
      if (button.getVersion() == 1 || AppSettingsModel.getCurrentControllerApiVersion(context) == 1) {      
        View.OnTouchListener touchListener = new OnTouchListener() {
           public boolean onTouch(View v, MotionEvent event) {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {
                 cancelTimer();
                 if (pressedImage != null) {
                    uiButton.setBackgroundDrawable(pressedImage);
                 } else if (defaultImage != null) {
                    defaultImage.setAlpha(200);
                    uiButton.setBackgroundDrawable(defaultImage);
                 } else {
                   uiButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_pressed));
                 }
                 if (button.isHasControlCommand()) {
                    sendPressCommand();
                    if (button.isRepeat()) {
                       Timer timer = new Timer();
                       timer.schedule(new TimerTask() {
                          public void run() {
                             sendPressCommand();
                          }
                       }, REPEAT_CMD_INTERVAL, REPEAT_CMD_INTERVAL);
                       setTimer(timer);
                    }
                 }
              } else if (event.getAction() == MotionEvent.ACTION_UP) {
                 cancelTimer();
                 if (defaultImage != null) {
                    defaultImage.setAlpha(255);
                    uiButton.setBackgroundDrawable(defaultImage);
                 } else {
                	 uiButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
                 }
                 if (button.getNavigate() != null) {
                    uiButton.setPressed(false);
                    ORListenerManager.getInstance().notifyOREventListener(ListenerConstant.ListenerNavigateTo, button.getNavigate());
                 }
              }
              return false;
           }
        };
        uiButton.setOnTouchListener(touchListener);
      } else if (button.getVersion() == 2) {
        View.OnTouchListener touchListener = new OnTouchListener() {
          public boolean onTouch(View v, MotionEvent event) {
             if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cancelTimers();
                if (pressedImage != null) {
                   uiButton.setBackgroundDrawable(pressedImage);
                } else if (defaultImage != null) {
                   defaultImage.setAlpha(200);
                   uiButton.setBackgroundDrawable(defaultImage);
                } else {
                  uiButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_pressed));
                }
                
                if (button.getPressCommandName() != null && !button.getPressCommandName().equals(""))
                {
                  sendNamedCommand(button.getPressCommandName());
                }
                
                // If repeat defined then setup timer
                if (button.getRepeatCommandName() != null && !button.getRepeatCommandName().equals("")) {
                  Timer timer = new Timer();
                  timer.schedule(new TimerTask() {
                     public void run() {
                         sendNamedCommand(button.getRepeatCommandName());
                     }
                  }, button.getRepeatInterval(), button.getRepeatInterval());
                  setTimer(timer);
               }
                
                // If long press defined then setup timer
                if (button.getLongPressCommandName() != null && !button.getLongPressCommandName().equals("")) {
                  Timer timer = new Timer();
                  timer. schedule(new TimerTask() {
                     public void run() {
                        sendNamedCommand(button.getLongPressCommandName());
                     }
                  }, button.getLongPressDelay());
                  longPressTimer = timer;
                }
                
             } else if (event.getAction() == MotionEvent.ACTION_UP) {
                cancelTimers();
                if (defaultImage != null) {
                   defaultImage.setAlpha(255);
                   uiButton.setBackgroundDrawable(defaultImage);
                } else {
                  uiButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
                }
                
                if (button.getReleaseCommandName() != null && !button.getReleaseCommandName().equals("")) {
                  sendNamedCommand(button.getReleaseCommandName());
                }
                
                if (button.getNavigate() != null) {
                   uiButton.setPressed(false);
                   ORListenerManager.getInstance().notifyOREventListener(ListenerConstant.ListenerNavigateTo, button.getNavigate());
                }
             }
             return false;
          }
       };
       uiButton.setOnTouchListener(touchListener);
      }
      
      addView(uiButton);
   }
    
   private void cancelTimers() {
     cancelTimer();
     if (longPressTimer != null) {
       longPressTimer.cancel();
       longPressTimer = null;
     }
   }

   private void sendPressCommand() {
      sendCommandRequest("click");
   }
   
   private void sendNamedCommand(String commandName) {
     sendCommandRequestByName(commandName);
   }
}
