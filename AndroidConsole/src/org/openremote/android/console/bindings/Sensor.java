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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parse the sensor node in sensor component.
 */
@SuppressWarnings("serial")
public class Sensor extends BusinessEntity {

   private int sensorId;
   
   /** The states are predefined for polling result. */
   private ArrayList<SensorState> states;

   public Sensor(Node node) {
      this.sensorId = Integer.valueOf(node.getAttributes().getNamedItem(REF).getNodeValue());
      states = new ArrayList<SensorState>();
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE && STATE.equals(childNodes.item(i).getNodeName())) {
            states.add(new SensorState(childNodes.item(i)));
         }
      }
   }
   public int getSensorId() {
      return sensorId;
   }
   public ArrayList<SensorState> getStates() {
      return states;
   }
   
   /**
    * Gets the state value by key.
    * 
    * @param key the key
    * 
    * @return the state value
    */
   public String getStateValue(String key) {
      int stateSize = states.size();
      for (int i = 0; i < stateSize; i++) {
         if (states.get(i).getName().equals(key)) {;
            return states.get(i).getValue();
         }
      }
      return null;
   }
}
