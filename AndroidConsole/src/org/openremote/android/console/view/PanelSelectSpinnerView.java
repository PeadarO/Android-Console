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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpResponse;
import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.Constants;
import org.openremote.android.console.ControllerObject;
import org.openremote.android.console.LoginDialog;
import org.openremote.android.console.R;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ControllerException;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.net.ORConnection;
import org.openremote.android.console.net.ORConnectionDelegate;
import org.openremote.android.console.net.ORHttpMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * The PanelSelectSpinnerView is used to select the Panel to load for the current Controller
 */
public class PanelSelectSpinnerView extends Spinner implements ORConnectionDelegate {
	public static final String TAG = Constants.LOG_CATEGORY + "PanelSelectSpinnerView";
	 private ArrayAdapter<String> arrayAdapter;
	 public static final String  NO_CONTROLLER = "Select Controller";
	 public static final String  SELECT_PANEL = "Select Panel";
	 public static final String  NO_PANEL = "No Panel";
	 private ControllerObject selectedController;
   
   public PanelSelectSpinnerView(Context context) {
      super(context);
      setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT));
      initView(context);
   }
   
   /** Constructor used when inflating from XML */
   public PanelSelectSpinnerView(Context context, AttributeSet attrs) {
     super(context, attrs);
     
     initView(context);
   }
   
   private void initView(final Context context) {
     arrayAdapter = new ArrayAdapter<String>(context, R.layout.simple_spinner_item_center);
     arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     setAdapter(arrayAdapter);
     
     setController(null);
     
     setOnTouchListener(new OnTouchListener() {
    	 
       public boolean onTouch(View v, MotionEvent event) {

      	 if (selectedController != null && selectedController.isControllerUp())
      		 new ORConnection(ORHttpMethod.GET, true, selectedController.getUrl() + "/rest/panels", PanelSelectSpinnerView.this, PanelSelectSpinnerView.this.getContext());
      	 
         return true;
       }
       
    });
   }
   
   public void setController(ControllerObject controller) {
  	 selectedController = controller;
  	 
  	 if (arrayAdapter == null)
  		 return;
  	 
  	 setEnabled(false);  	 
  	 arrayAdapter.clear();
  	 
  	 if (controller == null || !controller.isControllerUp()) {
  		 arrayAdapter.add(NO_CONTROLLER);
  	 } else {
  		 arrayAdapter.add(SELECT_PANEL);
  		 setEnabled(true);
  	 }
  	 
  	 arrayAdapter.notifyDataSetChanged();
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
    arrayAdapter.clear();
    
    if (panelList.size() == 0) {
      arrayAdapter.add(NO_PANEL);
    }
    
    for (int i = 0; i < panelList.size(); i++) {   
       arrayAdapter.add(panelList.get(i));
    }
    
    arrayAdapter.notifyDataSetChanged();
    PanelSelectSpinnerView.this.setSelection(0);
    
//    String panel = AppSettingsModel.getCurrentPanelIdentity(getContext());
//    int panelCount = arrayAdapter.getCount();
//    if (!TextUtils.isEmpty(panel)) {
//       if (panelCount == 0) {
//          arrayAdapter.add(panel);
//       } else {
//          for (int i = 0; i < panelCount; i++) {
//             if (panel.equals(arrayAdapter.getItem(i))) {
//                PanelSelectSpinnerView.this.setSelection(i, true);
//                break;
//             }
//          }
//       }
//    }
	}
   
   @Override
   public void urlConnectionDidFailWithException(Exception e) {
	   AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
	   alertDialog.setTitle("Panel List Error");
	   alertDialog.setMessage(ControllerException.exceptionMessageOfCode(ControllerException.REQUEST_ERROR));
	   alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	      	dialog.dismiss();
	      }
	   });
	   alertDialog.show();
      
	   Log.e("OpenRemote-PANEL LIST", "Can not get panel identity list", e);
   }

   @Override
   //This method is for setting the error dialog in the spinner
   public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      Log.e("PANEL", statusCode+"");
      if (statusCode != Constants.HTTP_SUCCESS) {
         if (statusCode == ControllerException.UNAUTHORIZED) {
            LoginDialog loginDialog = new LoginDialog(getContext());
            loginDialog.setOnClickListener(loginDialog.new OnloginClickListener() {
               @Override
               public void onClick(View v) {
                  super.onClick(v);
                  new ORConnection(ORHttpMethod.GET, true, selectedController.getUrl() + "/rest/panels", PanelSelectSpinnerView.this, PanelSelectSpinnerView.this.getContext());
               }
               
            });
         } else {
        	 urlConnectionDidFailWithException(new Exception("HTTP Error: " + httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase()));
         }
      }
   }
}
