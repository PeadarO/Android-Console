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
package org.openremote.android.console;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.openremote.android.console.bindings.Gesture;
import org.openremote.android.console.bindings.Group;
import org.openremote.android.console.bindings.Navigate;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.bindings.TabBar;
import org.openremote.android.console.bindings.TabBarItem;
import org.openremote.android.console.exceptions.ControllerAuthenticationFailureException;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ControllerException;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.OREvent;
import org.openremote.android.console.model.OREventListener;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.UserCache;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.model.XMLEntityDataBase;
import org.openremote.android.console.net.ControllerService;
import org.openremote.android.console.util.ImageUtil;
import org.openremote.android.console.view.GroupView;
import org.openremote.android.console.view.ScreenView;
import org.openremote.android.console.view.ScreenViewFlipper;

import roboguice.util.RoboAsyncTask;

import com.google.inject.Inject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.widget.LinearLayout;

/**
 * Controls all the screen views in a group.
 * The main operation of application is handled in it.
 * 
 * @author Tomsky Wang
 * @author Dan Cong
 * 
 */

public class GroupActivity extends GenericActivity implements OnGestureListener {

   /** To detect gesture, just for one finger touch. */
   private GestureDetector gestureScanner;
   private GroupView currentGroupView;
   
   /** The layout is the activity's content view, it contains currentScreenViewFlipper. */
   private LinearLayout contentLayout;
   
   /** The current screen view flipper contains current group views. */
   private ScreenViewFlipper currentScreenViewFlipper;
   private Screen currentScreen;
   private int screenSize;
   private HashMap<Integer, GroupView> groupViews;
   private ArrayList<Navigate> navigationHistory;
   private static final int SWIPE_MIN_DISTANCE = Screen.SCREEN_WIDTH / 4;
   private static final int SWIPE_THRESHOLD_VELOCITY = 20;

   private boolean useLocalCache;
   private boolean isNavigetionBackward;
   private boolean isLandscape = false;

   @Inject
   ControllerService controllerService;

   private int lastConfigurationOrientation = -1;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   	 	
      //Remove title bar
      this.requestWindowFeature(Window.FEATURE_NO_TITLE);

      //Remove notification bar
      this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      
      // Only hide the title bar if we are not on Android 3.0, as all of the current
      // Android 3.0 devices are tablets without menu buttons and the action bar
      // doesn't appear without the title bar's being shown.
      Log.i("VERSION.SDK_INT ", ""+VERSION.SDK_INT );      
   /*  
     if (VERSION.SDK_INT < 11) {
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      }
     */
      Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
      Log.i("OpenRemote-ORIENTATION", "onCreate:" + display.getOrientation());
      if (display != null && display.getOrientation() == 1) {
         isLandscape = true;
         lastConfigurationOrientation = Configuration.ORIENTATION_LANDSCAPE;
      } else {
         isLandscape = false;
         lastConfigurationOrientation = Configuration.ORIENTATION_PORTRAIT;
      }
      
      this.gestureScanner = new GestureDetector(this);

      if (groupViews == null) {
         groupViews = new HashMap<Integer, GroupView>();
      }
      if (navigationHistory == null) {
         navigationHistory = new ArrayList<Navigate>();
      }

      initGroupScreen();
      addControllerRefreshEventListener();

