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

package org.openremote.android.console.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openremote.android.console.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Retrieves a list of panels from a controller asynchronously.
 *
 * Implement onPostExecute() to do something with the results in subclasses.
 */
public class AsyncPanelListReader extends AsyncTask<InputStream, Void, List<String>>
{
  public static final String TAG = Constants.LOG_CATEGORY + "AsyncPanelListReader";

  protected List<String> doInBackground(InputStream... data)
  {
    ArrayList<String> panelList = new ArrayList<String>();
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document dom = builder.parse(data[0]);

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
    return panelList;
  }
}