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
import org.openremote.android.console.bindings.Component;

import android.content.Context;

/**
 * Wraps component view and display in a absolute position.
 */
public class AbsoluteLayoutContainerView extends LayoutContainerView {

   public AbsoluteLayoutContainerView(Context context, AbsoluteLayoutContainer absoluteLayoutContainer) {
      super(context);
      Component component = absoluteLayoutContainer.getComponent();
      component.setFrameWidth(absoluteLayoutContainer.getWidth());
      component.setFrameHeight(absoluteLayoutContainer.getHeight());
      ComponentView componentView = ComponentView.buildWithComponent(context, component);
      if (componentView != null) {
         addView(componentView);
      }
   }

}
