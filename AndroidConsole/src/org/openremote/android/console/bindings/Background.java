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
 * The background of screen, which contains background position in screen.
 * The position include absolute position and relative position.
 * 
 */
@SuppressWarnings("serial")
public class Background extends BusinessEntity {

   private boolean fillScreen = false;
   private boolean isBackgroundImageAbsolutePosition = false;
   private int backgroundImageAbsolutePositionLeft;
   private int backgroundImageAbsolutePositionTop;
   private String backgroundImageRelativePosition;
   private Image backgroundImage;
   
   public Background(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      if (nodeMap.getNamedItem("fillScreen") != null) {
         this.fillScreen = Boolean.valueOf(nodeMap.getNamedItem("fillScreen").getNodeValue());
      }
      if (nodeMap.getNamedItem("absolute") != null) {
         this.isBackgroundImageAbsolutePosition = true;
         String[] absolute = nodeMap.getNamedItem("absolute").getNodeValue().split("\\,");
         this.backgroundImageAbsolutePositionLeft = Integer.valueOf(absolute[0]);
         this.backgroundImageAbsolutePositionTop = Integer.valueOf(absolute[1]);
      } else if (nodeMap.getNamedItem("relative") != null) {
         this.backgroundImageRelativePosition = nodeMap.getNamedItem("relative").getNodeValue().toLowerCase();
      }
      
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE && "image".equals(childNodes.item(i).getNodeName())) {
            this.backgroundImage = new Image(childNodes.item(i));
         }
      }
   }
   
   /**
    * Checks if is fill screen.
    * If true, the background position is (0,0).
    * 
    * @return true, if is fill screen
    */
   public boolean isFillScreen() {
      return fillScreen;
   }
   public boolean isBackgroundImageAbsolutePosition() {
      return isBackgroundImageAbsolutePosition;
   }
   public int getBackgroundImageAbsolutePositionLeft() {
      return backgroundImageAbsolutePositionLeft;
   }
   public int getBackgroundImageAbsolutePositionTop() {
      return backgroundImageAbsolutePositionTop;
   }
   public String getBackgroundImageRelativePosition() {
      return backgroundImageRelativePosition;
   }
   public Image getBackgroundImage() {
      return backgroundImage;
   }
}
