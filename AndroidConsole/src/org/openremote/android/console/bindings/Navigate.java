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
 * Forwards to screen or do other logical functions.
 * Includes to group, to screen, to previous screen , to next screen, back, login, logout and setting.
 */
@SuppressWarnings("serial")
public class Navigate extends BusinessEntity {

   private int toScreen;
   private int toGroup;
   
   /** To previous screen in a group. */
   private boolean previousScreen;
   
   /** To next screen in a group. */
   private boolean nextScreen;
   
   /** Back to last display screen. */
   private boolean back;
   
   /** To display settings view. */
   private boolean setting;
   
   /** To display login view. */
   private boolean login;
   
   /** Make the user to logout. */
   private boolean logout;
   
   /** The last display screen. */
   private int fromScreen;
   
   /** The last visit group. */
   private int fromGroup;

   /**
    * Instantiates a new navigate without properties.
    */
   public Navigate() {
   }
   
   /**
    * Instantiates a new navigate by parse navigate node.
    * 
    * @param node the navigate node
    */
   public Navigate(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      if (nodeMap.getNamedItem("toGroup") != null) {
         this.toGroup = Integer.valueOf(nodeMap.getNamedItem("toGroup").getNodeValue());
         if (nodeMap.getNamedItem("toScreen") != null) {
            this.toScreen = Integer.valueOf(nodeMap.getNamedItem("toScreen").getNodeValue());
         }
      } else if (nodeMap.getNamedItem("to") != null) {
         String toValue = nodeMap.getNamedItem("to").getNodeValue().toLowerCase();
         if ("previousscreen".equals(toValue)) {
            this.previousScreen = true;
         } else if ("nextscreen".equals(toValue)) {
            this.nextScreen = true;
         } else if ("login".equals(toValue)) {
            this.login = true;
         } else if ("logout".equals(toValue)) {
            this.logout = true;
         } else if ("setting".equals(toValue)) {
            this.setting = true;
         } else if ("back".equals(toValue)) {
            this.back = true;
         }
      }
   }

   public boolean isPreviousScreen() {
      return previousScreen;
   }

   public boolean isNextScreen() {
      return nextScreen;
   }

   public boolean isBack() {
      return back;
   }

   public boolean isSetting() {
      return setting;
   }

   public boolean isLogin() {
      return login;
   }

   public boolean isLogout() {
      return logout;
   }

   public int getToScreen() {
      return toScreen;
   }

   public int getToGroup() {
      return toGroup;
   }

   public int getFromScreen() {
      return fromScreen;
   }

   public int getFromGroup() {
      return fromGroup;
   }

   public void setToScreen(int toScreen) {
      this.toScreen = toScreen;
   }

   public void setToGroup(int toGroup) {
      this.toGroup = toGroup;
   }

   public void setFromScreen(int fromScreen) {
      this.fromScreen = fromScreen;
   }

   public void setIsBack(boolean isBack) {
      this.back = isBack;
   }

   public void setFromGroup(int fromGroup) {
      this.fromGroup = fromGroup;
   }
   
   public void setIsPreviousScreen(boolean previousScreen) {
     this.previousScreen = previousScreen;
   }

   public void setIsNextScreen(boolean nextScreen) {
     this.nextScreen = nextScreen;
   }
}
