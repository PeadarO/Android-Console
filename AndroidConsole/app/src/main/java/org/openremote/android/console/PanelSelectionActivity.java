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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpResponse;
import org.openremote.android.console.LoginDialog.OnloginClickListener;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.exceptions.ORConnectionException;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ControllerException;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.net.ControllerService;
import org.openremote.android.console.net.IPAutoDiscoveryServer;
import org.openremote.android.console.net.ORConnection;
import org.openremote.android.console.net.ORConnectionDelegate;
import org.openremote.android.console.net.ORControllerServerSwitcher;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.net.ORNetworkCheck;
import org.openremote.android.console.net.SavedServersNetworkCheckTestAsyncTask;
import org.openremote.android.console.util.FileUtil;
import org.openremote.android.console.util.StringUtil;
import org.openremote.android.console.view.ControllerListItemLayout;
import org.openremote.android.console.view.PanelSelectSpinnerView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.google.inject.Inject;
import roboguice.util.RoboAsyncTask;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Panel Selection List View for displaying a list of panel's
 * and selecting which panel to load.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */

public class PanelSelectionActivity extends GenericActivity implements ORConnectionDelegate {
  public static final String TAG = Constants.LOG_CATEGORY + "PanelSelectionActivity";
  
  private ControllerDataHelper dh;

  private ListView panelListView;
  
  private ArrayAdapter<String> panelListAdapter;
  
  @Override
  public void onCreate(Bundle savedInstanceState) { 
    super.onCreate(savedInstanceState);

    setContentView(R.layout.panel_list);
  	
    panelListView = (ListView) findViewById(R.id.panel_list_view);

    addListChangeListener();
    
    initPanelList();
  }

  public void onPanelLoadRequest(String panelName) {
  	if (!TextUtils.isEmpty(panelName)) {
  		AppSettingsModel.setCurrentPanelIdentity(PanelSelectionActivity.this, panelName);
			Intent intent = new Intent();
			intent.setClass(this, Main.class);
			startActivity(intent);
			finish();
		}
  }
  	
  private void addListChangeListener() {
    panelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      	String panelName =  (String) parent.getItemAtPosition(position);
      	PanelSelectionActivity.this.onPanelLoadRequest(panelName);
      }
   });
  }

	/**
	 * Populate the list of Panels from current controller
	 */
  private void initPanelList() {
  	ControllerObject controller = AppSettingsModel.getCurrentController(this);
  	final String controllerUrl = controller.getUrl();
  	
 	 	if (!TextUtils.isEmpty(controllerUrl)) {
	  	// Get list of panels Asynchronously
	  	new ORConnection(ORHttpMethod.GET, true, controllerUrl + "/rest/panels", this, this);
	
	  	panelListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		  panelListView.setAdapter(panelListAdapter);
		  panelListView.setItemsCanFocus(true);
 	 	}
  }

	@Override
	public void urlConnectionDidReceiveData(InputSource data) {
    ArrayList<String> panelList = new ArrayList<String>();
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document dom = builder.parse(data);

      Element root = dom.getDocumentElement();

      NodeList nodeList = root.getElementsByTagName("panel");

      int nodeNums = nodeList.getLength();
      for (int i = 0; i < nodeNums; i++)
      {
      	panelList.add(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
      }
    }

    catch (IOException e)
    {
      Log.e(TAG, "IOException while requesting panel list", e);
    }

    catch (ParserConfigurationException e) {
      Log.e(TAG, "XML parser configuration error while requesting panel list", e);
    }

    catch (SAXException e) {
      Log.e(TAG, "parse error on panel list from controller", e);
    }

    Log.i(TAG, "received the following panel names from the controller: " + panelList.toString());

    // Add these panels to the list
    panelListAdapter.clear();
    
    for (int i = 0; i < panelList.size(); i++) {   
       panelListAdapter.add(panelList.get(i));
    }
    
    panelListAdapter.notifyDataSetChanged();
	}
   
   @Override
   public void urlConnectionDidFailWithException(Exception e) {
	   AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	   alertDialog.setTitle("Panel List Error");
	   alertDialog.setMessage(ControllerException.exceptionMessageOfCode(ControllerException.REQUEST_ERROR));
	   alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	      	dialog.dismiss();
	      }
	   });
	   alertDialog.show();
      
	   Log.e(TAG, "Can not get panel identity list", e);
   }

   @Override
   //This method is for setting the error dialog in the spinner
   public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      Log.i(TAG, statusCode+"");
      if (statusCode != Constants.HTTP_SUCCESS) {
         if (statusCode == ControllerException.UNAUTHORIZED) {
            LoginDialog loginDialog = new LoginDialog(this);
            	
            loginDialog.setOnClickListener(loginDialog.new OnloginClickListener() {
               @Override
               public void onClick(View v) {
                  super.onClick(v);
                	ControllerObject controller = AppSettingsModel.getCurrentController(PanelSelectionActivity.this);
                	String controllerUrl = controller.getUrl();
                  new ORConnection(ORHttpMethod.GET, true, controllerUrl + "/rest/panels", PanelSelectionActivity.this, PanelSelectionActivity.this);
               }
               
            });
         } else {
        	 urlConnectionDidFailWithException(new Exception("HTTP Error: " + httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase()));
         }
      }
   }
}
