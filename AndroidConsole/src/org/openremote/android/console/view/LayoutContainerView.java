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

import org.openremote.android.console.bindings.AbsoluteLayoutContainer;
import org.openremote.android.console.bindings.GridLayoutContainer;
import org.openremote.android.console.bindings.LayoutContainer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;

public class LayoutContainerView extends FrameLayout {

   protected LayoutContainerView(Context context) {
      super(context);
      
      this.setLongClickable(true);
      this.setOnLongClickListener(new OnLongClickListener() {
        
        @Override
        public boolean onLongClick(View v) {
          return true;
        }
      });
   }

   public static LayoutContainerView buildWithLayoutContainer(Context context, LayoutContainer layout) {
      LayoutContainerView layoutContainerView = null;
      if (layout instanceof AbsoluteLayoutContainer) {
         layoutContainerView = new AbsoluteLayoutContainerView(context, (AbsoluteLayoutContainer)layout);
      } else if(layout instanceof GridLayoutContainer) {
         layoutContainerView = new GridLayoutContainerView(context, (GridLayoutContainer)layout);
      }
      return layoutContainerView;
   }
}