      initOrientationListener();
   }
   
   /**
    * Inits a orientation listener, set request orientation be sensor when the current screen's orientation equals device orientation.
    */
   private void initOrientationListener() {
      OrientationEventListener orientationListener = new OrientationEventListener(this) {
         @Override
         public void onOrientationChanged(int orientation) {
            if (currentScreen == null) {
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
               return;
            }
            if (orientation > 315 || orientation < 45  || (orientation > 135 && orientation < 225)) {
               // portrait
               if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                     && !currentScreen.isLandscape() && currentScreen.getInverseScreenId() > 0) {
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
               } else if (!currentScreen.isLandscape() && currentScreen.getInverseScreenId() == 0) {
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
               }
            } else if ((orientation > 225 && orientation < 315) || (orientation > 45 && orientation < 135)) {
               // landscape
               if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                     && currentScreen.isLandscape() && currentScreen.getInverseScreenId() > 0) {
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
               } else if (currentScreen.isLandscape() && currentScreen.getInverseScreenId() == 0) {
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
               }
            }
         }
     };
     orientationListener.enable();
   }

   /**
    * If the controller refreshed, finish this activity.
    */
   private void addControllerRefreshEventListener() {
      final Activity that = this;
      ORListenerManager.getInstance().addOREventListener(ListenerConstant.FINISH_GROUP_ACTIVITY, new OREventListener() {
         @Override
         public void handleEvent(OREvent event) {
            that.finish();
         }
      });
   }

   private int getCurrentGroupId() {
      return currentGroupView == null ? 0 : currentGroupView.getGroup().getGroupId();
   }

   /**
    * Gets the screen index in a group with the same landscape.
    * 
    * @param screenId the screen id
    * @param landscape the landscape
    * 
    * @return the screen index
    */
   private int getScreenIndex(int screenId, boolean landscape) {
      if (currentGroupView != null && currentGroupView.getGroup() != null) {
         return currentGroupView.getGroup().getScreenIndexByOrientation(XMLEntityDataBase.getScreen(screenId),
               landscape);
      }
      return -1;
   }

   /**
    * Init default group and screen.
    * 
    */
   private void initGroupScreen() {

      Group group = XMLEntityDataBase.getFirstGroup();

      if (group == null || group.getScreens().size() == 0) {
         if (!useLocalCache) {
            ViewHelper.showAlertViewWithSetting(this, "No Group Found", "Please check your panel definition");
         }
         return;
      }
      
      screenSize = group.getScreenSizeByOrientation(isLandscape);
      if (screenSize == 0) {
         ViewHelper.showAlertViewWithTitle(this, "Info", "The group " + group.getName() + " has no " + (isLandscape ? "landscape" : "portrait") + " screen.");
         isLandscape = !isLandscape;
         screenSize = group.getScreenSizeByOrientation(isLandscape);
      }
      
      // Set activity orientation
      int requestedOrientation = isLandscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
      this.setRequestedOrientation(requestedOrientation);
      
      currentGroupView = new GroupView(this, group);
      groupViews.put(group.getGroupId(), currentGroupView);
      currentScreenViewFlipper = currentGroupView.getScreenViewFlipperByOrientation(isLandscape);

//      int lastScreenID = UserCache.getLastScreenId(this);
//      
//      if (lastScreenID > 0 && group.canfindScreenByIdAndOrientation(lastScreenID, isLandscape)) {
//         currentScreenViewFlipper.setDisplayedChild(getScreenIndex(lastScreenID, isLandscape));
//    }
      
      contentLayout = new LinearLayout(this);
      contentLayout.addView(currentScreenViewFlipper);
      this.setContentView(contentLayout);
      ScreenView currentScreenView = (ScreenView) currentScreenViewFlipper.getCurrentView();
      if (currentScreenView == null) {
         return;
      }
      UserCache.saveLastGroupIdAndScreenId(GroupActivity.this, currentGroupView.getGroup().getGroupId(),
            currentScreenView.getScreen().getScreenId());
      currentScreen = currentScreenView.getScreen();

      addNaviagateListener();
   }

   /**
    * If the activity resumed, handle the navigation.
    */
   private void addNaviagateListener() {
      ORListenerManager.getInstance().addOREventListener(ListenerConstant.ListenerNavigateTo, new OREventListener() {
         public void handleEvent(OREvent event) {
            Navigate navigate = (Navigate) event.getData();
            if (isActivityResumed() && navigate != null) {
               handleNavigate(navigate);
            }
         }
      });
   }

   /**
    * Display the next screen in a group.
    * 
    * @return true, if successful
    */
   private boolean moveRight() {
      Log.d(this.toString(), "MoveRight");
      if (currentScreenViewFlipper.getDisplayedChild() < screenSize - 1) {
         cancelCurrentPolling();
         currentScreenViewFlipper.setToNextAnimation();
         currentScreenViewFlipper.showNext();
         startCurrentPolling();
         UserCache.saveLastGroupIdAndScreenId(GroupActivity.this, currentGroupView.getGroup().getGroupId(),
               ((ScreenView) currentScreenViewFlipper.getCurrentView()).getScreen().getScreenId());
         return true;
      }
      return false;
   }

   /**
    * Display the previous screen in a group.
    * 
    * @return true, if successful
    */
   private boolean moveLeft() {
      Log.d(this.toString(), "MoveLeft");
      if (currentScreenViewFlipper.getDisplayedChild() > 0) {
         cancelCurrentPolling();
         currentScreenViewFlipper.setToPreviousAnimation();
         currentScreenViewFlipper.showPrevious();
         startCurrentPolling();
         UserCache.saveLastGroupIdAndScreenId(GroupActivity.this, currentGroupView.getGroup().getGroupId(),
               ((ScreenView) currentScreenViewFlipper.getCurrentView()).getScreen().getScreenId());
         return true;
      }
      return false;
   }

   /**
    * Handle the touch event by GestureDetector.
    * 
    * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
    */
   @Override
   public boolean onTouchEvent(MotionEvent me) {
      return gestureScanner.onTouchEvent(me);
   }

   @Override
   public boolean onDown(MotionEvent e) {
      return false;
   }

   /**
    * Detect the gesture and handle it.
    * Support fling type: "right to left", "left to right", "bottom to top" and "top to bottom".
    * 
    * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
    */
   @Override
   public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      // The panel or group is empty.
      if (currentGroupView != null) {
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
           Log.i("OpenRemote-FLING", "right to left");
           onScreenGestureEvent(Gesture.GESTURE_SWIPE_TYPE_RIGHT2LEFT);
           Navigate navNext = new Navigate();
           navNext.setIsNextScreen(true);
           handleNavigate(navNext);
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
           Log.i("OpenRemote-FLING", "left to right");
           onScreenGestureEvent(Gesture.GESTURE_SWIPE_TYPE_LEFT2RIGHT);
           Navigate navPrevious = new Navigate();
           navPrevious.setIsPreviousScreen(true);
           handleNavigate(navPrevious);
        } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
           Log.i("OpenRemote-FLING", "bottom to top");
           onScreenGestureEvent(Gesture.GESTURE_SWIPE_TYPE_BOTTOM2TOP);
        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
           Log.i("OpenRemote-FLING", "top to bottom");
           onScreenGestureEvent(Gesture.GESTURE_SWIPE_TYPE_TOP2BOTTOM);
        }
      }
      return true;
   }

   /**
    * Handle screen gesture by type.
    * Support navigate and send command.
    * 
    * @param gestureType the gesture type
    */
   private void onScreenGestureEvent(int gestureType) {
      currentScreen = ((ScreenView) currentScreenViewFlipper.getCurrentView()).getScreen();
      final Gesture gesture = currentScreen.getGestureByType(gestureType);
      if (gesture != null) {
         if (gesture.isHasControlCommand()) {
            new RoboAsyncTask<Void>() {
              public Void call() throws Exception {
            	  Log.i("GroupActivity", "sendWriteCommand");
                controllerService.sendWriteCommand(gesture.getComponentId(), "swipe");
                
                return null;
              }

              public void onException(Exception e) {
                if (e instanceof ControllerAuthenticationFailureException) {
                  // TODO is this really what we want to do here?  Can this be refactored
                  // to make handling errors like this simpler for other small
                  // RoboAsyncTasks?
                  Intent loginIntent = new Intent();
                  loginIntent.setClass(getBaseContext(), LoginViewActivity.class);
                  loginIntent.setData(Uri.parse(Main.LOAD_RESOURCE));
                  finish();
                  getBaseContext().startActivity(loginIntent);
                }

                // TODO This is what we used to do when a response was given that did not have
                // 200 (OK) or 401 (unauthorized) as its response code.  Make sure we have an
                // adequate number of checked exceptions for what could go wrong and handle them here.

                // ViewHelper.showAlertViewWithTitle(this, "Send Request Error", ControllerException
                // .exceptionMessageOfCode(responseCode));
              }
            }.execute();
         }
         else if (gesture.getNavigate() != null) {
            handleNavigate(gesture.getNavigate());
         }
      }
   }

   @Override
   public void onLongPress(MotionEvent e) {
     loadSettings();
   }

   @Override
   public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      // Do nothing.
      return false;
   }

   @Override
   public void onShowPress(MotionEvent e) {
      // Do nothing.

   }

   @Override
   public boolean onSingleTapUp(MotionEvent e) {
      // Do nothing.
      return false;
   }

   public boolean onOptionsItemSelected(MenuItem item) {
      handleMenu(item);
      return true;
   }

   /**
    * Handle default menu("setting" and "logout").
    * 
    * @param item the item
    */
   public void handleMenu(MenuItem item) {
      switch (item.getItemId()) {
      case Constants.MENU_ITEM_SETTING:
         loadSettings();
         break;
      case Constants.MENU_ITEM_LOGOUT:
         doLogout();
         loadSettings();
         break;
         
      case Constants.MENU_ITEM_QUIT:
          doQuit();
          break;
      }
   }
   
   /**
    * Handle logout navigation, clear user password.
    */
   private void doQuit() {
	   //finish will finish current activity as well as inform parent activity that may be listening via startactivity for result. then parent activity finishes itself
    finish();
   }

   /**
    * Handle logout navigation, clear user password.
    */
   private void doLogout() {
      String username = UserCache.getUsername(GroupActivity.this);
      String password = UserCache.getPassword(GroupActivity.this);
      if (!TextUtils.isEmpty(password)) {
         UserCache.saveUser(GroupActivity.this, username, "");
      }
   }


   /**
    * If there is no global and local tabbar, create default menu.
    * Contains "setting" and "logout".
    * 
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.setQwertyMode(true);
      MenuItem setting = menu.add(-1, Constants.MENU_ITEM_SETTING, 0, R.string.setting);
      setting.setIcon(R.drawable.ic_menu_manage);
      MenuItem logout = menu.add(-1, Constants.MENU_ITEM_LOGOUT, 1, R.string.logout);
      logout.setIcon(R.drawable.ic_menu_revert);
      MenuItem quit = menu.add(-1, Constants.MENU_ITEM_QUIT, 2, R.string.quit);
      quit.setIcon(R.drawable.ic_menu_close_clear_cancel);
      return true;
   }

   /**
    * If there have global or local tabbar, create and update menu before the menu is shown.
    * 
    * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      if (currentGroupView == null || currentGroupView.getGroup() == null) {
         return true;
      }
      TabBar tabBar = currentGroupView.getGroup().getTabBar();
      if (tabBar == null) {
         tabBar = XMLEntityDataBase.globalTabBar;
      }
      if (tabBar != null && tabBar.getTabBarItems().size() > 0) {
         menu.clear();
         ArrayList<TabBarItem> items = tabBar.getTabBarItems();
         int itemSize = items.size();
         for (int i = 0; i < itemSize; i++) {
            MenuItem menuItem = menu.add(0, i, i, items.get(i).getName());
            if (items.get(i).getImage() != null) {
               menuItem.setIcon(ImageUtil.createFromPathQuietly(this, Constants.FILE_FOLDER_PATH
                     + items.get(i).getImage().getSrc(), 100, 100)); // Only need small images for tab bar
            }
            final Navigate navigate = items.get(i).getNavigate();
            if (navigate != null) {
               menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem item) {
                     handleNavigate(navigate);
                     return true;
                  }

               });
            }
         }
      }
      return true;
   }

   /**
    * Make current screen do polling.
    */
   private void startCurrentPolling() {
      if (currentScreenViewFlipper == null) {
         return;
      }
      ScreenView sv = (ScreenView) currentScreenViewFlipper.getCurrentView();
      if (sv != null) {
         currentScreen = sv.getScreen();
         sv.startPolling();
      }
   }

   /**
    * Make current screen stop polling.
    */
   private void cancelCurrentPolling() {
      if (currentScreenViewFlipper == null) {
         return;
      }
      ScreenView sv = (ScreenView) currentScreenViewFlipper.getCurrentView();
      if (sv != null) {
         sv.cancelPolling();
      }
   }

   /**
    * When the activity is stoped, unregister sensor listener,
    * stop current screen's polling.
    * 
    * @see android.app.Activity#onStop()
    */
   @Override
   protected void onStop() {
      super.onStop();
      cancelCurrentPolling();
   }

   /**
    * When the activity is destroyed, stop current screen's polling.
    * 
    * @see android.app.Activity#onDestroy()
    */
   @Override
   protected void onDestroy() {
      super.onDestroy();
      cancelCurrentPolling();
      ImageUtil.clearBitmaps();
   }

   @Override
   protected void onPause() {
    super.onPause();
    cancelCurrentPolling();
   }

   /**
    * When the activity is resumed, close the loading toast if it is not null,
    * indicate use cached content if load resources from controller error,
    * register the orientation sensor listener, start current screen's polling.
    *  
    * @see org.openremote.android.console.GenericActivity#onResume()
    */
   @Override
   protected void onResume() {
      super.onResume();
      if (Main.loadingToast != null) {
         Main.loadingToast.cancel();
      }
      if (getIntent().getDataString() != null) {
         useLocalCache = true;
         ViewHelper.showAlertViewWithSetting(this, "Using cached content", getIntent().getDataString());
      }
      startCurrentPolling();
   }

   /**
    * Handle the 7 sorts of navigate.
    * 
    * @param navigate the navigate
    * 
    * @return true, if successful
    */
   private boolean navigateTo(Navigate navigate) {
      if (navigate.isNextScreen()) {
         return moveRight();
      } else if (navigate.isPreviousScreen()) {
         return moveLeft();
      } else if (navigate.getToGroup() > 0) {
         return navigateToGroup(navigate.getToGroup(), navigate.getToScreen());
      } else if (navigate.isBack()) {
         if (navigationHistory.size() > 0) {
            Navigate backward = navigationHistory.get(navigationHistory.size() - 1);
            if (backward.getFromGroup() > 0 && backward.getFromScreen() > 0) {
               if (backward.getFromGroup() == getCurrentGroupId()) {
                  isNavigetionBackward = getScreenIndex(backward.getFromScreen(), isLandscape) < getScreenIndex(
                        currentScreen.getScreenId(), isLandscape);
               }
               navigateToGroup(backward.getFromGroup(), backward.getFromScreen());
            }
            navigationHistory.remove(backward);
         } else {
           loadSettings();
         }
      } else if (navigate.isSetting()) {
         loadSettings();
      } else if (navigate.isLogin()) {
         Intent intent = new Intent();
         intent.setClass(GroupActivity.this, LoginViewActivity.class);
         startActivity(intent);
      } else if (navigate.isLogout()) {
         doLogout();
         loadSettings();
      }
      return false;
   }
   
 
   /**
    * Handle the navigate to group, it contains to group and to screen.
    * 
    * @param toGroupId the to group id
    * @param toScreenId the to screen id
    * 
    * @return true, if successful
    */
   private boolean navigateToGroup(int toGroupId, int toScreenId) {
      Group targetGroup = XMLEntityDataBase.getGroup(toGroupId);
      if (targetGroup != null) {
         cancelCurrentPolling();
         boolean currentOrientation = currentScreen.isLandscape();
         
         if (currentGroupView.getGroup().getGroupId() != toGroupId) {
            // in different group.
            GroupView targetGroupView = groupViews.get(toGroupId);
            if (targetGroupView == null) {
               targetGroupView = new GroupView(this, targetGroup);
               groupViews.put(toGroupId, targetGroupView);
            }
            if (targetGroup.getScreens().size() == 0) {
               return false;
            }
            
            boolean newOrientation = currentOrientation;
            if (toScreenId > 0) {
               newOrientation = XMLEntityDataBase.getScreen(toScreenId).isLandscape();
            }
            if (! targetGroup.hasOrientationScreens(newOrientation)) {
               newOrientation = !newOrientation;
            }
            
            contentLayout.removeView(currentScreenViewFlipper);
            currentScreenViewFlipper = targetGroupView.getScreenViewFlipperByOrientation(newOrientation);
            currentGroupView = targetGroupView;
            contentLayout.addView(currentScreenViewFlipper);
            if (toScreenId > 0 && targetGroup.canfindScreenByIdAndOrientation(toScreenId, newOrientation)) {
               currentScreenViewFlipper.setDisplayedChild(getScreenIndex(toScreenId, newOrientation));
            }
            screenSize = targetGroup.getScreenSizeByOrientation(newOrientation);
            if (newOrientation != currentOrientation) {
               manualRotateScreen(newOrientation);
            }
            
         } else if (toScreenId > 0) {
            // in same group.
            boolean newOrientation = XMLEntityDataBase.getScreen(toScreenId).isLandscape();
            if (newOrientation != currentOrientation) {
               screenSize = targetGroup.getScreenSizeByOrientation(newOrientation);
               contentLayout.removeView(currentScreenViewFlipper);
               currentScreenViewFlipper = currentGroupView.getScreenViewFlipperByOrientation(newOrientation);
               contentLayout.addView(currentScreenViewFlipper);
               manualRotateScreen(newOrientation);
            }
            if (isNavigetionBackward) {
               currentScreenViewFlipper.setToPreviousAnimation();
            } else {
               currentScreenViewFlipper.setToNextAnimation();
            }
            currentScreenViewFlipper.setDisplayedChild(getScreenIndex(toScreenId, newOrientation));
         }
         startCurrentPolling();
         currentScreen = ((ScreenView) currentScreenViewFlipper.getCurrentView()).getScreen();
         return true;
      }
      return false;
   }

   /**
    * @param newOrientation
    */
   private void manualRotateScreen(boolean newOrientation) {
      if (newOrientation) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         lastConfigurationOrientation = Configuration.ORIENTATION_LANDSCAPE;
         isLandscape = true;
      } else {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         lastConfigurationOrientation = Configuration.ORIENTATION_PORTRAIT;
         isLandscape = false;
      }
   }

   /**
    * Handle navigate, if navigate to group or to screen success, store the navigation history(for back navigation).
    * 
    * @param navigate the navigate
    */
   private void handleNavigate(Navigate navigate) {
      Navigate historyNavigate = new Navigate();
      if (currentGroupView.getGroup() != null) {
         historyNavigate.setFromGroup(currentGroupView.getGroup().getGroupId());
         if (currentScreenViewFlipper == null) {
            return;
         }
         ScreenView sv = (ScreenView) currentScreenViewFlipper.getCurrentView();
         if (sv == null) {
            return;
         } else {
            historyNavigate.setFromScreen(sv.getScreen().getScreenId());
         }
         if (navigateTo(navigate)) {
            UserCache.saveLastGroupIdAndScreenId(GroupActivity.this, currentGroupView.getGroup().getGroupId(),
                  ((ScreenView) currentGroupView.getScreenViewFlipperByOrientation(isLandscape).getCurrentView())
                        .getScreen().getScreenId());
            navigationHistory.add(historyNavigate);
         }
      }
   }

   /**
    * Check the current screen if can rotate.
    * 
    * @return true, if successful
    */
   private boolean canRotateToInterfaceOrientation() {
      if (currentScreen != null && currentScreen.getScreenId() > 0) {
         return currentScreen.getInverseScreenId() > 0;
      }
      return false;
   }

   /**
    * Display the current screen's inverse screen.
    */
   private void rotateToIntefaceOrientation() {
      if (currentScreen == null) return;
      int inverseScreenId = currentScreen.getInverseScreenId();
      if (currentGroupView != null) {
         cancelCurrentPolling();
         contentLayout.removeView(currentScreenViewFlipper);
         currentScreenViewFlipper = currentGroupView.getScreenViewFlipperByOrientation(isLandscape);
         contentLayout.addView(currentScreenViewFlipper);
         currentScreenViewFlipper.setDisplayedChild(getScreenIndex(inverseScreenId, isLandscape));
         startCurrentPolling();
         if (currentGroupView.getGroup() != null) {
            screenSize = currentGroupView.getGroup().getScreenSizeByOrientation(isLandscape);
            UserCache.saveLastGroupIdAndScreenId(GroupActivity.this, currentGroupView.getGroup().getGroupId(),
                  ((ScreenView) currentScreenViewFlipper.getCurrentView()).getScreen().getScreenId());
         }
      }
   }

   /**
    * Detect the phone's orientation and display the corresponding screen.
    * 
    * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
    */
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
      int newOrientation = newConfig.orientation;
      Log.i("OpenRemote-ORIENTATION", "orientation:" + newOrientation);
      if (lastConfigurationOrientation != newOrientation) {
         if (newOrientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;
         } else if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
         }
         if (canRotateToInterfaceOrientation()) {
            rotateToIntefaceOrientation();
         }
      }
      super.onConfigurationChanged(newConfig);
      lastConfigurationOrientation = newOrientation;
   }

//   @Override
//   public boolean dispatchTouchEvent(MotionEvent ev) {
//	   boolean handled = gestureScanner.onTouchEvent(ev);
//	   if (!handled) {
//		   handled = super.dispatchTouchEvent(ev);
//	   }
//       return handled;
//   }
   
// /**
// * If press back key, finish the activity.
// * 
// * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
// */
//@Override
//public boolean onKeyDown(int keyCode, KeyEvent event) {
//   if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//      finish();
//      return true;
//   }
//   return super.onKeyDown(keyCode, event);
//}
   
   @Override
   public void onBackPressed() 
   {
	   Navigate nav = new Navigate();
	   nav.setIsBack(true);
	   navigateTo(nav);
	   // TODO
   }
   
   private void loadSettings() {
     AppSettingsModel.setCurrentController(this, null);
     AppSettingsModel.setCurrentPanelIdentity(this, null);
  	 Intent i = new Intent(this, AppSettingsActivity.class);
  	 i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  	 startActivity(i);
  	 finish();
   }
}
