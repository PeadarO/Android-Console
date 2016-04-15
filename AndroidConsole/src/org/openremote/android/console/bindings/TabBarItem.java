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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for storing data about tabBarItem.
 */
@SuppressWarnings("serial")
public class TabBarItem extends BusinessEntity {

   private String name;
   
   /** Navigate to. */
   private Navigate navigate;
   
   /** The image display on the item. */
   private Image image;
   
   public TabBarItem(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      if (nodeMap.getNamedItem(NAME) != null) {
         this.name = nodeMap.getNamedItem(NAME).getNodeValue();
      }
      
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Node elementNode = childNodes.item(i);
            if (NAVIGATE.equals(elementNode.getNodeName())) {
               this.navigate = new Navigate(elementNode);
            } else if (IMAGE.equals(elementNode.getNodeName())) {
               this.image = new Image(elementNode);
            }
         }
      }
   }

   public String getName() {
      return name;
   }

   public Navigate getNavigate() {
      return navigate;
   }

   public Image getImage() {
      return image;
   }
   
}
