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

package org.openremote.android.test.console.net;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.io.IOException;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import org.apache.http.HttpResponse;
import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.net.ORNetworkCheck;

import com.google.inject.Inject;

/**
 * Tests for {@link org.openremote.android.console.net.ORNetworkCheck} class.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author handy 2010-04-28
 *
 */
public class ORNetworkCheckTest extends ActivityInstrumentationTestCase2<AppSettingsActivity>
{

  // TODO :
  //   these tests are integration tests requiring a WiFi connection to a public controller
  //   instance at controller.openremote.org/test -- they should be separated from unit tests.
  //
  //                                                                          [JPL]


  // Instance Fields ------------------------------------------------------------------------------

  /**
   * Android application context shared between tests.
   */
  private Context ctx;

  /**
   * Reference to Android wifi manager shared between tests.
   */
  private WifiManager wifi;

  @Inject
  private ORNetworkCheck orNetworkCheck;



  // Constructors ---------------------------------------------------------------------------------

  public ORNetworkCheckTest()
  {
    super("org.openremote.android.console", AppSettingsActivity.class);
  }



  // Test Set Up ----------------------------------------------------------------------------------

  public void setUp()
  {
    this.ctx = getInstrumentation().getTargetContext();

    wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);

    enableWifi();

    AppSettingsModel.setCurrentPanelIdentity(ctx, null);
  }

//
//  public void tearDown()
//  {
//    //AppSettingsModel.setCurrentPanelIdentity(ctx, null);
//  }


  // Tests ----------------------------------------------------------------------------------------


  
  /**
   * Connect to controller.openremote.org/test/controller and attempt to verify the existence
   * of "SimpleName" panel design.
   *
   * @throws IOException  if connecting to remote controller fails for every reason
   */
  public void testVerifyControllerURL() throws IOException
  {
    URL publicControllerUrl = new URL("http://controller.openremote.org/test/controller");

    try
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, "SimpleName");

      if (!wifi.isWifiEnabled())
        fail(wifiRequired());

      HttpResponse response = orNetworkCheck.verifyControllerURL(publicControllerUrl);

      assertNotNull("Got null HTTP response, was expecting: " + HttpURLConnection.HTTP_OK, response);

      int status = response.getStatusLine().getStatusCode();

      assertTrue("Was expecting HTTP_OK, got " + status, status == HttpURLConnection.HTTP_OK);
    }
    finally
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, null);
    }
  }



  /**
   * Connect to controller.openremote.org/test/controller instance and attempt to verify a panel
   * design ID that does not exist.
   *
   * @throws IOException if connecting to the remote test controller fails for any reason
   */
  public void testVerifyControllerURLWrongPanelName() throws IOException
  {
    URL publicControllerUrl = new URL("http://controller.openremote.org/test/controller");

    try
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, "nothing");

      if (!wifi.isWifiEnabled())
        fail(wifiRequired());

      HttpResponse response = orNetworkCheck.verifyControllerURL(publicControllerUrl);

      assertNotNull("Was expecting error response 428, got null.", response);

      int status = response.getStatusLine().getStatusCode();

      assertTrue("Was expecting Error 428 response, got : " + status, status == 428);
    }
    finally
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, null);
    }
  }


  /**
   * Connect to controller.openremote.org/test/controller instance and attempt to verify a
   * panel design ID that has white space in the name.
   *
   * @throws IOException if remote test controller can't be accessed
   */
  public void testVerifyControllerURLSpacesInPanelName() throws IOException
  {
    URL publicControllerUrl = new URL("http://controller.openremote.org/test/controller");

    try
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, "Name With Spaces");

      //if (!wifi.isWifiEnabled())
      //  fail(wifiRequired());

      HttpResponse response = orNetworkCheck.verifyControllerURL(publicControllerUrl);

      assertNotNull("Got null response, was expecting " + HttpURLConnection.HTTP_OK, response);

      int status = response.getStatusLine().getStatusCode();

      assertTrue("Was expecting " + HttpURLConnection.HTTP_OK + " response, got : " + status,
                 status == HttpURLConnection.HTTP_OK);
    }
    finally
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, null);
    }
  }



  /**
   * Test behavior when controller URL has been set to a non-existent location.
   *
   * @throws IOException if something happens to the connection, invalid URL path should only
   *                     result an HTTP error code
   */
  public void testVerifyControllerWrongURL() throws IOException
  {
    URL publicControllerUrl = new URL("http://controller.openremote.org/nothing/here");

    try
    {
      //if (!wifi.isWifiEnabled())
      //  fail(wifiRequired());

      AppSettingsModel.setCurrentPanelIdentity(ctx, "something");

      HttpResponse response = orNetworkCheck.verifyControllerURL(publicControllerUrl);

      assertNotNull("Got null HTTP response, was expecting: " + HttpURLConnection.HTTP_NOT_FOUND,
                    response);

      int status = response.getStatusLine().getStatusCode();

      assertTrue("Was expecting 404, got " + status,
                 status == HttpURLConnection.HTTP_NOT_FOUND);
    }
    finally
    {
      AppSettingsModel.setCurrentPanelIdentity(ctx, null);
    }
  }



  /**
   * Tests behavior with a controller URL containing a hostname that doesn't resolve.
   */
  public void testControllerAtUnknownHost() throws MalformedURLException
  {
    URL controllerUrlWithUnknownHost =
        new URL("http://controller.openremotetest.org/test/controller");

    try
    {
      orNetworkCheck.verifyControllerURL(controllerUrlWithUnknownHost);

      fail ("Should not get here...");
    }
    catch (IOException e)
    {
      // expected

      assertTrue(e instanceof UnknownHostException);
    }
  }


