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
package org.openremote.android.console.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openremote.android.console.Constants;
import org.openremote.android.console.bindings.Group;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.bindings.TabBar;
import org.openremote.android.console.exceptions.AppInitializationException;
import org.openremote.android.console.model.XMLEntityDataBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

/**
 * File I/O utility.
 * 
 * @author Tomsky Wang
 * @author Dan Cong
 *
 */
public class FileUtil {
   public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "FileUtil";

   /**
    * Checks whether a file exists by file name.
    * 
    * @param context
    *           Context instance
    * @param fileName
    *           file name
    * @return true if exists.
    */
   public static boolean checkFileExists(Context context, String fileName) {
      return context.getFileStreamPath(fileName).exists();
   }

   /**
    * Parses the panel from panel.xml.
    * 
    * @throws AppInitializationException if an XML parser could not be constructed
    * @throws SAXException if a parse error occurred
    * @throws IOException if could not read panel.xml file
    */
   public static void parsePanelXML(Context context) throws SAXException, IOException,
         AppInitializationException {
      parsePanelXMLInputStream(context.openFileInput(Constants.PANEL_XML));
   }
   
   public static void parsePanelXMLInputStream(InputStream fIn) throws SAXException, IOException,
         AppInitializationException {
      final String logPrefix = "parsePanelXMLInputStream(): ";

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document dom = builder.parse(fIn);
         Element root = dom.getDocumentElement();
         NodeList nodes = root.getChildNodes();
         int nodeLength = nodes.getLength();
         XMLEntityDataBase.globalTabBar = null;
         for (int i = 0; i < nodeLength; i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && "tabbar".equals(nodes.item(i).getNodeName())) {
               XMLEntityDataBase.globalTabBar = new TabBar(nodes.item(i));
            }
         }

         XMLEntityDataBase.screens.clear();
         NodeList screenNodes = root.getElementsByTagName("screen");
         int screenNum = screenNodes.getLength();
         for (int i = 0; i < screenNum; i++) {
            Screen screen = new Screen(screenNodes.item(i));
            XMLEntityDataBase.screens.put(screen.getScreenId(), screen);
         }

         XMLEntityDataBase.groups.clear();
         NodeList groupNodes = root.getElementsByTagName("group");
         int groupNum = groupNodes.getLength();
         for (int i = 0; i < groupNum; i++) {
            Group group = new Group(groupNodes.item(i));
            XMLEntityDataBase.groups.put(group.getGroupId(), group);
         }
      } catch (ParserConfigurationException e) {
         String message = "cannot construct an XML parser";
         Log.e(LOG_CATEGORY, logPrefix + message, e);
         throw new AppInitializationException(message, e);
      } catch (SAXException e) {
         Log.e(LOG_CATEGORY, logPrefix + "parse error while trying to parse panel", e);
         throw e;
      } catch (IOException e) {
         Log.e(LOG_CATEGORY, logPrefix + "IOException while trying to parse panel", e);
         throw e;
      } finally {
         try {
            fIn.close();
         } catch (IOException e) {
            Log.e(LOG_CATEGORY, logPrefix + "IOException while closing panel InputStream", e);
            throw e;
         }
      }
   }
   
   /**
    * Clear images from cache.
    * 
    */
   public static void clearImagesInCache(Context context) {
      String[] fileNames = context.fileList();
      for (int i = 0; i < fileNames.length; i++) {
         if (fileNames[i].toLowerCase().matches("^.+\\.(png|gif|jpg|bmp)$")) {
            Log.i("OpenRemote-CLEAR IMAGE", fileNames[i]);
            context.deleteFile(fileNames[i]);
         }
      }
   }
   
   /**
    * Opens a new file in the app's local storage space, writing the contents of an
    * InputStream to it and closing it.
    *
    * Change Constants.DEFAULT_FILE_CREATION_MODE if you need to pull files from
    * an actual device for debugging.
    *
    * @param context an Android Context, preferably the Application context
    * @param in the input stream to read from
    * @param filename name of the file to write
    *
    * @throws IOException
    */
   public static void writeStreamToFile(Context context, InputStream in, String filename)
         throws IOException {

      FileOutputStream fOut = context.openFileOutput(filename,
          Constants.DEFAULT_FILE_CREATION_MODE);
      byte buf[] = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
         fOut.write(buf, 0, len);
      }
      fOut.close();
   }
}
