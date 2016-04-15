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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.openremote.android.console.Constants;
import org.openremote.android.console.ControllerDataHelper;
import org.openremote.android.console.ControllerObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.openremote.android.console.net.ORHttpMethod;

/**
 * Controller IP auto discovery server, this is a TCP server receiving IP from Controllers.
 *
 * @author Tomsky Wang
 *
 */
														//params,progress,report		
public class SavedServersNetworkCheckTestAsyncTask extends AsyncTask<String, String, ArrayList<ControllerObject>> {
  public static final String TAG = Constants.LOG_CATEGORY + "SavedServersNetworkCheckTestAsyncTask";

      Context ctx;
      ProgressDialog pDialog;
      
      public SavedServersNetworkCheckTestAsyncTask(Context context){
    	  this.ctx=context;
      }
   
      @Override
      protected ArrayList<ControllerObject> doInBackground(String... params) {
    	  HttpResponse response = null;
          Log.i("LOGGER", "Starting...");
    	  ArrayList<ControllerObject> customServers = new ArrayList<ControllerObject>();
    	  
    	  ControllerDataHelper dh = new ControllerDataHelper(ctx);
    	    customServers = dh.getAllControllers();
    	    ArrayList<ControllerObject> customServersNew = new ArrayList<ControllerObject>(customServers.size());
    	    
    	    //ping test all controllers to check if they are available
    	    for(int i=0;i<customServers.size();i++){
    	    	
    	    	ControllerObject co=customServers.get(i);
    	    	boolean coUp;
    	    	
                try{
                    response = ORConnection.checkURLWithHTTPProtocol(ORHttpMethod.GET,new URL(co.getUrl()),false);
                   
                    Log.i(TAG,co.getUrl()+ "response: "+response.getStatusLine());
                    coUp=true;
                    }
                    catch(Exception e){
                  	  e.printStackTrace();
                  	  coUp=false;
                    }
    	    	
    	    	
    	    	    co.setIsControllerUp(coUp);	    	
    	    	customServersNew.add(co);
    	    }
    	    
    	    customServers=customServersNew;	
    	 
    	    //dh.closeConnection();
    
           return customServers;
         
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
