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

import java.util.HashSet;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The subclass of LayoutContainer which include a component.
 * It parse the absolute node, contains size and position info.
 * 
 */
@SuppressWarnings("serial")
public class AbsoluteLayoutContainer extends LayoutContainer {

   private Component component;
   
   /**
    * Instantiates a new absolute layout container by parse absolute node.
    * 
    * @param node the absolute node
    */
   public AbsoluteLayoutContainer(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      this.left = Integer.valueOf(nodeMap.getNamedItem("left").getNodeValue());
      this.top = Integer.valueOf(nodeMap.getNamedItem("top").getNodeValue());
      this.width = Integer.valueOf(nodeMap.getNamedItem("width").getNodeValue());
      this.height = Integer.valueOf(nodeMap.getNamedItem("height").getNodeValue());
      
      this.left   = (int)((double)this.left * Screen.WIDTH_SCALE); 
      this.top    = (int)((double)this.top * Screen.HEIGHT_SCALE); 
      this.width  = (int)((double)this.width * Screen.WIDTH_SCALE); 
      this.height = (int)((double)this.height * Screen.HEIGHT_SCALE); 
      
      NodeList nodes = node.getChildNodes();
      int nodeNum = nodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            this.component = Component.buildWithXML(nodes.item(i));
            break;
         }
      }
   }
   
   public Component getComponent() {
      return component;
   }
   
   @Override
   public HashSet<Integer> getPollingComponentsIds() {
      HashSet<Integer> ids = new HashSet<Integer>();
      if (component instanceof SensorComponent) {
         if (((SensorComponent)component).getSensor() != null && ((SensorComponent)component).getSensor().getSensorId() > 0) {
            ids.add(((SensorComponent)component).getSensor().getSensorId());
         }
      }
      return ids;
   }
}
