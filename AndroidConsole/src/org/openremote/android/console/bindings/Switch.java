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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for storing data about switch.
 */
@SuppressWarnings("serial")
public class Switch extends SensorComponent {

   /** It's for display in on state. */
   private Image onImage;
   
   /** It's for display in off state. */
   private Image offImage;
   
   public Switch(Node node) {
      setComponentId(Integer.valueOf(node.getAttributes().getNamedItem(ID).getNodeValue()));
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE && LINK.equals(childNodes.item(i).getNodeName())) {
            this.parser(childNodes.item(i));
         }
      }
   }

   public Image getOnImage() {
      return onImage;
   }

   public Image getOffImage() {
      return offImage;
   }

   /**
    * Parse the on image and off image from sensor states.
    * 
    * @see org.openremote.android.console.bindings.SensorComponent#parser(org.w3c.dom.Node)
    */
   @Override
   public void parser(Node node) {
      super.parser(node);
      for (SensorState state : getSensor().getStates()) {
         if(ON.equals(state.getName().toLowerCase())) {
            onImage = new Image(state.getValue());
         } else if (OFF.equals(state.getName().toLowerCase())) {
            offImage = new Image(state.getValue());
         }
      }
   }
}