//
// Commented the following test out due to despite turning off wifi, actual devices tend to
// automatically switch to a backup connection over 3G and still reach target (goal was to
// test behavior when URL cannot be reached). There was no obvious API in connectivity manager
// to switch off 3G connections -- it may exist elsewhere.
//                                                                                    [JPL]
//  /**
//   * TODO
//   */
//  public void testVerifyControllerNoWifi()
//  {
//
//    WifiManager wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
//
//    disableWifi();
//
//    if (wifi.isWifiEnabled())
//      fail(noWifiWanted());
//
//
//    HttpResponse response = orNetworkCheck.verifyControllerURL(
//        "http://controller.openremote.org/test/controller"
//    );
//
//    int status = response.getStatusLine().getStatusCode();
//
//    assertNull("Was expecting null, got " + status, response);
//
//  }





  // Test helper methods --------------------------------------------------------------------------


  private void enableWifi() 
  {
    enableWifi(true);
  }

  private void disableWifi()
  {
    enableWifi(false);
  }

  private void enableWifi(boolean enable)
  {

    WifiManager wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);

    if (enable && wifi.isWifiEnabled())
      return;

    if (!enable && !wifi.isWifiEnabled())
      return;

    if (enable && !wifi.isWifiEnabled())
    {
      if (!wifi.setWifiEnabled(true))
        fail("Cannot enable WiFi");
    }
    else
    {
      wifi.disconnect();
      wifi.setWifiEnabled(false);
    }

    // wait for it...

    for (int iterations = 0; iterations < 10; iterations++)
    {
      SystemClock.sleep(500);

      if (enable && wifi.isWifiEnabled())
        break;
      if (!enable && !wifi.isWifiEnabled())
        break;
    }
  }


  private String wifiRequired()
  {
    return
        "\n\n******************************\n\n" +
        " This test assumes availability of WiFi network.\n" +
        " If you're running tests in the emulator, WiFi may not be available.\n" +
        " If you're running tests on a device, you may need to turn WiFi on.\n\n" +
        "******************************\n";
  }

  private String noWifiWanted()
  {
    return
        "\n\n******************************\n\n" +
        " Testing for behavior when WiFi is not enabled.\n" +
        " For some reason, could not disable the WiFi connection.\n\n" +
        "******************************\n";
  }


}
