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

/**
 * The super class of AbsoluteLayoutContainer and GridLayoutContainer.
 * It contains the absolute position and size information.
 */
@SuppressWarnings("serial")
public class LayoutContainer extends BusinessEntity {

   protected int left;
   protected int top;
   protected int width;
   protected int height;
   
   public int getLeft() {
      return left;
   }

   public int getTop() {
      return top;
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   /**
    * Gets the polling components ids in the container.
    * 
    * @return the polling components ids
    */
   public HashSet<Integer> getPollingComponentsIds() {
      return null;
   }
}
