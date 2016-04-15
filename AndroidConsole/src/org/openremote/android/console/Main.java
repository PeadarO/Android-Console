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

import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.net.IPAutoDiscoveryClient;
import org.openremote.android.console.util.AsyncResourceLoader;
import org.openremote.android.console.util.ImageUtil;

import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class represents the main OpenRemote activity. It starts up, reads the
 * xml file via AsyncResourceLoader class. It is launched via an intent to the GroupActivity class.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * @author Andrew C. Oliver <acoliver at osintegrators.com> 
 *         Tomsky
 */
public class Main extends GenericActivity {

    LinearLayout activitiesListView;
    public static final String LOAD_RESOURCE = "loadResource";
    public static boolean isRefreshingController;
    public static Toast loadingToast;
    private AsyncResourceLoader loader;
    
    @InjectView(R.id.loading_text)
    private TextView loadingText;

    /** Called when the activity is first created. */
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*
         * Temporary fix for ANDROID-90 Bug relating to NetworkOnMainThreadException
         */
        if (android.os.Build.VERSION.SDK_INT > 9) {
          StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
          StrictMode.setThreadPolicy(policy);
        }
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                
        loadingToast = Toast.makeText(this, "Refreshing from Controller...", Integer.MAX_VALUE);
//        if (!isRefreshingController) {
//        } else {
//           loadingToast.show();
//        }
        ImageUtil.setContentViewQuietly(this, R.layout.welcome_view);
        isRefreshingController = false;
        
        Button cancelButton = (Button)findViewById(R.id.cancel_button);
        cancelButton.setClickable(true);
        cancelButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            loader.setIsCancelled(true); // cancel method doesn't happen straight away onSuccess can be called before onInterrupted (useless)
            loader.cancel(true);
            doSettings();
          }});
        
        checkNetType();
        readDisplayMetrics();   
        
        ControllerObject controller = AppSettingsModel.getCurrentController(this);
        
        if (controller == null || TextUtils.isEmpty(AppSettingsModel.getCurrentPanelIdentity(this))) {
          doSettings();
        } else {
           loader = getInjector().getInstance(AsyncResourceLoader.class);
           loader.setLoadingText(loadingText);
           loader.setActivity(this);
           loader.execute();
        }
    }
    
    /**
     * Display toast with message "switching controller".
     */
    public static void prepareToastForSwitchingController() {
       isRefreshingController = true;
       loadingToast.setText("Switching from Controller...");
    }
    
    /**
     * Display toast with message "refreshing controller".
     */
    public static void prepareToastForRefreshingController() {
       isRefreshingController = true;
       loadingToast.setText("Refreshing Controller...");
    }
    
    /**
     * Check net type.
     * It is used for ipAutoDiscovery, if net type is not wifi,
     * multicast to non wifi address.
     */
    private void checkNetType() {
       ConnectivityManager conn = (ConnectivityManager)(this).getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo info = conn.getActiveNetworkInfo();
       String type = info == null ? null : info.getTypeName();
       if (type != null && "wifi".equals(type.toLowerCase())) {
          IPAutoDiscoveryClient.isNetworkTypeWIFI = true;
       } else  {
          IPAutoDiscoveryClient.isNetworkTypeWIFI = false;
       }
    }
    
    /**
     * Read display metrics.
     * Include screen width and height.
     */
    private void readDisplayMetrics() {
      DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
      ControllerObject controller = AppSettingsModel.getCurrentController(this);
      if (dm.widthPixels > dm.heightPixels) {
        Screen.SCREEN_WIDTH = dm.heightPixels;
        Screen.SCREEN_HEIGHT = dm.widthPixels;
      } else {
        Screen.SCREEN_WIDTH = dm.widthPixels;
        Screen.SCREEN_HEIGHT = dm.heightPixels;        
      }

      double xScale = 1.0;
	  double yScale = 1.0;
      if ( controller != null ) {
         xScale = controller.getXScale();
         yScale = controller.getYScale();
	  }
      Screen.WIDTH_SCALE =  xScale;
      Screen.HEIGHT_SCALE = yScale;
    }
      
    
    public void setActivityToFinish(Activity activity){
    	
    }

    /**
     * Forward to settings view.
     */
    private void doSettings() {
      AppSettingsModel.setCurrentController(this, null);
      AppSettingsModel.setCurrentPanelIdentity(this, null);
      Intent i = new Intent(this, AppSettingsActivity.class);
      startActivity(i);
      finish();
    }

  
}