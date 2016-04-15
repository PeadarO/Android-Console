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
package org.openremote.android.console.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;


/**
 * Polling status result XML parser.
 * 
 * Fix added for ANDROID-102 see JIRA
 * 
 * @author Tomsky Wang
 * @author Dan Cong
 * @author Rich Turner
 * 
 */
public class PollingStatusParser {
   public static final HashMap<String, String> statusMap = new HashMap<String, String>();
   
   /**
    * Parses the polling result.
    * 
    * @param inputStream the input stream
    */
   public static void parse(InputStream inputStream) {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document dom = builder.parse(inputStream);
         Element root = dom.getDocumentElement();
         
         NodeList nodeList = root.getElementsByTagName("status");
         int nodeNums = nodeList.getLength();
         for (int i = 0; i < nodeNums; i++) {
            String lastId = nodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String newStatus = "";
            Node newStatusNode = nodeList.item(i).getFirstChild();
            if (newStatusNode != null)
            	newStatus = newStatusNode.getNodeValue();
            statusMap.put(lastId, newStatus);
            Log.i("OpenRemote/POLLING", "set " + lastId + " to new status: " + newStatus);
            ORListenerManager.getInstance().notifyOREventListener(ListenerConstant.ListenerPollingStatusIdFormat + lastId, null);
         }
      } catch (ParserConfigurationException e) {
         Log.e("OpenRemote/POLLING", "failed", e);
      } catch (SAXException e) {
         Log.e("OpenRemote/POLLING", "failed", e);
      } catch (IOException e) {
         Log.e("OpenRemote/POLLING", "failed", e);
      }
   }
}
