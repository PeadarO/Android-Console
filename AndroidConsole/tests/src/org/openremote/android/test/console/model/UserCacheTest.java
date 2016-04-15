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

import org.openremote.android.console.model.UserCache;

import android.content.Context;
import android.test.InstrumentationTestCase;

/**
 * Test for {@link org.openremote.android.console.model.UserCache} class.
 */
public class UserCacheTest extends InstrumentationTestCase {

   private Context ctx;

   public void setUp() {
     this.ctx = getInstrumentation().getContext();
   }
   
   /**
    * Reset user cache after each test.
    */
   public void tearDown() {
      UserCache.saveLastGroupIdAndScreenId(ctx, 0, 0);
      UserCache.saveUser(ctx, "", "");
   }
   
   /**
    * Test get empty group id and screen id, if there has not been set.
    */
   public void testGetEmptyGroupIdAndScreenId() {
      assertEquals(0, UserCache.getLastGroupId(ctx));
      assertEquals(0, UserCache.getLastScreenId(ctx));
   }
   
   /**
    * Test basic set/get on groupId and screenId.
    */
   public void testSaveLastGroupIdAndScreenId() {
      int groupId = 1;
      int screenId = 10;
      
      UserCache.saveLastGroupIdAndScreenId(ctx, groupId, screenId);
      
      assertEquals(groupId, UserCache.getLastGroupId(ctx));
      assertEquals(screenId, UserCache.getLastScreenId(ctx));
   }
   
   /**
    * Test get empty username and password, if there has not been set.
    */
   public void testGetEmptyUserAndPassword() {
      assertEquals("", UserCache.getUsername(ctx));
      assertEquals("", UserCache.getPassword(ctx));
   }
   
   /**
    * Test basic save/get username and password.
    */
   public void testSaveUserAndPassword() {
      String username = "openremote";
      String password = "123456";
      
      UserCache.saveUser(ctx, username, password);
      
      assertEquals(username, UserCache.getUsername(ctx));
      assertEquals(password, UserCache.getPassword(ctx));
   }
}
