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
import java.util.HashSet;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The subclass of LayoutContainer which include grid cells.
 * It contains column size and row size.
 * 
 */
@SuppressWarnings("serial")
public class GridLayoutContainer extends LayoutContainer {

   private int rows;
   private int cols;
   private ArrayList<GridCell> cells;
   
   /**
    * Instantiates a new grid layout container by parse a grid node.
    * 
    * @param node the grid node
    */
   public GridLayoutContainer(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      this.left = Integer.valueOf(nodeMap.getNamedItem("left").getNodeValue());
      this.top = Integer.valueOf(nodeMap.getNamedItem("top").getNodeValue());
      this.width = Integer.valueOf(nodeMap.getNamedItem("width").getNodeValue());
      this.height = Integer.valueOf(nodeMap.getNamedItem("height").getNodeValue());
      
      this.left   = (int)((double)this.left * Screen.WIDTH_SCALE); 
      this.top    = (int)((double)this.top * Screen.HEIGHT_SCALE); 
      this.width  = (int)((double)this.width * Screen.WIDTH_SCALE); 
      this.height = (int)((double)this.height * Screen.HEIGHT_SCALE); 
      
      this.rows = Integer.valueOf(nodeMap.getNamedItem("rows").getNodeValue());
      this.cols = Integer.valueOf(nodeMap.getNamedItem("cols").getNodeValue());
      cells = new ArrayList<GridCell>();
      NodeList cellNodes = node.getChildNodes();
      int cellNodeSize = cellNodes.getLength();
      for (int i = 0; i < cellNodeSize; i++) {
         if(cellNodes.item(i).getNodeType() == Node.ELEMENT_NODE && "cell".equals(cellNodes.item(i).getNodeName())) {
            cells.add(new GridCell(cellNodes.item(i)));
         }
      }
   }
   
   public int getRows() {
      return rows;
   }
   
   public int getCols() {
      return cols;
   }
   
   /**
    * Gets the grid cells.
    * 
    * @return the cells
    */
   public ArrayList<GridCell> getCells() {
      return cells;
   }
   
   @Override
   public HashSet<Integer> getPollingComponentsIds() {
      HashSet<Integer> ids = new HashSet<Integer>();
      for (GridCell cell : cells) {
         if (cell.getComponent() instanceof SensorComponent) {
            Sensor sensor = ((SensorComponent)cell.getComponent()).getSensor();
            if (sensor != null && sensor.getSensorId() > 0) {
               ids.add(sensor.getSensorId());
            }
         }
      }
      return ids;
   }
}
