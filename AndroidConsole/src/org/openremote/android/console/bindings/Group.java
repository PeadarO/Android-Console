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
import java.util.List;

import org.openremote.android.console.model.XMLEntityDataBase;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Group is parsed by group node, which contains id, name, screens and tabBar.
 */
@SuppressWarnings("serial")
public class Group extends BusinessEntity{

   private int groupId;
   private String name;
   private List<Screen> screens;
   private TabBar tabBar;
   private List<Screen> portraitScreens;
   private List<Screen> landscapeScreens;
   
   public Group(Node node) {
      screens = new ArrayList<Screen>();
      
      NamedNodeMap nodeMap = node.getAttributes();
      this.groupId = Integer.valueOf(nodeMap.getNamedItem("id").getNodeValue());
      this.name = nodeMap.getNamedItem("name").getNodeValue();
      NodeList nodeList = node.getChildNodes();
      int childNum = nodeList.getLength();
      for (int i = 0; i < childNum; i++) {
         Node childNode = nodeList.item(i);
         if(childNode.getNodeType() == Node.ELEMENT_NODE) {
            if ("tabbar".equals(childNode.getNodeName())) {
               this.tabBar = new TabBar(childNode);
            } else if ("include".equals(childNode.getNodeName())) {
               screens.add(XMLEntityDataBase.screens.get(Integer.valueOf(childNode.getAttributes().getNamedItem("ref")
                     .getNodeValue())));
            }
         }
      }
   }

   public int getGroupId() {
      return groupId;
   }

   public String getName() {
      return name;
   }

   public List<Screen> getScreens() {
      return screens;
   }

   public TabBar getTabBar() {
      return tabBar;
   }
   
   /**
    * Gets the portrait screens from the group's screens.
    * 
    * @return the portrait screens
    */
   public List<Screen> getPortraitScreens() {
      if (portraitScreens == null) {
         portraitScreens = new ArrayList<Screen>();
         for (Screen screen : screens) {
            if (!screen.isLandscape()) {
               portraitScreens.add(screen);
            }
         }
      }
      return portraitScreens;
   }
   
   /**
    * Gets the landscape screens from the group's screens.
    * 
    * @return the landscape screens
    */
   public List<Screen> getLandscapeScreens() {
      if (landscapeScreens == null) {
         landscapeScreens = new ArrayList<Screen>();
         for (Screen screen : screens) {
            if (screen.isLandscape()) {
               landscapeScreens.add(screen);
            }
         }
      }
      return landscapeScreens;
   }
   
   /**
    * Check the group if has a screen by the screen id and the orientation.
    * 
    * @param screenId the screen id
    * @param landscape the landscape
    * 
    * @return true, if successful
    */
   public boolean canfindScreenByIdAndOrientation(int screenId, boolean landscape) {
      for (Screen screen : screens) {
         if (screen.getScreenId() == screenId && screen.isLandscape() == landscape) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Gets the group's screen size by the orientation.
    * 
    * @param landscape the landscape
    * 
    * @return the screen size by orientation
    */
   public int getScreenSizeByOrientation(boolean landscape) {
      if(landscape) {
         return getLandscapeScreens().size();
      }
      return getPortraitScreens().size();
   }
   
   /**
    * Gets the screen index by orientation(in portrait screens or landscape screens).
    * 
    * @param screen the screen
    * @param landscape the landscape
    * 
    * @return the screen index by orientation
    */
   public int getScreenIndexByOrientation(Screen screen, boolean landscape) {
      if(landscape) {
         return getLandscapeScreens().indexOf(screen);
      }
      return getPortraitScreens().indexOf(screen);
   }
   
   /**
    * Checks the group if have the orientation's screens.
    * 
    * @param landscape the landscape
    * 
    * @return true, if successful
    */
   public boolean hasOrientationScreens(boolean landscape) {
      if(landscape) {
         return getLandscapeScreens().size() > 0 ? true : false;
      }
      return getPortraitScreens().size() > 0 ? true : false;
   }
}
