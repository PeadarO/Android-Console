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
package org.openremote.android.console.model;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.Constants;
import org.openremote.android.console.ControllerDataHelper;
import org.openremote.android.console.ControllerObject;
import org.openremote.android.console.Main;
import org.openremote.android.console.net.IPAutoDiscoveryClient;
import org.openremote.android.console.net.ORControllerServerSwitcher;
import org.openremote.android.console.net.SelfCertificateSSLSocketFactory;
import org.openremote.android.console.util.SecurityUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Polling Helper, this class will setup a polling thread to listen 
 * and notify screen component status changes.
 * 
 * @author Tomsky Wang, Dan Cong
 * 
 */
public class PollingHelper {

   /** The polling status ids is split by ",". */
   private String pollingStatusIds;
   /** whether polling is enabled for ? */
   private boolean isPolling;
   private HttpClient client;
   private HttpGet httpGet;
   private URL serverUrl;
   private Context context;
   private static String deviceId = null;
   private Handler handler;
   private static final int NETWORK_ERROR = 0;
   private final static String LOG_CATEGORY = Constants.LOG_CATEGORY + "POLLING";

   /**
    * Instantiates a new polling helper.
    * 
    * @param ids the ids
    * @param context the context
    */
   public PollingHelper(HashSet<Integer> ids, final Context context) {
      this.context = context;
      this.serverUrl = AppSettingsModel.getSecuredServer(context);
      readDeviceId(context);
      
      Iterator<Integer> id = ids.iterator();
      if (id.hasNext()) {
         pollingStatusIds = id.next().toString();
      }
      while (id.hasNext()) {
         pollingStatusIds = pollingStatusIds + "," + id.next();
      }
      
      handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
            isPolling = false;
            Log.i(LOG_CATEGORY, "polling failed and canceled." + msg.what);
            // only if the network is error, server error and request error, 
            // switch controller server, or endless loop would happen to switch server.
            int statusCode = msg.what;
//            if (statusCode == NETWORK_ERROR || statusCode == ControllerException.SERVER_ERROR
//                  || statusCode == ControllerException.REQUEST_ERROR) {
               // TODO switching the controller should probably happen in the implementation of
               //      the ControllerService interface

               // ORControllerServerSwitcher.doSwitch();
//            } else {
               // TODO ANDROID--89 Gracefully fail on polling errors
               //      don't be so loud and disruptive here
            
            if (ControllerException
                    .exceptionMessageOfCode(statusCode).equals("Current controller isn't available.")){
            	ControllerDataHelper dh = new ControllerDataHelper(context);
            	
            ControllerObject availableGroupMemberURL = ORControllerServerSwitcher.getOneAvailableFromGroupMemberURLs(AppSettingsModel.getCurrentController(context),dh);
            	 
            //dh.closeConnection();
            
            //if none vailable show dialog for now and finish
            Log.i(LOG_CATEGORY, "availableGroupMemberURL." + availableGroupMemberURL);
            if(availableGroupMemberURL==null){
            	 // ViewHelper.showAlertViewWithTitle(context, "Polling Error", ControllerException.exceptionMessageOfCode(statusCode));
            	 ViewHelper.showAlertViewWithSetting(context, "Switch controller", "Choose a different controller");
            	//  this.notifyAll();
            	 
            }
         
            else{
            	ORControllerServerSwitcher.switchControllerWithURL(context, availableGroupMemberURL.getUrl());
						//AppSettingsModel.setCurrentServer(context, new URL(availableGroupMemberURL.getControllerName()));
            }
					
            	    }

            
//            }
         }
      };
   }

   /**
    * Request current status and start polling.
    */
   public void requestCurrentStatusAndStartPolling() {
      HttpParams params = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(params, 50 * 1000);
      
      // make polling socket timeout bigger than Controller (50s)
      HttpConnectionParams.setSoTimeout(params, 55 * 1000);
      
      client = new DefaultHttpClient(params);
      if (isPolling) {
         // TODO Explain this part! We don't want to start polling again for the same thing?
         return;
      }
      
      try {
         serverUrl.toURI();
         if ("https".equals(serverUrl.getProtocol())) {
            Scheme sch = new Scheme(serverUrl.getProtocol(), new SelfCertificateSSLSocketFactory(), serverUrl.getPort());
            client.getConnectionManager().getSchemeRegistry().register(sch);
         }
      } catch (URISyntaxException e) {
         Log.e(LOG_CATEGORY, "Could not convert " + serverUrl + " to a compliant URI");
         return;
      }
      isPolling = true;
      handleRequest(serverUrl + "/rest/status/" + pollingStatusIds);
      while (isPolling) {
         doPolling();
      }
   }

   private void doPolling() {
      if (httpGet != null) {
         httpGet.abort();
         httpGet = null;
      }
      Log.i(LOG_CATEGORY, "polling start");
      handleRequest(serverUrl + "/rest/polling/" + deviceId + "/" + pollingStatusIds);
   }

   /**
    * Execute request and handle the result.
    * 
    * @param requestUrl the request url
    */
   private void handleRequest(String requestUrl) {
      Log.i(LOG_CATEGORY, requestUrl);
      httpGet = new HttpGet(requestUrl);
      // why would httpGet ever be aborted here, right after instantiation?
      if (!httpGet.isAborted()) {
         SecurityUtil.addCredentialToHttpRequest(context, httpGet);
         try {
            HttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == Constants.HTTP_SUCCESS) {
               PollingStatusParser.parse(response.getEntity().getContent());
            } else {
               response.getEntity().getContent().close();
               handleServerErrorWithStatusCode(statusCode);
            }
            return;
         } catch (SocketTimeoutException e) {
            Log.i(LOG_CATEGORY, "polling [" + pollingStatusIds +"] socket timeout.");
         } catch (ClientProtocolException e) {
            isPolling = false;
            Log.e(LOG_CATEGORY, "polling [" + pollingStatusIds +"] failed.", e);
            handler.sendEmptyMessage(NETWORK_ERROR);
         } catch (SocketException e) {
        	if (isPolling) {
        		//isPolling = false;
        		//handler.sendEmptyMessage(NETWORK_ERROR);
            Log.e(LOG_CATEGORY, "polling [" + pollingStatusIds +"] failed.", e);
        	}
         } catch (IllegalArgumentException e) {
            isPolling = false;
            Log.e(LOG_CATEGORY, "polling [" + pollingStatusIds +"] failed", e);
            handler.sendEmptyMessage(NETWORK_ERROR);
         } catch (OutOfMemoryError e) {
            isPolling = false;
            Log.e(LOG_CATEGORY, "OutOfMemoryError");
         } catch (InterruptedIOException e) {
            isPolling = false;
            Log.i(LOG_CATEGORY, "last polling [" + pollingStatusIds +"] has been shut down");
         } catch (IOException e) {
            isPolling = false;
            Log.i(LOG_CATEGORY, "last polling [" + pollingStatusIds +"] already aborted");
         }
         
      }
   }
   
   /**
    * Cancel the polling, abort HTTP request.
    */
   public void cancelPolling() {
      Log.i(LOG_CATEGORY, "polling [" + pollingStatusIds +"] canceled");
      isPolling = false;
      if (httpGet != null) {
         httpGet.abort();
         httpGet = null;
      }
   }

   /**
    * Handle server error with status code.
    * If request timeout, return and start a new request.
    * 
    * @param statusCode the status code
    */
   private void handleServerErrorWithStatusCode(int statusCode) {
      if (statusCode != Constants.HTTP_SUCCESS) {
         httpGet = null;
         if (statusCode == ControllerException.GATEWAY_TIMEOUT) { // polling timeout, need to refresh
            return;
         } if (statusCode == ControllerException.REFRESH_CONTROLLER) {
            // TODO explain "refreshing the controller"
            Main.prepareToastForRefreshingController();
         	 	Intent i = new Intent(context, Main.class);
         	 	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);

            // Notify the group activity to finish.
            ORListenerManager.getInstance().notifyOREventListener(ListenerConstant.FINISH_GROUP_ACTIVITY, null);
            return;
         } else {
            isPolling = false;
            handler.sendEmptyMessage(statusCode);
         }
      }
   }

   /**
    * Read the device id for send it in polling request URL.
    * 
    * @param context the context
    */
   private static void readDeviceId(Context context) {
      if (deviceId == null) {
        deviceId = UUID.randomUUID().toString();
      }
   }

   
}
