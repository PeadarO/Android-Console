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
package org.openremote.android.test.console.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.openremote.android.console.model.PollingStatusParser;

import android.content.Context;
import android.test.InstrumentationTestCase;

/**
 * Test for polling result's parser.
 */
public class PollingStatusParserTest extends InstrumentationTestCase {

   /**
    * Test parsing fixture/polling_status.xml.
    */
   public void testParse() {
      Context ctx = getInstrumentation().getContext();
      
      try {
         InputStream is = ctx.getAssets().open("fixture/polling_status.xml");
         PollingStatusParser.parse(is);
         
         HashMap<String, String> statusMap = PollingStatusParser.statusMap;
         
         String[] keys = {"573", "574", "575", "576"};
         String[] values = {"on", "off", "20", "60"};
         
         for (int i = 0; i < 4; i ++) {
            assertEquals(values[i], statusMap.get(keys[i]));
         }
         
      } catch (IOException e) {
         fail("Failed reading fixture/polling_status.xml!");
      }
   }
}
