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

import java.net.URL;
import org.openremote.android.console.Constants;
import org.openremote.android.console.R;
import org.openremote.android.console.bindings.Sensor;
import org.openremote.android.console.bindings.Web;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.OREvent;
import org.openremote.android.console.model.OREventListener;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.PollingStatusParser;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * OpenRemote wrapper around the standard Android WebView widget
 *
 * May be associated with a sensor, which gives a URL to load as
 * its polling value.
 *
 * @author Andrew D. Ball <aball@osintegrators.com>
 */
public class ORWebView extends ComponentView implements SensoryDelegate
{
  public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "ORWebView";

  private android.webkit.WebView webView;

  /**
   * This gets set to what is sent to webView.loadUrl() before calling that
   * method.  Used to avoid reloading the same URL when getting the same
   * result on successive polls of the sensor.
   */
  private String currentUrl;

  /**
   * This is used to prevent clicking on links in the WebView from launching the system
   * browser as well as to respond to HTTP authentication requests (if a username
   * and password was specified in the Web binding object)
   */
  private class ORWebViewClient extends WebViewClient
  {
    public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "ORWebView.ORWebViewClient";

    /**
     * Make sure that links are handled by the associated WebView, rather than
     * the system browser.
     *
     * Adapted from http://developer.android.com/resources/tutorials/views/hello-webview.html
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
      view.loadUrl(url);
      return true;
    }

    /**
     * Deal with HTTP authentication requests, using the username and password
     * specified in panel.xml if present.
     *
     * A failed authentication will result in an error log message and a suitable
     * HTML error message page's being written to the WebView.
     *
     * If either a username or a password is not specified in panel.xml for this
     * web element, an error log message will be written and a suitable HTML
     * error page will be written to the WebView.
     *
     * Adapted from
     *     http://stackoverflow.com/questions/2585055/using-webview-sethttpauthusernamepassword
     *
     * If authentication has failed for the given HttpAuthHandler, this will not try to
     * authenticate again.  Otherwise, this method would be called over and over
     * again in the case of incorrect credentials.
     *
     * This allows us to authenticate without knowing the realm ahead of time.
     **/
    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler,
        String host, String realm)
    {
      Log.d(LOG_CATEGORY, "onRecievedHttpAuthRequest(): entry");
      if (!handler.useHttpAuthUsernamePassword())
      {
        Log.e(LOG_CATEGORY, "onReceivedHttpAuthRequest(): HTTP authentication failed, " +
            "not trying again.");
        handler.cancel();
        String errorMessage = context.getString(R.string.http_auth_failed);
        webView.loadData(composeHtmlErrorPage(errorMessage), "text/html", "utf-8");
        return;
      }

      Web web = (Web) getComponent();
      String username = web.getUsername();
      String password = web.getPassword();
      if (username != null && password != null)
      {
        Log.i(LOG_CATEGORY, "onReceivedHttpAuthRequest(): supplying username and password for HTTP authentication");
        handler.proceed(username, password);
      }
      else
      {
        Log.e(LOG_CATEGORY, "received HTTP authentication request but did not have both username" +
            " and password from web element with id " + web.getComponentId());
        handler.cancel();
        String errorMessage =
            context.getString(R.string.web_element_missing_username_or_password) + " " +
            web.getComponentId();
        webView.loadData(composeHtmlErrorPage(errorMessage), "text/html", "utf-8");
      }
    }
    
    @SuppressLint("NewApi")
    @Override
    public void onPageFinished(WebView view, String url)
    {
      webView.setBackgroundColor(0x00000000);
        if (Build.VERSION.SDK_INT >= 11) {
          webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }
    }
  }

  /**
   * Write a very simple HTML page with the title and main content taken from
   * an error message.
   *
   * @param errorMessage the error message to write the page around
   * @return a String containing the HTML markup of the error page
   */
  private static String composeHtmlErrorPage(String errorMessage)
  {
    return String.format("<html>" +
        "\n<head>" +
        "\n<title>%s</title>" +
        "\n</head>" +
        "\n\n<body>" +
        "\n    <p><b>%s</b></p>" +
        "\n</body>" +
        "\n</html>", errorMessage, errorMessage);
  }

  /**
   * Initialize the standard Android WebView here and add it as a child of this Layout (this
   * is a subclass of LinearLayout).
   *
   * JavaScript is enabled on the WebView.
   *
   * Additionally, the WebView is configured to handle links, so that the system web
   * browser isn't launched when a user clicks on one.
   *
   * @param context the Android context to use for this widget
   * @param web the binding object with information about the Web element from XML
   */
  @SuppressLint("NewApi")
  public ORWebView(Context context, Web web)
  {
    super(context);
    setComponent(web);
    if (web != null)
    {
      // initialize the standard view here
      webView = new WebView(context);
      webView.setWebViewClient(new ORWebViewClient());
      ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      webView.setLayoutParams(layoutParams);
      webView.getSettings().setJavaScriptEnabled(true);
      webView.getSettings().setDomStorageEnabled(true);
      webView.setInitialScale(100);

      webView.setBackgroundColor(0x00000000);
      if (Build.VERSION.SDK_INT >= 11) {
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
      }

      URL url = web.getSrc();
      if (url != null)
      {
        currentUrl = url.toString();
        webView.loadUrl(url.toString());
      }
      else
      {
        webView.loadData(composeHtmlErrorPage(
            context.getResources().getString(R.string.web_element_invalid_or_missing_src_url) +
            " " + web.getComponentId()), "text/html", "utf-8");
      }
      
      //webView.setBackgroundColor(0x00000000);
      //webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
      
      addView(webView);

      if (web.getSensor() != null)
      {
        addPollingSensoryListener();
      }
    }
  }

  @Override
  public void addPollingSensoryListener()
  {
    final Sensor sensor = ((Web) getComponent()).getSensor();
    final Integer sensorId = sensor.getSensorId();
    if (sensorId > 0)
    {
      ORListenerManager.getInstance().addOREventListener(
          ListenerConstant.ListenerPollingStatusIdFormat + sensorId, new OREventListener()
          {
            public void handleEvent(OREvent event)
            {
              String newUrl = PollingStatusParser.statusMap.get(sensorId.toString());
              if (currentUrl != newUrl)
              {
                currentUrl = newUrl;
              }
              // inform the UI thread that we have a new URL to load
              handler.sendEmptyMessage(0);
            }
          });
    }
  }

  /**
   * Android message handler, used to inform the UI thread that the
   * web view needs to load a different URL.
   */
  private Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
        if (currentUrl != null) {
           webView.loadUrl(currentUrl);
        }
        super.handleMessage(msg);
     }
  };
}
