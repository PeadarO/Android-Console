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

import org.openremote.android.console.model.XMLEntityDataBase;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The image component can has sensor and can change status.
 * It can also has a linked label to display as a label. 
 */
@SuppressWarnings("serial")
public class Image extends SensorComponent {

   private String src;
   
   /** The image's style, but now is unUsed. */
   private String style;
   
   /** The linked label. */
   private Label label;
   
   /** The linked label id. */
   private int labelRefId = 0;
   
   /**
    * Instantiates a new image by parse the image node.
    * 
    * @param node the node
    */
   public Image(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      if (nodeMap.getNamedItem(ID) != null) {
         setComponentId(Integer.valueOf(nodeMap.getNamedItem(ID).getNodeValue()));
      }
      
      if (nodeMap.getNamedItem("src") != null) {
         this.src = nodeMap.getNamedItem("src").getNodeValue();
         XMLEntityDataBase.imageSet.add(src);
      }
      
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE && LINK.equals(childNodes.item(i).getNodeName())) {
            this.parser(childNodes.item(i));
         } else if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE && INCLUDE.equals(childNodes.item(i).getNodeName())) {
            labelRefId = Integer.valueOf(childNodes.item(i).getAttributes().getNamedItem(REF).getNodeValue());
            setLinkedLabel();
         }
      }
   }
   
   public Image(String src) {
      this.src = src;
      XMLEntityDataBase.imageSet.add(src);
   }
   public String getSrc() {
      return src;
   }
   public String getStyle() {
      return style;
   }
   
   @Override
   public void parser(Node node) {
      super.parser(node);
      for (SensorState state : getSensor().getStates()) {
         XMLEntityDataBase.imageSet.add(state.getValue());
      }
   }

   public Label getLabel() {
      return label;
   }

   public void setLinkedLabel() {
      if (label == null) {
         label = XMLEntityDataBase.labels.get(labelRefId);
      }
   }
}
