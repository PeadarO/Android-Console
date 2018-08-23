/*
 * OpenRemote, the Home of the Digital Home.
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

import android.content.Context;
import android.view.Menu;

/**
 * constants used throughout the application in more than one place
 * 
 * @author Andrew C. Oliver <acoliver osintegrators.com>
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * 
 */
public class Constants
{

  private Constants() {}


  public static final long BINDING_VERSION = 1L;
  public static final long DEFAULT_FONT_SIZE_DIP = 12;
  public static final long BUTTON_FONT_SIZE_DIP = 12;
  public static final long MODEL_VERSION = 2L;
  public static final long CACHE_VERSION = 3L;
  public static final int MENU_ITEM_SETTING = Menu.FIRST + 1;
  public static final int MENU_ITEM_LOGOUT = Menu.FIRST + 2;
  public static final int MENU_ITEM_QUIT = Menu.FIRST + 3;
  public static final String ERROR = "error";
  public static final int DIALOG_ERROR_ID = 0;

  // TODO : can be replaced with HttpURLConnection.HTTP_OK
  public static final int HTTP_SUCCESS = 200;
  public static final int HTTP_UNAUTHORISED = 401; 
  /**
   * Default timeout (in milliseconds) to wait for a connection to a controller
   * to be established
   */
  public static final int DEFAULT_CONTROLLER_CONNECTION_TIMEOUT = 5 * 1000;
  /**
   * Default timeout (in milliseconds) to wait for data on a connection to a
   * controller
   */
  public static final int DEFAULT_CONTROLLER_SOCKET_TIMEOUT = 5 * 1000;
  

  public static final String ACTIVITY = "activity";
  public static final String ELEMENT_OPENREMOTE = "openremote";
  public static final String ELEMENT_BUTTONS = "buttons";
  public static final String URL = "url";
  public static final String LOADER = "loader";
  public static final String ADD_CONTROLLER_ACTION = "ADD_CONTROLLER";
  public static final String EDIT_CONTROLLER_ACTION = "EDIT_CONTROLLER";
  public static final int REQUEST_CODE = 1;
  public static final int ADD_CONTROLLER = 2;
  public static final int RESULT_PANEL_SELECTED = 3;
  public static final int RESULT_CONTROLLER_DELETE = 4;
  public static final int EDIT_CONTROLLER = 5;
  public static final int CONTROLLER_OBJECT = 6;
  public static final int CONTROLLER_ADDED = 7;
  public static final int CONTROLLER_UPDATED = 8;
  public static final String MULTICAST_ADDRESS = "224.0.1.100";
  public static final String NON_WIFI_MULTICAST_ADDRESS = "10.0.2.2";
  public static final int MULTICAST_PORT = 3333;
  public static final int LOCAL_SERVER_PORT = 2346;
  /**
   * How long (in milliseconds) the local server that receives responses from controllers for
   * auto-discovery will wait before timing out (set to 0 for no time out)
   */
  public static final int LOCAL_DISCOVERY_SERVER_TIMEOUT = 5000;
  public static final String PANEL_XML = "panel.xml";
  public static final String FILE_FOLDER_PATH = "/data/data/org.openremote.android.console/files/";
  public static final int SECURED_HTTP_PORT = 8443;
  public static final int HTTP_CONNECTION_TIMEOUT = 30000;
  public static final int PANEL_GET_TIMEOUT = 120000;
  public static final int RESOURCE_GET_TIMEOUT = 60000;
  /**
   * Prefix for logging so OpenRemote related entries can be easily filtered.
   */
  public final static String LOG_CATEGORY = "OpenRemote/";

  public static final String UTF8_ENCODING = "UTF-8";

  /**
   * Files are ordinarily created with mode Context.MODE_PRIVATE.  However, this
   * makes it next to impossible to read the files from real devices when debugging.
   * If Context.MODE_WORLD_READABLE is used instead, one can use adb pull to download
   * files written by the app.  For example, to download the panel.xml file, the
   * following command could be used:
   *
   * adb pull /data/data/org.openremote.android.console/files/panel.xml
   */
  public static final int DEFAULT_FILE_CREATION_MODE = Context.MODE_PRIVATE;
}
