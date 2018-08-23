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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.net.ControllerService;
import org.openremote.android.console.net.IPAutoDiscoveryServer;
import org.openremote.android.console.net.ORControllerServerSwitcher;
import org.openremote.android.console.net.SavedServersNetworkCheckTestAsyncTask;
import org.openremote.android.console.util.FileUtil;
import org.openremote.android.console.view.PanelSelectSpinnerView;

import com.google.inject.Inject;

import roboguice.util.RoboAsyncTask;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Controller List View for displaying saved and auto-discovered controllers
 * and selecting which controller to load as well as which panel.
 * Can also clear the image cache.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * @author Tomsky Wang
 * @author Dan Cong
 */

public class AppSettingsActivity extends GenericActivity {
  public static final String TAG = Constants.LOG_CATEGORY + "AppSettingsActivity";
  
  private ControllerDataHelper dh;

  private LinearLayout appSettingsView;
  
  /** List View for Controllers */
  private ListView controllerListView;
  
  private ControllerObject selectedController;
  
  /** The view for selecting panel identity. */
  private PanelSelectSpinnerView panelSelectSpinnerView;
  
  /** The progress layout display auto discovery progress. */
  private LinearLayout progressLayout;

  private ControllerListAdapter serverListAdapter;
  
  private ControllerObject editController;
  
  private SwipeRefreshLayout serversLayout;
  
  private ProgressDialog loadingPanelProgress;
  
  private IPAutoDiscoveryServer autoDiscoveryServer;

  private SavedServersNetworkCheckTestAsyncTask checkedControllers;
  
  //private boolean fullOptions=false;
  public int selectedPosition=-1;
 
  @Inject
  private ORControllerServerSwitcher orControllerServerSwitcher;

  @Override
  public void onCreate(Bundle savedInstanceState) { 
    super.onCreate(savedInstanceState);
        
    setContentView(R.layout.controller_list);
    
    loadingPanelProgress = new ProgressDialog(this);
    dh = new ControllerDataHelper(this);
    
    controllerListView = (ListView) findViewById(R.id.controller_list_view);
    appSettingsView = (LinearLayout) findViewById(R.id.appSettingsView);
    serversLayout = (SwipeRefreshLayout) findViewById(R.id.custom_servers_layout);
    panelSelectSpinnerView = (PanelSelectSpinnerView) findViewById(R.id.panel_select_spinner_view);
    progressLayout = (LinearLayout) findViewById(R.id.choose_controller_progress);

    serversLayout.setOnRefreshListener(new OnRefreshListener() {
      
      @Override
      public void onRefresh() {
        for (int i=0; i<serverListAdapter.getCount(); i++) {
          ControllerObject controller = serverListAdapter.getItem(i);
          if (!controller.isAvailabilityCheckInProgress()) {
            controller.setAvailabilityCheckDone(false);
          }
        }
        serverListAdapter.notifyDataSetChanged();
      }
    });
    
    addListChangeListener();
    
    addButtonOnClickListeners();
    
    initControllerList();
      
    startControllerAutoDiscovery();
  }

  public void onControllerLoadRequest(ControllerObject controller) {
  	if (controller != null && controller.isControllerUp()) {
  		// Set the controller as the current controller
      try {
        URL controllerURL = new URL(controller.getUrl());
        AppSettingsModel.setCurrentController(this.getApplicationContext(), controllerURL);
      } catch (MalformedURLException e) {
	    	ViewHelper.showAlertViewWithTitle(this, "Error", "Controller URL is not valid!");
	    	return;
      }
  		
  		// Load Default panel if none specified then go to panel selection screen
  		if (!TextUtils.isEmpty(controller.getDefaultPanel())) {
  			AppSettingsModel.setCurrentPanelIdentity(this, controller.getDefaultPanel());
  			Intent intent = new Intent(this, Main.class);
  			startActivity(intent);
  			//finish();
  		} else {
  			Intent intent = new Intent(this, PanelSelectionActivity.class);
  			startActivity(intent);
  			//finish();
  		}
  	} else {
	    	ViewHelper.showAlertViewWithTitle(this, "Warning", "Controller is not available!");
	    	return;
  	}
  }
  
