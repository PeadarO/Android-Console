/*
 * OpenRemote, the Home of the Digital Home.
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
package org.openremote.android.console.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.Constants;
import org.openremote.android.console.ControllerDataHelper;
import org.openremote.android.console.ControllerObject;
import org.openremote.android.console.Main;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.PollingHelper;
import org.openremote.android.console.util.StringUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * This is responsible for detecting failover group members and switching to available controller.
 * 
 * @author handy 2010-04-29
 * @author Andrew D. Ball <aball@osintegrators.com>
 */
@Singleton
public class ORControllerServerSwitcher
{
	
	//maybe make this global, use BusinessDelegate pattern
	


  private ORNetworkCheck orNetworkCheck;
  private static Context context;
  private ControllerDataHelper dh;

  public static class ControllerCheckResult
  {
    public ControllerCheckResult(boolean contactable, Date timeOfCheck)
    {
      this.contactable = contactable;
      this.timeOfCheck = timeOfCheck;
    }
    public boolean isContactable()
    {
      return contactable;
    }
    public Date getTimeOfCheck()
    {
      return timeOfCheck;
    }
    private boolean contactable;
    private Date timeOfCheck;
  }

  @Inject
  public ORControllerServerSwitcher(ORNetworkCheck orNetworkCheck, Context context)
  {
    this.orNetworkCheck = orNetworkCheck;
    this.context = context;

    controllerCheckStatus = new HashMap<URL, ControllerCheckResult>();
  }

  /**
   * This stores the result of controller presence checks, indexed by their URLs.
   */
  private Map<URL, ControllerCheckResult> controllerCheckStatus;

  // Constants ------------------------------------------------------------------------------------

  /**
   * Common log category for fail-over functionality.
   */
  public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + "Failover";

  private static final String SERIALIZE_GROUP_MEMBERS_FILE_NAME = "group_members";
  public static final int SWITCH_CONTROLLER_SUCCESS = 1;
  public static final int SWITCH_CONTROLLER_FAIL = 2;


  // Class Members --------------------------------------------------------------------------------


  
//  public boolean saveGroupMembersToDB(List<URL> groupMembers, String failoverFor)
//  {
//	  dh=new DataHelper(this.context);
//	    for (int i = 0; i < groupMembers.size(); i++)
//	    {
//	    	  dh.insert(groupMembers.get(i).toString(), "grp1", 1,1,0,failoverFor);
//	    }
//
//	    //return editor.commit();
//	    dh.closeConnection();
//       
//	  return false;
//  }
  
  /**
   * doswitch in polling helper instead of here
   */
  public void doSwitch(Context context)
  {
  /*  URL availableGroupMemberURL = getOneAvailableFromGroupMemberURLs();
    AppSettingsModel.setCurrentServer(context, availableGroupMemberURL);*/

  /*  List<String> allGroupMembers = findAllGroupMembersFromFile(context);

    if (availableGroupMemberURL != null)
    {
      Log.i(LOG_CATEGORY, "Got an available controller URL from group members" + allGroupMembers);
      AppSettingsModel.setCurrentServer(context, availableGroupMemberURL);
    }*/

      //switchControllerWithURL(context, availableGroupMemberURL);
    }
  /**
   * Check each group member's URL and get an available one. Depends on the WiFi network.
   *
   * @param context global Android application context
   *
   * @return  TODO
   */
  public static ControllerObject getOneAvailableFromGroupMemberURLs(ControllerObject controller, ControllerDataHelper dh)
  {
	  HttpResponse response= null;
	  
    String[] allGroupMembers = controller.getFailoverControllers();
    
    //ping test all controllers to check if they are available
    for(int i=0; i<allGroupMembers.length; i++) {
    	String url = allGroupMembers[i];
    	boolean coUp;
    	
        try{
            response = ORConnection.checkURLWithHTTPProtocol(ORHttpMethod.GET,new URL(url),false);
            Log.i(LOG_CATEGORY, url + "response: "+response.getStatusLine());
            coUp=true; //if you found one, break
            
          /*  if (response != null && response.getStatusLine().getStatusCode() == Constants.HTTP_SUCCESS)
            {
             
              return controllerURL;
            }*/
            
            return new ControllerObject(url,controller.getDefaultPanel(), 
            		controller.getUsername(), controller.getUserPass(), 
            		controller.getXScale(), controller.getYScale());
        }
        catch(Exception e){
      	  e.printStackTrace();
      	  coUp=false;
        }    	
    }  

    return null;
  }
  
  /**
   * Switch to the controller identified by the availableGroupMemberURL
   *
   * @param context                 global Android application context
   * @param availableGroupMemberURL TODO
   */
  public static void switchControllerWithURL(Context context, String availableGroupMemberURL)
  {
    if (availableGroupMemberURL.equals(AppSettingsModel.getCurrentController(context).getUrl()))
    {
      Log.i(
          LOG_CATEGORY,
          "The current server is already: " + availableGroupMemberURL +
          ", should not switch to self."
      );

      return;
    }

//    Main.prepareToastForSwitchingController();
    
    Log.i(LOG_CATEGORY, "ControllerServerSwitcher is switching controller to " + availableGroupMemberURL);

    try {
		AppSettingsModel.setCurrentController(context, new URL(availableGroupMemberURL));
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  /*  Intent intent = new Intent();
    intent.setClass(context, Main.class);
    finish();
    startActivity(intent);*/
    
    
  }

}
