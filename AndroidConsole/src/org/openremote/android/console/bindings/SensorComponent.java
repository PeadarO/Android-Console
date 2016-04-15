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

/**
 * The super component class of which have sensor.
 */
@SuppressWarnings("serial")
public class SensorComponent extends Component {

   private Sensor sensor;

   /**
    * Parser the sensor node in the component.
    * 
    * @param node the node
    */
   public void parser(Node node) {
      if (LINK.equals(node.getNodeName()) && SENSOR.equals(node.getAttributes().getNamedItem(TYPE).getNodeValue())) {
         sensor = new Sensor(node);
      }
   }
   
   public Sensor getSensor() {
      return sensor;
   }
   
}
