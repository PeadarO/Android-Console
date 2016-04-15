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
 * The grid cell include a component, have position and span in grid.
 */
@SuppressWarnings("serial")
public class GridCell extends BusinessEntity {

   private int x;
   private int y;
   private int rowspan = 1;
   private int colspan = 1;
   private Component component;
   
   public GridCell(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      this.x = Integer.valueOf(nodeMap.getNamedItem("x").getNodeValue()); 
      this.y = Integer.valueOf(nodeMap.getNamedItem("y").getNodeValue());
      if (nodeMap.getNamedItem("rowspan") != null) {
         this.rowspan = Integer.valueOf(nodeMap.getNamedItem("rowspan").getNodeValue()); 
      }
      if (nodeMap.getNamedItem("colspan") != null) {
         this.colspan = Integer.valueOf(nodeMap.getNamedItem("colspan").getNodeValue());
      }
      
      NodeList nodes = node.getChildNodes();
      int nodeNum = nodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            this.component = Component.buildWithXML(nodes.item(i));
            break;
         }
      }
   }
   public int getX() {
      return x;
   }
   public int getY() {
      return y;
   }
   public int getRowspan() {
      return rowspan;
   }
   public int getColspan() {
      return colspan;
   }
   public Component getComponent() {
      return component;
   }
   
   
   
}
