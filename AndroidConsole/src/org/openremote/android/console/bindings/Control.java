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

import android.util.Log;


/**
 * The super class of control component, which include button, switch and slider.
 */
@SuppressWarnings("serial")
public class Control extends Component {

   /**
    * Builds the control component by parse component node, 
    * which include button, switch and slider.
    * 
    * @param node the node
    * 
    * @return the control component
    */
   public static Component buildWithXML(Node node) {
      Component component = null;
      if (node == null) {
         Log.e("OpenRemote-COMPONENT", "The node is null in buildWithXML.");
         return null;
      }
      if (BUTTON.equals(node.getNodeName())) {
         component =  new ORButton(node);
      } else if (SWITCH.equals(node.getNodeName())) {
         component = new Switch(node);
      } else if (SLIDER.equalsIgnoreCase(node.getNodeName())) {
         component = new Slider(node);
      } else if (COLORPICKER.equals(node.getNodeName())) {
         component = new ColorPicker(node);
      }
      return component;
   }
}
