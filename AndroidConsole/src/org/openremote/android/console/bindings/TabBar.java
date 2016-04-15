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

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The tabBar contains tabBarItems, which displayed as menus.
 */
@SuppressWarnings("serial")
public class TabBar extends BusinessEntity {

   private ArrayList<TabBarItem> tabBarItems;
   
   /**
    * Instantiates a new tab bar by parse tabBar node.
    * 
    * @param node the tabBar node
    */
   public TabBar(Node node) {
      tabBarItems = new ArrayList<TabBarItem>();
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Node elementNode = childNodes.item(i);
            if (TABBAR_ITEM.equals(elementNode.getNodeName())) {
               tabBarItems.add(new TabBarItem(elementNode));
            }
         }
      }
   }
   
   public ArrayList<TabBarItem> getTabBarItems() {
      return tabBarItems;
   }

   
}
