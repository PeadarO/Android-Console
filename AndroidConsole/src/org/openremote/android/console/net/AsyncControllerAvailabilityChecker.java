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
package org.openremote.android.console.net;

import java.lang.ref.WeakReference;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.openremote.android.console.Constants;
import org.openremote.android.console.ControllerObject;
import org.openremote.android.console.R;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.view.ControllerListItemLayout;
/**
 * Controller availability checker checks that the controller is alive and the status of it.
 *
 * @author Rich Turner
 *
 */
		
public class AsyncControllerAvailabilityChecker extends AsyncTask<String, String, Boolean> {
	public static final String TAG = Constants.LOG_CATEGORY + "ControllerAvailabilityChecker";
	private final WeakReference<ControllerListItemLayout> viewLayoutRef;
	private final WeakReference<ControllerObject> controllerRef;
	
  public AsyncControllerAvailabilityChecker(ControllerListItemLayout viewLayout, ControllerObject controller) {
  	viewLayoutRef = new WeakReference<ControllerListItemLayout>(viewLayout);
  	controllerRef = new WeakReference<ControllerObject>(controller);
  }
   
  @Override
  protected Boolean doInBackground(String... params) {
	  HttpResponse response = null;
    Log.i(TAG, "Starting...");
	  boolean isAvailable = false;
	  String controllerUrl = "";
	  
	  // Get Controller URL parameter
	  ControllerObject controller = controllerRef.get();
	  controllerUrl = controller.getUrl();
	  
	  if (TextUtils.isEmpty(controllerUrl)) {
	  	return false;
	  }
	  
	  // Check controller /rest/servers if response received then Controller is available
    try{
      response = ORConnection.checkURLWithHTTPProtocol(ORHttpMethod.GET, new URL(controllerUrl + "/rest/servers"), false, 5000);
     
      if (response != null && (response.getStatusLine().getStatusCode() != Constants.HTTP_SUCCESS || response.getStatusLine().getStatusCode() != Constants.HTTP_UNAUTHORISED)) {
	      Log.i(TAG, controllerUrl + " Response: "+ response.getStatusLine());
	      isAvailable=true;
      }
    }
    catch(Exception e){
  	  e.printStackTrace();
    }
    
    return isAvailable;
  }

	@Override
	public void onPostExecute(Boolean result) {
		// Update the Controller Object with the result
		if (controllerRef != null) {
			ControllerObject controller = controllerRef.get();
			if (controller != null) {
				controller.setIsControllerUp(result);
				controller.setAvailabilityCheckInProgress(false);
				controller.setAvailabilityCheckDone(true);
			}
		}
		
		// If view Layout is still linked to this task then update it as well
		if (viewLayoutRef != null) {
			ControllerListItemLayout itemLayout = viewLayoutRef.get();
			if (itemLayout != null) {
  			AsyncControllerAvailabilityChecker checkerTask = itemLayout.getCheckerTask();
  			if (this == checkerTask) {
  			  // Set availability indicator on controller item and make item checkable if available
  			  ProgressBar pb = (ProgressBar)itemLayout.findViewById(R.id.controller_status_searching);
  			  pb.setVisibility(View.GONE);
  			  
  			  if (result) {
  			  	ImageView ok = (ImageView)itemLayout.findViewById(R.id.controller_status_ok);
  			  	ImageView nok = (ImageView)itemLayout.findViewById(R.id.controller_status_nok);
  			  	ok.setVisibility(View.VISIBLE);
  			  	nok.setVisibility(View.GONE);
  			  	//itemLayout.setCheckable(true);
  			  } else {
  			  	ImageView ok = (ImageView)itemLayout.findViewById(R.id.controller_status_ok);
  				  ImageView nok = (ImageView)itemLayout.findViewById(R.id.controller_status_nok);
  				  nok.setVisibility(View.VISIBLE);
  			  	ok.setVisibility(View.GONE);
  			  	//itemLayout.setCheckable(false);
  			  }
  			}
			}
		}
	}
  
  /*    @Override
      protected void onPostExecute(String result) {

                     
                      Log.i("LOGGER", "Done...");
                      pDialog.dismiss();
                      if(result=="success"){
                              Toast.makeText(ctx, "complete", Toast.LENGTH_LONG).show();
                             // Intent i=new Intent(ctx,SillyActivity.class);
                              //startActivity(i);
                      }else{
                              Toast.makeText(ctx, "fail", Toast.LENGTH_LONG).show();
                      }

              super.onPostExecute(result); 
      }*/

   

}
