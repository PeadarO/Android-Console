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

import java.io.Serializable;

import org.openremote.android.console.Constants;

/**
 * Super class for all entities with the predefined strings.
 */
public class BusinessEntity implements Serializable {
   private static final long serialVersionUID = Constants.BINDING_VERSION;

   public static final String ID = "id";
   public static final String REF = "ref";

   public static final String SWITCH = "switch";
   public static final String ON = "on";
   public static final String OFF = "off";

   public static final String SENSOR = "sensor";
   public static final String LINK = "link";
   public static final String TYPE = "type";
   public static final String INCLUDE = "include";

   public static final String STATE = "state";
   public static final String NAME = "name";
   public static final String VALUE = "value";

   public static final String SLIDER = "slider";
   public static final String THUMB_IMAGE = "thumbImage";
   public static final String VERTICAL = "vertical";
   public static final String PASSIVE = "passive";
   public static final String MIN_VALUE = "min";
   public static final String MAX_VALUE = "max";
   public static final String IMAGE = "image";
   public static final String TRACK_IMAGE = "trackImage";

   public static final String LABEL = "label";
   public static final String FONT_SIZE = "fontSize";
   public static final String COLOR = "color";
   public static final String TEXT = "text";

   public static final String BUTTON = "button";
   public static final String DEFAULT = "default";
   public static final String PRESSED = "pressed";

   public static final String SRC = "src";
   public static final String STYLE = "style";
   
   public static final String NAVIGATE = "navigate";
   public static final String TABBAR_ITEM = "item";
   public static final String LANDSCAPE = "landscape";
   public static final String INVERSE_SCREEN_ID = "inverseScreenId";

   public static final String COLORPICKER = "colorpicker";
   
   public static final String BG_IMAGE_RELATIVE_POSITION_LEFT = "LEFT";
   public static final String BG_IMAGE_RELATIVE_POSITION_RIGHT = "RIGHT";
   public static final String BG_IMAGE_RELATIVE_POSITION_TOP = "TOP";
   public static final String BG_IMAGE_RELATIVE_POSITION_BOTTOM = "BOTTOM";
   public static final String BG_IMAGE_RELATIVE_POSITION_TOP_LEFT = "TOP_LEFT";
   public static final String BG_IMAGE_RELATIVE_POSITION_BOTTOM_LEFT = "BOTTOM_LEFT";
   public static final String BG_IMAGE_RELATIVE_POSITION_TOP_RIGHT = "TOP_RIGHT";
   public static final String BG_IMAGE_RELATIVE_POSITION_BOTTOM_RIGHT = "BOTTOM_RIGHT";
   public static final String BG_IMAGE_RELATIVE_POSITION_CENTER = "CENTER";

   public static final String WEB = "web";
   public static final String USERNAME = "username";
   public static final String PASSWORD = "password";
   public static final String IGNORE_SSL_ERRORS = "ignoreSslErrors";
   
   public static final String CONFIG = "config";
}
