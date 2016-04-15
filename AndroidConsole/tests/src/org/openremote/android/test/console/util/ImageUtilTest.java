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
package org.openremote.android.test.console.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openremote.android.console.Constants;
import org.openremote.android.console.util.ImageUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.InstrumentationTestCase;

/**
 * The Class test to parse panel.xml.
 */
public class ImageUtilTest extends InstrumentationTestCase
{
  public static final String LIGHTS_BUTTON_FILENAME = "LightsButton1303397127497.png";
  public static final int LIGHTS_BUTTON_WIDTH = 416;
  public static final int LIGHTS_BUTTON_HEIGHT = 106;

  /** the instrumentation context */
  private Context ctx;
  /** context of application under test */
  private Context targetCtx;

  public void setUp() throws IOException
  {
    ctx = getInstrumentation().getContext();
    targetCtx = getInstrumentation().getTargetContext();

    // copy the example image file from the assets folder to the data folder
    InputStream in = ctx.getAssets().open("fixture/" + LIGHTS_BUTTON_FILENAME);
    // Trying to invoke openFileOutput() on ctx (the instrumentation context)
    // throws a NullPointerException.  Use the target context instead.
    FileOutputStream out = targetCtx.openFileOutput(LIGHTS_BUTTON_FILENAME,
        Context.MODE_PRIVATE);

    byte buf[] = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0)
    {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }

  public void tearDown() throws IOException
  {
    targetCtx.deleteFile(LIGHTS_BUTTON_FILENAME);
  }

  /**
   * Running this on a device with high density or greater should demonstrate that
   * bitmaps are not being scaled down by ImageUtil.createFromPathQuietly().
   *
   * @throws IOException
   */
  public void testCreateFromPathQuietlyNoScaling() throws IOException
  {
    Drawable d = ImageUtil.createFromPathQuietly(targetCtx, Constants.FILE_FOLDER_PATH + LIGHTS_BUTTON_FILENAME);
    assertEquals(LIGHTS_BUTTON_WIDTH, d.getIntrinsicWidth());
    assertEquals(LIGHTS_BUTTON_HEIGHT, d.getIntrinsicHeight());
  }

  /**
   * Running this on a device with high density or greater should demonstrate that
   * bitmaps are not being scaled down by ImageUtil.testCreateClipedDrawableFromPath().
   *
   * @throws IOException
   */
  public void testCreateClipedDrawableFromPathNoScaling() throws IOException
  {
    BitmapDrawable bd = ImageUtil.createClipedDrawableFromPath(targetCtx,
        Constants.FILE_FOLDER_PATH + LIGHTS_BUTTON_FILENAME, LIGHTS_BUTTON_WIDTH,
        LIGHTS_BUTTON_HEIGHT);
    assertEquals(LIGHTS_BUTTON_WIDTH, bd.getIntrinsicWidth());
    assertEquals(LIGHTS_BUTTON_HEIGHT, bd.getIntrinsicHeight());
  }
}
