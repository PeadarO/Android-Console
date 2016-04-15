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
package org.openremote.android.console.bindings;

import java.net.MalformedURLException;
import java.net.URL;

import org.openremote.android.console.Constants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

/**
 * Represents a web element from a panel definition, which represents an area of the screen where a
 * web page will be rendered.
 */
@SuppressWarnings("serial")
public class Web extends SensorComponent
{
  public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "bindings/Web";
  /** the URL to view */
  private URL src;
  /** optional username to user for HTTP basic authentication */
  private String username;
  /** optional password to use for HTTP basic authentication */
  private String password;
  /**
   * whether to ignore any SSL errors (such as self-signed certificates or expired certificates)
   *
   * This corresponds to an optional attribute of the same name in the XML used to construct an
   * instance of this class, using the xsd:boolean type.
   */
  private boolean ignoreSslErrors;

  /**
   * Constructs a new Web object from an XML Node
   *
   * @param node node representing the "web" element
   */
  public Web(Node node)
  {
    NamedNodeMap attributes = node.getAttributes();
    setComponentId(Integer.valueOf(attributes.getNamedItem(ID).getNodeValue()));

    Node usernameNode = attributes.getNamedItem(USERNAME);
    if (usernameNode != null)
    {
      username = usernameNode.getNodeValue();
    }

    Node passwordNode = attributes.getNamedItem(PASSWORD);
    if (passwordNode != null)
    {
      password = passwordNode.getNodeValue();
    }

    Node srcAttribute = attributes.getNamedItem(SRC);
    if (srcAttribute != null)
    {
      String urlText = srcAttribute.getNodeValue();
      try
      {
        this.src = new URL(urlText);
      }
      catch (MalformedURLException e)
      {
        Log.e(LOG_CATEGORY, "invalid URL for web element with id " + getComponentId() + ": " +
            urlText);
      }
    }

    Node ignoreSslErrorsAttribute = attributes.getNamedItem(IGNORE_SSL_ERRORS);
    if (ignoreSslErrorsAttribute != null)
    {
      String ignoreSslErrorsValue = ignoreSslErrorsAttribute.getNodeValue();
      if (ignoreSslErrorsValue.equals("true") || ignoreSslErrorsValue.equals("1"))
      {
        ignoreSslErrors = true;
      }
      else if (ignoreSslErrorsValue.equals("false") || ignoreSslErrorsValue.equals("0"))
      {
        ignoreSslErrors = false;
      }
      else
      {
        Log.e(LOG_CATEGORY, "invalid ignoreSslErrors value for xsd:boolean, defaulting to false " +
            "for web element with id " + getComponentId());
        ignoreSslErrors = false;
      }
    }

    // We should have zero or one <link> elements pointing to a sensor which
    // will supply updated URLs
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++)
    {
      parser(childNodes.item(i));
    }
  }

  /** Returns the source URL */
  public URL getSrc()
  {
    return src;
  }

  /** Returns the username specified for HTTP basic authentication (or null if none set) */
  public String getUsername()
  {
    return username;
  }

  /** Returns the password specified for HTTP basic authentication (or null if none set) */
  public String getPassword()
  {
    return password;
  }

  /** Returns whether SSL errors should be ignored when displaying content */
  public boolean getIgnoreSslErrors()
  {
    return ignoreSslErrors;
  }
}
