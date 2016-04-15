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
package org.openremote.android.console;

import roboguice.activity.RoboActivity;
import android.os.Bundle;
import android.view.Window;

/**
 * The Class GenericActivity is the super activity in the application.
 */
public class GenericActivity extends RoboActivity {

   /** The is activity resumed. */
   private boolean isActivityResumed;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   protected void onPause() {
      isActivityResumed = false;
      super.onPause();
   }

   @Override
   protected void onResume() {
      isActivityResumed = true;
      super.onResume();
   }
   
   /**
    * Checks if is activity resumed.
    * If activity is resumed, can operate it's views;
    * otherwise can't.
    * 
    * @return true, if is activity resumed
    */
   public boolean isActivityResumed() {
      return isActivityResumed;
   }
}