  public void onControllerDeleteRequest(final ControllerObject controller) {
  	ViewHelper.showAlertViewWithTitleYesOrNo(this,
  			"Confirm Delete", "Are you sure you want to Delete controller '" + controller.getUrl() +"'?",
  			new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
							// Delete the controller from the list
	          ((ArrayAdapter<ControllerObject>) controllerListView.getAdapter()).remove(controller);
	          dh.deleteController(controller.getUrl());
					}
				});
  }
  
  public void onControllerAddRequest() {
    Intent intent = new Intent(AppSettingsActivity.this, AddEditControllerActivity.class);
    intent.setAction(getPackageName() + "." + Constants.ADD_CONTROLLER_ACTION);
    startActivityForResult(intent, Constants.ADD_CONTROLLER);
  }
  
  public void onControllerEditRequest(ControllerObject controller) {
  	editController = controller;
    Intent intent = new Intent(AppSettingsActivity.this, AddEditControllerActivity.class);
    intent.setAction(getPackageName() + "." + Constants.EDIT_CONTROLLER_ACTION);
    intent.putExtra(this.getPackageName() + ".ControllerUrl", controller.getUrl());
    startActivityForResult(intent, Constants.EDIT_CONTROLLER);    
  }

  private void addListChangeListener() {
    controllerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      	ControllerObject newlySelectedController =  (ControllerObject) parent.getItemAtPosition(position);
      	
      	if (newlySelectedController instanceof DummyControllerObject)
      	{
      		onControllerAddRequest();
      	}
//      	ControllerListItemLayout listItem = (ControllerListItemLayout)view;
//        
//      	// Only select controller if availability check has been completed and it is available
//      	if (newlySelectedController == null || !newlySelectedController.isAvailabilityCheckDone() || !newlySelectedController.isControllerUp())
//      		return;
//      	
//      	if (selectedController != null && selectedController == newlySelectedController)
//      		return;
//      	
//      	if (selectedController != null)
//      		selectedController.setIs_Selected(false);
//      	
//      	selectedController = newlySelectedController;
//
//      	if (newlySelectedController != null) {
//      		selectedController.setIs_Selected(true);
//      	}
//      	
//      	panelSelectSpinnerView.setController(newlySelectedController);  
//        serverListAdapter.notifyDataSetChanged();
      }
   });
  }
  
  /**
   * Adds the onclick listeners for the buttons in this view
   */
  private void addButtonOnClickListeners() {
    Button btnClearCache = (Button) findViewById(R.id.button_clear_image_cache);
    
    btnClearCache.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        ViewHelper.showAlertViewWithTitleYesOrNo(AppSettingsActivity.this, "",
            "Are you sure you want to clear image cache?",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                FileUtil.clearImagesInCache(AppSettingsActivity.this);
              }
            });
      }
    });
  }

	/**
	 * Populate the list of Controllers from saved controller's
	 */
  private void initControllerList() {
    ArrayList<ControllerObject> savedControllers = dh.getAllControllers();
    
	  serverListAdapter = new ControllerListAdapter(appSettingsView.getContext(), R.layout.controller_list_item, savedControllers);
    // Add Dummy to end of list
    serverListAdapter.add(new DummyControllerObject());

	  controllerListView.setAdapter(serverListAdapter);    
    controllerListView.setItemsCanFocus(true);
  }
  
  /**
   * It contains a list view to display custom servers,
   * "Add" button to add custom server, "Delete" button to delete custom server.
   * The custom servers would be saved in customServers.xml. If click a list item, it would be saved as current server.
   *
   * @return the linear layout
   */
