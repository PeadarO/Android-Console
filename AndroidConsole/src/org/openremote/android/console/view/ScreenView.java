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

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.openremote.android.console.Constants;
import org.openremote.android.console.bindings.Background;
import org.openremote.android.console.bindings.LayoutContainer;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.model.PollingHelper;
import org.openremote.android.console.util.ImageUtil;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class ScreenView extends RelativeLayout {

   private Screen screen;
   private PollingHelper polling;
   /**
    * Instantiates a new screen view.
    * 
    * @param context
    *           the context
    * @param screen
    *           the screen
    */
   public ScreenView(Context context, Screen screen) {
      super(context);
      this.screen = screen;
      setBackgroundColor(0);
      setTag(screen.getName());
      setId(screen.getScreenId());
      if (screen.getBackground() != null) {
         addBackground();

      }

    ArrayList<LayoutContainer> layouts = screen.getLayouts();
    for (int i = 0; i < layouts.size(); i++) {
      LayoutContainerView la = LayoutContainerView
              .buildWithLayoutContainer(context, layouts.get(i));
      if (la != null) {
        LayoutContainer layout = layouts.get(i);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layout.getWidth(),
                layout.getHeight());
        params.topMargin = layout.getTop();
        params.leftMargin = layout.getLeft();
        addView(la, params);
      }
    }

  }

  /**
   * @param screen
   */
  private void addBackground() {
    ImageView backgroundView = new ImageView(this.getContext());
    int left = 0;
    int top = 0;
    int screenWidth = screen.isLandscape() ? Screen.SCREEN_HEIGHT : Screen.SCREEN_WIDTH;
    int screenHeight = screen.isLandscape() ? Screen.SCREEN_WIDTH - Screen.SCREEN_STATUS_BAR_HEIGHT
            : Screen.SCREEN_HEIGHT - Screen.SCREEN_STATUS_BAR_HEIGHT;
    Background background = screen.getBackground();
    BitmapDrawable backgroundBitmap = null;
    String imagePath = Constants.FILE_FOLDER_PATH + screen.getBackgroundSrc();
    // BitmapDrawable backgroundBitmap =
    // ImageUtil.createFromPathQuietly(getContext(),
    // Constants.FILE_FOLDER_PATH + screen.getBackgroundSrc());
    //
    // if (backgroundBitmap == null) {
    // return;
    // }
    //
    // int imageWidth = backgroundBitmap.getIntrinsicWidth();
    // int imageHeight = backgroundBitmap.getIntrinsicHeight();

    if (background.isFillScreen()) {
      backgroundBitmap = ImageUtil.createScaledDrawableFromPath(getContext(), imagePath, screenWidth, screenHeight, false, true);
      //backgroundBitmap = ImageUtil.createClipedDrawableFromPath(getContext(), imagePath, screenWidth, screenHeight, screenWidth, screenHeight);
      // backgroundView.setScaleType(ScaleType.CENTER_CROP);
    } else {
      backgroundBitmap = ImageUtil.createFromPathQuietly(getContext(), imagePath, screenWidth, screenHeight);
      if (background.isBackgroundImageAbsolutePosition()) {        
          left = background.getBackgroundImageAbsolutePositionLeft();
          top = background.getBackgroundImageAbsolutePositionTop();
      } else {
        Pair<Integer, Integer> size = ImageUtil.getNativeImageSize(imagePath);        
        String backgroundImageRelativePosition = background.getBackgroundImageRelativePosition();
        
        if ("top_left".equals(backgroundImageRelativePosition)) {
        } else if ("top".equals(backgroundImageRelativePosition)) {
          left = (screenWidth - size.first) / 2;
        } else if ("top_right".equals(backgroundImageRelativePosition)) {
          left = screenWidth - size.first;
        } else if ("left".equals(backgroundImageRelativePosition)) {
          top = (screenHeight - size.second) / 2;
        } else if ("center".equals(backgroundImageRelativePosition)) {
          left = (screenWidth - size.first) / 2;
          top = (screenHeight - size.second) / 2;
        } else if ("right".equals(backgroundImageRelativePosition)) {
          left = screenWidth - size.first;
          top = (screenHeight - size.second) / 2;
        } else if ("bottom".equals(backgroundImageRelativePosition)) {
          left = (screenWidth - size.first) / 2;
          top = screenHeight - size.second;
        } else if ("bottom_left".equals(backgroundImageRelativePosition)) {
          top = screenHeight - size.second;
        } else if ("bottom_right".equals(backgroundImageRelativePosition)) {
          left = screenWidth - size.first;
          top = screenHeight - size.second;
        }
      }
    }
    
    LayoutParams layout = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT);
    backgroundView.setAdjustViewBounds(true);
    backgroundView.setScaleType(ScaleType.MATRIX);
    layout.topMargin = top;
    layout.leftMargin = left;
    backgroundView.setImageDrawable(backgroundBitmap);
    backgroundView.setLayoutParams(layout);
    addView(backgroundView);
  }

  /**
   * Start polling on the screen's sensor components.
   */
  public void startPolling() {
    if (!screen.getPollingComponentsIds().isEmpty()) {
      polling = new PollingHelper(screen.getPollingComponentsIds(), getContext());
    }
    if (polling != null) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          polling.requestCurrentStatusAndStartPolling();
        }
      }).start();
    }
  }

  public void cancelPolling() {
    if (polling != null) {
      polling.cancelPolling();
    }
  }

  public Screen getScreen() {
    return screen;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    try {
    super.onDraw(canvas);
    } catch (Exception e) {
      Log.e("Screen Error", "Screen Error", e);
    }
  }
}
