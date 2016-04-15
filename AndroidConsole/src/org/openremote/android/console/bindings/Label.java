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
 * The label can set font size and color, change text by polling status.
 */
@SuppressWarnings("serial")
public class Label extends SensorComponent {

   private int fontSize;
   private String color;
   private String text;
   
   public Label(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      setComponentId(Integer.valueOf(nodeMap.getNamedItem(ID).getNodeValue()));
      if (nodeMap.getNamedItem(FONT_SIZE) != null) {
         this.fontSize = Integer.valueOf(nodeMap.getNamedItem(FONT_SIZE).getNodeValue());
      }
      if (nodeMap.getNamedItem(COLOR) != null) {
         this.color = nodeMap.getNamedItem(COLOR).getNodeValue();
      }
      if (nodeMap.getNamedItem(TEXT) != null) {
         this.text = nodeMap.getNamedItem(TEXT).getNodeValue();
      }
      
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE && LINK.equals(childNodes.item(i).getNodeName())) {
            this.parser(childNodes.item(i));
         }
      }
   }
   
   public int getFontSize() {
      return fontSize;
   }
   public String getColor() {
      return color;
   }
   public String getText() {
      return text;
   }
   
   
}