//  private void () {
//    ArrayList<ControllerObject> customServers = new ArrayList<ControllerObject>();
//   
//    
//	  checkedControllers = new SavedServersNetworkCheckTestAsyncTask(getApplicationContext()){
//			
//		 @Override
//			  protected void onPreExecute() {
//				  if (progressLayout != null) {
//					  progressLayout.setVisibility(View.VISIBLE);
//				  }
//			  }
//
//			  @Override
//			  protected void onPostExecute(ArrayList<ControllerObject> result) {
//				  
//			        if (progressLayout != null) {
//			            progressLayout.setVisibility(View.INVISIBLE);
//			          }
//
//
//				  Log.i("LOGGER", "Done...");
//
//				  if(result!=null){
//					// customServers=result.addAll(index, collection);
//					 // serverListAdapter.add(result.get(0));
//					  
//					//serverListAdapter.addAll(result); //2.1 update pphone doesnt have this method. tablet does
//					  
//					  for(int i=0;i<result.size();i++){
//					  serverListAdapter.add(result.get(i)); //this should work for 2.1 update
//					  }					   
//					  
////					  Toast toast= Toast.makeText(getApplicationContext(), "complete", Toast.LENGTH_LONG);
////					  toast.show();
//					  serverListAdapter.notifyDataSetChanged();
//					  
//				  }else{
//					  Toast.makeText(getApplicationContext(), "HttpResponse is null", Toast.LENGTH_LONG).show();
//				  }	
//				  
//				  //load the database first then autodiscover so duplicate entries wont try to add
//				//  startControllerAutoDiscovery();
//				  
//			  }
//		  };
//
//	  checkedControllers.execute();  //execute oes to doInBackground
//		  
//		
//
//
//    
//
//    
//   // serverListAdapter
//    
//    //gets the layout length, padding etc
//    
//
//    Log.e("AppSettingsActivity", "currentCustomServerIndex"+currentCustomServerIndex);
//
//    if (currentCustomServerIndex != -1) {
//
//    	customAndAutoListView.setItemChecked(currentCustomServerIndex, true);
//        currentServer = ((ControllerObject)customAndAutoListView.getItemAtPosition(currentCustomServerIndex)).getControllerName();
//     
//    }
//  }
  

  /**
   * Received custom server from AddServerActivity, add prefix "http://" before it.
   * Add the result in custom server list and set it as current server.
   *
   * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  	boolean wasEdit = (requestCode == Constants.EDIT_CONTROLLER);
  	ControllerObject newController = null;
  	  	
  	if (data == null)
  		return;
  	
  	String controllerUrl = data.getStringExtra(this.getPackageName() + ".ControllerUrl");
  	if (!TextUtils.isEmpty(controllerUrl)) {
  		newController = dh.getControllerByUrl(controllerUrl);
  	}
  	
  	if (newController == null) {
  		return;
  	}
  	
  	// Replace old Controller Object if edit performed
  	if (wasEdit) {
  		updateControllerInList(editController, newController);
  		editController = null;
  	} else {
  		addControllerToList(newController);
    }
  }
  
  
   /**
   * Starts a new {@link IPAutoDiscoveryServer} thread, which starts a server to receive
   * messages declaring the presence of controllers.  Once the server is started,
   * a message is sent via multicast over UDP to announce that such messages are desired.
   * 
   * The list of controllers discovered this way is cleared before the new discovery
   * thread begins.
   * 
   * If any controllers are found, the first one found is automatically selected and its
   * panel list is requested.
   */
  private void startControllerAutoDiscovery() {
    Log.i(TAG, "Start Controller Discovery");
    
    autoDiscoveryServer = new IPAutoDiscoveryServer() {
      @Override
      protected void onProgressUpdate(Void... values) {
        if (progressLayout != null) {
          progressLayout.setVisibility(View.VISIBLE);
        }
      }

      @Override
      protected void onPostExecute(List<String> result) {
        if (progressLayout != null) {
          progressLayout.setVisibility(View.INVISIBLE);
        }

        int length = result.size();
        
        // Add each new controller to the controller list
        for (int i = 0; i < length; i++) {

        	if (!dh.controllerExists(result.get(i))) {
        		ControllerObject controller = new ControllerObject(result.get(i), "", "", "", 1.0, 1.0);
        		dh.addController(controller);
        		addControllerToList(controller);
        	}
        }
      }
    };

    autoDiscoveryServer.execute((Void) null);
  }

  public void stopControllerAutoDiscovery() {
    if (autoDiscoveryServer != null) {
      autoDiscoveryServer.cancel(true);
    }
  }
   
  private void updateControllerInList(ControllerObject oldController, ControllerObject newController) {
  	if (oldController == null || newController == null)
  		return;
  	
		int pos = serverListAdapter.getPosition(editController);
		serverListAdapter.remove(editController);
		serverListAdapter.insert(newController, pos);
    serverListAdapter.notifyDataSetChanged();
    Log.i(TAG,"Controller Updated '" + newController.getUrl() + "'");
  }
  
  private void addControllerToList(ControllerObject controller) {
  	if (controller == null)
  		return;

    // Insert before last dummy record
 	  serverListAdapter.insert(controller, serverListAdapter.getCount() - 1);
    serverListAdapter.notifyDataSetChanged();
    Log.i(TAG,"New Controller Added '" + controller.getUrl() + "'");
  }

  public static class FetchControllerFailoverGroupUrls extends RoboAsyncTask<Void>
  {
    @Inject
    private ControllerService controllerService;

    @Inject
    private ORControllerServerSwitcher orControllerServerSwitcher;

    @Override
    public Void call() throws Exception
    {
      List<URL> servers = controllerService.getServers();//this does not work
      
      Log.e(TAG, "controllerService.getServers() " + servers);

      for (URL url : servers)
      {
        Log.e(TAG, "received failover group member URL " + url);

        
       // TODO: Re-enable group discovery
       //orControllerServerSwitcher.saveGroupMembersToDB(servers,AppSettingsActivity.currentServer );
        //i dont want to use files instead wnt to use database put the failover urls as separate rows.
        //So, url1's failover URLs will have url1 column as "FailoverFor" column. same as controller name
        //grup is not really needed maybe refactor that in subsequent code bases
        
      }

      return null;
    }
  }
}
