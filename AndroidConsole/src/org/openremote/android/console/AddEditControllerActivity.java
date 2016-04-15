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

import java.net.URI;
import java.net.URISyntaxException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.openremote.android.console.Constants;

/**
 * This is the edit controller screen for altering the Controller URL, default Panel Name
 * and default login credentials as well as viewing Fail over Group members
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class AddEditControllerActivity extends GenericActivity {
    public static final String OPEN_REMOTE_PREFS = "openRemoteConfig";
    public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "AddEditControllerActivity";
    private static final String PASSWORD_MASK = "........";
    
    private ControllerDataHelper dh;
    private ControllerObject controller;
    private String controllerUrl;
    private String defaultPanel;
    private String username;
    private String userpass;
    private boolean editMode;
    private double xScale = 1.0;
    private double yScale = 1.0;
    
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        ///getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d(LOG_CATEGORY, "onCreate()");
        String action = getIntent().getAction();
        
        if (action != null && action.equalsIgnoreCase(getPackageName() + "." + Constants.EDIT_CONTROLLER_ACTION))
        	editMode = true;
        
        dh = new ControllerDataHelper(this);
        
        if (editMode)
        {
        	// Get controller object
        	controllerUrl = getIntent().getStringExtra(this.getPackageName() + ".ControllerUrl");
        	controller = dh.getControllerByUrl(controllerUrl);
        	
        	if (controller == null || TextUtils.isEmpty(controller.getUrl()))
        	{
        		finish();
        		return;
        	}
        	
        	defaultPanel = controller.getDefaultPanel();
        	username = controller.getUsername();
        	userpass = controller.getUserPass();
        	userpass = TextUtils.isEmpty(userpass) ? "" : PASSWORD_MASK; // We don't store raw password 
        	xScale = controller.getXScale();
        	yScale = controller.getYScale();
        }
        
        setContentView(R.layout.add_edit_controller);        
     
        // Configure views
        TextView titleView = (TextView) findViewById(R.id.addEditControllerTitle);
        String title = editMode ? "Edit Controller" : "Add Controller";
        titleView.setText(title);
        
        final EditText controllerUrlText = (EditText) findViewById(R.id.controller_url_edit_text);
        final EditText controllerPanelText = (EditText) findViewById(R.id.controller_default_panel_edit_text);
        final EditText controllerUsernameText = (EditText) findViewById(R.id.controller_username_edit_text);
        final EditText controllerPasswordText = (EditText) findViewById(R.id.controller_password_edit_text);
        final EditText controllerXScaleText = (EditText) findViewById(R.id.controller_x_scale);
        final EditText controllerYScaleText = (EditText) findViewById(R.id.controller_y_scale);
        Button saveBtn = (Button) findViewById(R.id.edit_controller_save_button);
        Button cancelBtn = (Button) findViewById(R.id.edit_controller_cancel_button);
        
        controllerUrlText.setText(controllerUrl);
        controllerPanelText.setText(defaultPanel);
        controllerUsernameText.setText(username);
        controllerPasswordText.setText(userpass);
        controllerXScaleText.setText(Double.toString(xScale));
        controllerYScaleText.setText(Double.toString(yScale));
        
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            controllerUrl = controllerUrlText.getText().toString().trim();
	            defaultPanel = controllerPanelText.getText().toString().trim();
	            username = controllerUsernameText.getText().toString().trim();
	            userpass = controllerPasswordText.getText().toString().trim();
	            String xTemp = controllerXScaleText.getText().toString().trim();
	            String yTemp = controllerYScaleText.getText().toString().trim();
	            
	            try
	            {
	            	xScale = Double.parseDouble(xTemp);
	            	yScale = Double.parseDouble(yTemp);
	            }
	            catch(Exception e)
	            {
	          	  	Toast toast = Toast.makeText(AddEditControllerActivity.this, "Scale factor must be a numeric value between 0.1 and 5.0", 1);
	          	  	toast.show();
	          	  	xScale = 1.0;
	          	  	yScale = 1.0;
	            }	            
	            
	            if (TextUtils.isEmpty(controllerUrl)) {
	          	  Toast toast = Toast.makeText(AddEditControllerActivity.this, "You must specify a Controller URL.", 1);
	              toast.show();
	              return;
	            }
	            
	          	if (controllerUrl.indexOf("http") < 0)
	          		controllerUrl = "http://" + controllerUrl;
	            
	            if(dh.controllerExists(controllerUrl) && !editMode) {
	          	  Toast toast = Toast.makeText(AddEditControllerActivity.this, "Controller already exists.", 1);
	              toast.show();
	              return;
	            }
                
							try {
								new URI(controllerUrl);
							} catch (URISyntaxException e) {
								Toast toast = Toast.makeText(AddEditControllerActivity.this, "Invalid Controller URL!", 1);
								toast.show();
								return;
							}

							if (editMode &&
									controller.getUrl().equals(controllerUrl) &&
									controller.getDefaultPanel().equals(defaultPanel) &&
									controller.getUsername().equals(username) &&
									controller.getXScale().equals(xScale) &&
									controller.getYScale().equals(yScale) &&
									((controller.getUserPass().equals("") && userpass.equals("")) || (!controller.getUserPass().equals("") && !userpass.equals(PASSWORD_MASK)))
							) {
								// Nothing has changed so just finish
								finish();
								return;
							} else {
								
							}
							
							// If we get this far then update or create the specified controller
							ControllerObject newController = new ControllerObject(controllerUrl, defaultPanel, username, userpass, xScale, yScale);
							
							if (editMode) {
								// If controller url changed check new one doesn't already exist
								if (!controller.getUrl().equals(controllerUrl) && dh.controllerExists(controllerUrl)) {
									Toast toast = Toast.makeText(AddEditControllerActivity.this, "New Controller URL Already Exists!", 1);
									toast.show();
									return;
								}									
								dh.updateController(controller, newController);
							} else {
								dh.addController(newController);
							}
							
           	 	//dh.closeConnection();
							
              Intent intent = getIntent();
              intent.putExtra(getPackageName() + ".ControllerUrl", controllerUrl);
              int resultCode = editMode ? Constants.EDIT_CONTROLLER : Constants.ADD_CONTROLLER;
              setResult(resultCode, intent);
              finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//dh.closeConnection();
              finish();
            }            
        });        
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return handleMenu(item);
    }

    public boolean handleMenu(MenuItem item) {
        switch (item.getItemId()) {
        case Constants.MENU_ITEM_QUIT:
            System.exit(0);
            return true;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return true;
    }

    public void populateMenu(Menu menu) {
        menu.setQwertyMode(true);
        MenuItem quit = menu.add(0, Constants.MENU_ITEM_QUIT, 0, R.string.back);
        quit.setIcon(R.drawable.ic_menu_revert);
    }
}
