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
package org.openremote.android.console.view;

import org.openremote.android.console.bindings.Group;
import org.openremote.android.console.bindings.Screen;

import android.content.Context;

/**
 * The GroupView contains portrait and landscape screen view flipper, which can be fling in a group of screens .
 */
public class GroupView {

   private Group group;
   private ScreenViewFlipper portraitScreenViewFlipper;
   private ScreenViewFlipper landscapeScreenViewFlipper;
   public GroupView(Context context, Group group) {
      this.group = group;
      if (group.getPortraitScreens().size() > 0) {
         portraitScreenViewFlipper = new ScreenViewFlipper(context);
         for (Screen screen : group.getPortraitScreens()) {
            portraitScreenViewFlipper.addView(new ScreenView(context, screen));
         }
      }
      
      if (group.getLandscapeScreens().size() > 0) {
         landscapeScreenViewFlipper = new ScreenViewFlipper(context);
         for (Screen screen : group.getLandscapeScreens()) {
            landscapeScreenViewFlipper.addView(new ScreenView(context, screen));
         }
      }
   }
   
   public Group getGroup() {
      return group;
   }
   public ScreenViewFlipper getScreenViewFlipperByOrientation(boolean landscape) {
      if (landscape) {
         return landscapeScreenViewFlipper;
      }
      return portraitScreenViewFlipper;
   }
   public void setGroup(Group group) {
      this.group = group;
   }
   
}
