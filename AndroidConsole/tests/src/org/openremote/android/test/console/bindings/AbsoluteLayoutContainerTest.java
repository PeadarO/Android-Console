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

package org.openremote.android.test.console.bindings;

import junit.framework.TestCase;

import org.openremote.android.console.bindings.AbsoluteLayoutContainer;
import org.openremote.android.console.bindings.Web;
import org.openremote.android.test.TestUtils;
import org.w3c.dom.Node;

/**
 * Tests the {@link org.openremote.android.console.bindings.AbsoluteLayoutContainer} class.
 *
 * @author <a href="mailto:aball@osintegrators.com">Andrew Ball</a>
 */
public class AbsoluteLayoutContainerTest extends TestCase
{
  /**
   * Tests that we can construct an AbsoluteLayoutContainer object from XML which contains
   * a child &lt;web&gt; element.
   */
  public void testConstructorWithChildWebElement()
  {
    final int left = 12;
    final int top = 24;
    final int width = 400;
    final int height = 800;
    final int id = 12;
    final String url = "http://muppets.com/videofeed";
    final String username = "fozzy";
    final String password = "bear";
    final String xmlText = String.format("<absolute left='%d' top='%d' width='%d' height='%d'>" +
        "\n    <web id='%d' src='%s' username='%s' password='%s' />" +
        "\n</absolute>", left, top, width, height, id, url, username, password);

    Node parsedXml = TestUtils.parseXml(xmlText);
    AbsoluteLayoutContainer alc = new AbsoluteLayoutContainer(parsedXml);

    assertEquals(left, alc.getLeft());
    assertEquals(top, alc.getTop());
    assertEquals(width, alc.getWidth());
    assertEquals(height, alc.getHeight());

    Web web = (Web) alc.getComponent();

    assertEquals(web.getSrc().toString(), url);
    assertEquals(web.getComponentId(), id);
    assertEquals(web.getUsername(), username);
    assertEquals(web.getPassword(), password);
  }
}
