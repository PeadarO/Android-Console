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
package org.openremote.android.test;

import android.test.ActivityInstrumentationTestCase2;
import android.app.Activity;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.view.KeyEvent;
import android.view.View;
import android.content.Context;
import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.R;
import org.openremote.android.console.model.AppSettingsModel;
import junit.framework.Assert;

/**
 * UI tests for application settings.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class AppSettingsActivityTest extends ActivityInstrumentationTestCase2<AppSettingsActivity>
{

  /**
   * Global Android app context.
   */
  private Context ctx;

  /**
   * SSL Port editable text field UI widget.
   */
  private EditText sslEditField;

  /**
   * SSL Toggle (on/off)
   */
  private ToggleButton sslToggle;

  /**
   * Reference to the current activity being tested.
   */
  private Activity activity;


  // Constructors ---------------------------------------------------------------------------------

  public AppSettingsActivityTest()
  {
    super("org.openremote.android.console", AppSettingsActivity.class);
  }


  // Test Setup -----------------------------------------------------------------------------------

  @Override protected void setUp()
  {
    activity = this.getActivity();
    ctx = getInstrumentation().getTargetContext();

    View sslPortView = activity.findViewById(R.id.ssl_port);
    View sslToggleView = activity.findViewById(R.id.ssl_toggle);

    if (!(sslPortView instanceof EditText))
    {
      fail(
          "\n\n*************************************\n" +
          "SSL Port view has changed from assumed EditText type.\n" +
          "Update the test suite accordingly." +
          "\n*************************************\n\n"
      );
    }

    if (!(sslToggleView instanceof ToggleButton))
    {
      fail(
          "\n\n*************************************\n" +
          "SSL Toggle view has changed from assumed ToggleButton type. \n" +
          "Update the test suite accordingly." +
          "\n*************************************\n\n"
      );
    }

    sslEditField = (EditText)sslPortView;
    sslToggle = (ToggleButton)sslToggleView;
  }



  // SSL Port Setting Tests -----------------------------------------------------------------------



  /**
   * Tests SSL valid port values through R.id.ssl_port EditField UI widget.
   */
  public void testValidSSLPorts()
  {

    toggleSSL(true);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("1234");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();
    
    int sslPort = AppSettingsModel.getSSLPort(activity);

    Assert.assertTrue("Expected SSL port value 1234, got " + sslPort, sslPort == 1234);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("8443");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );

    getInstrumentation().waitForIdleSync();
    
    Assert.assertTrue(AppSettingsModel.getSSLPort(ctx) == 8443);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("443");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();
    
    Assert.assertTrue(AppSettingsModel.getSSLPort(ctx) == 443);
  }

  /**
   * Test SSL port value limits through R.id.ssl_port EditField UI widget.
   */
  public void testSSLPortLimits()
  {
    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("0");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();
    
    Assert.assertTrue(AppSettingsModel.getSSLPort(ctx) == 0);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("65535");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();

    Assert.assertTrue(AppSettingsModel.getSSLPort(ctx) == 65535);
  }

  /**
   * Test SSL port values out of bounds -- incorrect user input should not allow settings
   * to change.
   */
  public void testOutOfBoundsSSLPorts()
  {
    AppSettingsModel.setSSLPort(ctx, 8443);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("100000");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();

    assertTrue(AppSettingsModel.getSSLPort(ctx) == 8443);
  }


  /**
   * Test SSL port values with invalid input -- should allow numbers only
   */
  public void testInvalidInputOnSSLPortField()
  {
    AppSettingsModel.setSSLPort(ctx, 8443);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("abc");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );

    getInstrumentation().waitForIdleSync();
    
    assertTrue(AppSettingsModel.getSSLPort(ctx) == 8443);
  }



  // SSL toggle and SSL port mutual exclusion tests -----------------------------------------------


  /**
   * Test the SSL toggle and SSL port edit field mutual exclusion and inclusion -- when SSL is
   * on/off, the port edit field should be enabled/disabled
   */
  public void testSSLTogglePortMutualExclusionAndInclusion()
  {
    toggleSSL(true);

    // check the toggle is visible, checked, enabled and focusable..

    assertTrue(sslToggle.getVisibility() == View.VISIBLE);

    assertTrue(sslToggle.isChecked());
    assertTrue(sslToggle.isClickable());
    assertTrue(sslToggle.isEnabled());
    assertTrue(sslToggle.isFocusable());

    // check the port edit field is visible, enabled, focusable in all modes...
    
    assertTrue(sslEditField.getVisibility() == View.VISIBLE);
    assertTrue(sslEditField.isEnabled());
    assertTrue(sslEditField.isFocusable());
    assertTrue(sslEditField.isFocusableInTouchMode());


    // click toggle...

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslToggle.performClick();
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();

    // check the toggle is unchecked, but still visible, enabled and focusable..

    assertTrue(sslToggle.getVisibility() == View.VISIBLE);

    assertTrue(!sslToggle.isChecked());
    assertTrue(sslToggle.isClickable());
    assertTrue(sslToggle.isEnabled());
    assertTrue(sslToggle.isFocusable());


    // check the port edit field is visible, disabled, and not focusable in all modes...

    assertTrue(sslEditField.getVisibility() == View.VISIBLE);
    assertTrue(!sslEditField.isEnabled());
    assertTrue(!sslEditField.isFocusable());
    assertTrue(!sslEditField.isFocusableInTouchMode());
    
  }


  /**
   * Tests the SSL Port widget text value is rolled back to previous valid value if new
   * incorrect value was entered.
   */
  public void testIncorrectValueUIRollback()
  {
    toggleSSL(true);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("5000");
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();
    
    assertTrue(AppSettingsModel.getSSLPort(ctx) == 5000);

    activity.runOnUiThread(
      new Runnable() {
        public void run() {
          sslEditField.setText("100000");
          sslEditField.requestFocus();
          sslEditField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        }
      }
    );
    
    getInstrumentation().waitForIdleSync();

    assertTrue(AppSettingsModel.getSSLPort(ctx) == 5000);

    assertTrue(sslEditField.getText().toString().equals("5000"));

  }



  // Private Helpers ------------------------------------------------------------------------------

  /**
   * Toggle SSL ToggleButton in UI thread.
   *
   * @param toggle  SSL true or false
   */
  private void toggleSSL(final boolean toggle)
  {
    activity.runOnUiThread(
        new Runnable()
        {
          public void run()
          {
            sslToggle.setChecked(toggle);
          }
        }
    );

    getInstrumentation().waitForIdleSync();
  }

}

