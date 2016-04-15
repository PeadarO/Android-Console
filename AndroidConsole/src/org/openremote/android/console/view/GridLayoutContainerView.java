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

import java.util.ArrayList;

import org.openremote.android.console.bindings.GridCell;
import org.openremote.android.console.bindings.GridLayoutContainer;

import android.content.Context;
import android.widget.AbsoluteLayout;

/**
 * The GridLayoutContainerView in screen's absolute position, and contains grid cell views.
 */
public class GridLayoutContainerView extends LayoutContainerView {

   @SuppressWarnings("deprecation")
   public GridLayoutContainerView(Context context, GridLayoutContainer gridLayoutContainer) {
      super(context);
      AbsoluteLayout gridContainer = new AbsoluteLayout(context);
      ArrayList<GridCell> gridCells = gridLayoutContainer.getCells();
      int cellSize = gridCells.size();
      int cellWidth = gridLayoutContainer.getWidth()/gridLayoutContainer.getCols();
      int cellHeight = gridLayoutContainer.getHeight()/gridLayoutContainer.getRows();
      for (int i = 0; i < cellSize; i++) {
         GridCell gridCell = gridCells.get(i);
         gridContainer.addView(new GridCellView(context, cellWidth, cellHeight, gridCell),
               new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, cellWidth
                     * gridCell.getX(), cellHeight * gridCell.getY()));
      }
      addView(gridContainer);
   }

}
