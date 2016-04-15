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

/**
 * Represents Sensor State in sensor.
 * It use key-value structure, the name as key.
 */
@SuppressWarnings("serial")
public class SensorState extends BusinessEntity {

   private String name;
   private String value;
   
   public SensorState(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      this.name = nodeMap.getNamedItem("name").getNodeValue();
      this.value = nodeMap.getNamedItem("value").getNodeValue();
   }
   public String getName() {
      return name;
   }
   public String getValue() {
      return value;
   }
}
