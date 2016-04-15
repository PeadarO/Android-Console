package org.openremote.android.console;

import java.net.URI;
import java.net.URISyntaxException;

import org.openremote.android.console.model.AppSettingsModel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ControllerListActivity extends GenericActivity {
    public static final String OPEN_REMOTE_PREFS = "openRemoteConfig";

    public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "ControllerListActivity";
    
public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    Log.d(LOG_CATEGORY, "onCreate()");
    
    setContentView(R.layout.ssl_field_view);

 
    
    initSSLState();
    
}

  /**
   * Initializes the SSL related UI widget properties and event handlers to deal with user
   * interactions.
   */
  private void initSSLState()
  {
    // Get UI Widget references...

    final ToggleButton sslToggleButton = (ToggleButton)findViewById(R.id.ssl_toggle);
    final EditText sslPortEditField = (EditText)findViewById(R.id.ssl_port);

    // Configure UI to current settings state...

    boolean sslEnabled = AppSettingsModel.isSSLEnabled(this);

    sslToggleButton.setChecked(sslEnabled);
    sslPortEditField.setText("" + AppSettingsModel.getSSLPort(this));

    // If SSL is off, disable the port edit field by default...

    if (!sslEnabled)
    {
      sslPortEditField.setEnabled(false);
      sslPortEditField.setFocusable(false);
      sslPortEditField.setFocusableInTouchMode(false);
    }

    // Manage state changes to SSL toggle...

    sslToggleButton.setOnCheckedChangeListener(
        new OnCheckedChangeListener()
        {
          public void onCheckedChanged(CompoundButton buttonView, boolean isEnabled)
          {

            // If SSL is being disabled, and the user had soft keyboard open, close it...

            if (!isEnabled)
            {
              InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
              input.hideSoftInputFromWindow(sslPortEditField.getWindowToken(), 0);
            }

            // Set SSL state in config model accordingly...

            AppSettingsModel.enableSSL(ControllerListActivity.this, isEnabled);

            // Enable/Disable SSL Port text field according to SSL toggle on/off state...

            sslPortEditField.setEnabled(isEnabled);
            sslPortEditField.setFocusable(isEnabled);
            sslPortEditField.setFocusableInTouchMode(isEnabled);
          }
        }
    );


    // ...


    sslPortEditField.setOnKeyListener(new OnKeyListener()
    {
      public boolean onKey(View v, int keyCode, KeyEvent event)
      {
        if (keyCode == KeyEvent.KEYCODE_ENTER)
        {
          String sslPortStr = ((EditText)v).getText().toString();

          try
          {
            int sslPort = Integer.parseInt(sslPortStr.trim());
            AppSettingsModel.setSSLPort(ControllerListActivity.this, sslPort);
          }

          catch (NumberFormatException ex)
          {
            Toast toast = Toast.makeText(getApplicationContext(), "SSL port format is not correct.", 1);
            toast.show();

            return false;
          }

          catch (IllegalArgumentException e)
          {
            Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), 2);
            toast.show();

            sslPortEditField.setText("" + AppSettingsModel.getSSLPort(ControllerListActivity.this));
             
            return false;
          }
        }

        return false;
      }

    });
    
  }
}