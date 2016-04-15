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
package org.openremote.android.console.net;

import org.openremote.android.console.exceptions.ORConnectionException;

import android.content.Context;

/**
 * The Class ORRoundRobinConnection for switching the connection of android console to a available controller server in groupmembers of self.
 *
 * TODO deprecate this class!
 */
public class ORRoundRobinConnection extends ORConnection {

   public ORRoundRobinConnection(Context context, ORHttpMethod httpMethod, boolean isNeedHttpBasicAuth, String url,
         ORConnectionDelegate delegateParam) {
      super(httpMethod, isNeedHttpBasicAuth, url, delegateParam, context);
   }

   /** 
    * This method is invoked by self while the connection of android console to controller was failed,
    * and sends a notification to delegate with <b>urlConnectionDidFailWithException</b> method calling and
    * switching the connection of android console to a available controller server in groupmembers of self.
    */
   protected void connectionDidFailWithException(Context context, ORConnectionException e) {
      delegate.urlConnectionDidFailWithException(e);

      // TODO this should happen in the ControllerService implementation instead

       //ORControllerServerSwitcher.doSwitch();
   }
}
