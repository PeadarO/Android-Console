/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
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

package org.openremote.android.test.console.view;

import org.openremote.android.console.bindings.Web;
import org.openremote.android.console.view.ORWebView;
import org.openremote.android.test.TestUtils;
import org.w3c.dom.Node;

import android.view.ViewGroup;
import android.webkit.WebView;
import android.test.AndroidTestCase;

/**
 * Tests the {@link org.openremote.android.console.view.ORWebView} class.
 *
 * @author <a href="mailto:aball@osintegrators.com">Andrew Ball</a>
 */
public class ORWebViewTest extends AndroidTestCase
{

  /** Tests constructing an ORWebView from a Web binding object. */
  public void testConstructor() throws NoSuchFieldException
  {
    final int id = 12;
    final String url = "http://google.com/";
    final String username = "fozzy";
    final String password = "bear";
    final String xmlText = "<web id='" + id + "' src='" + url + "' username='" + username +
        "' password='" + password + "' />";

    // The width and height of the Web binding object would typically be set by the
    // AbsoluteContainerView constructor.
    final int width = 200;
    final int height = 300;

    Web web = new Web(TestUtils.parseXml(xmlText));
    web.setFrameWidth(width);
    web.setFrameHeight(height);
    ORWebView orWebView = new ORWebView(getContext(), web);
    assertEquals(web, (Web) orWebView.getComponent());
    WebView webView = (WebView) orWebView.getChildAt(0);
    assertNotNull(webView);
    ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
    assertEquals(width, layoutParams.width);
    assertEquals(height, layoutParams.height);
    assertEquals(true, webView.getSettings().getJavaScriptEnabled());

    assertEquals(1, orWebView.getChildCount());
    assertEquals(webView, (WebView) orWebView.getChildAt(0));
  }

  /**
   * Make sure that nothing blows up when we initialize an ORWebView from a Web binding
   * object that has a sensor.
   *
   * TODO figure out how to do some more involved testing here -- simulating polling results,
   *      ensuring that the WebView.loadUrl() method isn't called unless the URL is
   *      different from the last one it was called with, etc.
   */
  public void testWithSensor()
  {
    final int id = 12;
    final int sensorId = 13;
    final String url = "http://muppets.com/videofeed";
    final String username = "fozzy";
    final String password = "bear";
    final String xmlText = "<web id='" + id + "' src='" + url + "' username='" + username + "' password='" + password + "' >" +
        "\n  <link type='sensor' ref='" + sensorId + "' />" +
        "\n</web>";

    Node parsedXml = TestUtils.parseXml(xmlText);
    Web web = new Web(parsedXml);
    ORWebView orWebView = new ORWebView(getContext(), web);
  }
}
