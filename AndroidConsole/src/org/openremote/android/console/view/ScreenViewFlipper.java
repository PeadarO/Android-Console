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

import org.openremote.android.console.R;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

/**
 * The ScreenViewFlipper is for fling a group of screen views and make some animation.
 */
public class ScreenViewFlipper extends ViewFlipper {
   private Animation slideLeftIn;
   private Animation slideLeftOut;
   private Animation slideRightIn;
   private Animation slideRightOut;
   public ScreenViewFlipper(Context context) {
      super(context);
      slideLeftIn = AnimationUtils.loadAnimation(context,R.anim.slide_to_left_in);
      slideLeftOut = AnimationUtils.loadAnimation(context,R.anim.slide_to_left_out);
      slideRightIn = AnimationUtils.loadAnimation(context,R.anim.slide_to_right_in);
      slideRightOut = AnimationUtils.loadAnimation(context,R.anim.slide_to_right_out);
   }
   
   public void setToPreviousAnimation() {
      setInAnimation(slideRightIn);
      setOutAnimation(slideRightOut);
   }
   
   public void setToNextAnimation() {
      setInAnimation(slideLeftIn);
      setOutAnimation(slideLeftOut);
   }
}
