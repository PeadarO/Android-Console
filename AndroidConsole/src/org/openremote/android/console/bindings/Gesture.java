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
 * The screen gesture, which contains 4 swipe types: "top-to-bottom", "bottom-to-top", "left-to-right" and "right-to-left".
 */
@SuppressWarnings("serial")
public class Gesture extends Control {

   private int swipeType;
   public static final int GESTURE_SWIPE_TYPE_BOTTOM2TOP = 0;
   public static final int GESTURE_SWIPE_TYPE_LEFT2RIGHT = 1;
   public static final int GESTURE_SWIPE_TYPE_TOP2BOTTOM = 2;
   public static final int GESTURE_SWIPE_TYPE_RIGHT2LEFT = 3;
   
   private boolean hasControlCommand;
   private Navigate navigate;
   
   /**
    * Parse gesture node to instantiates a new gesture.
    * It contains swipe type, navigation and command.
    *  
    * @param node the node
    */
   public Gesture(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      this.setComponentId(Integer.valueOf(nodeMap.getNamedItem("id").getNodeValue()));
      String type = nodeMap.getNamedItem("type").getNodeValue();
      if ("swipe-top-to-bottom".equals(type)) {
         this.swipeType = GESTURE_SWIPE_TYPE_TOP2BOTTOM;
      } else if ("swipe-bottom-to-top".equals(type)) {
         this.swipeType = GESTURE_SWIPE_TYPE_BOTTOM2TOP;
      } else if ("swipe-left-to-right".equals(type)) {
         this.swipeType = GESTURE_SWIPE_TYPE_LEFT2RIGHT;
      } else if ("swipe-right-to-left".equals(type)) {
         this.swipeType = GESTURE_SWIPE_TYPE_RIGHT2LEFT;
      }
      
      if (nodeMap.getNamedItem("hasControlCommand") != null) {
         this.hasControlCommand = Boolean.valueOf(nodeMap.getNamedItem("hasControlCommand").getNodeValue());
      }
      
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Node elementNode = childNodes.item(i);
            if("navigate".equals(elementNode.getNodeName())) {
               this.navigate = new Navigate(elementNode);
            }
         }
      }
   }
   
   public int getSwipeType() {
      return swipeType;
   }

   public boolean isHasControlCommand() {
      return hasControlCommand;
   }

   public Navigate getNavigate() {
      return navigate;
   }
   
}
