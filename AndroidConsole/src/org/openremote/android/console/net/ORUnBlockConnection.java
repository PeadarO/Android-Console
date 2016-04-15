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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Connection class that creates a thread for its message handler.
 *
 * The only significant difference between this an its parent class appears to be that
 * a handler is created in a new thread, with a pseudo-random id.  Do we really
 * need such a class?
 *
 * TODO Determine whether this class is needed.  If so, name it something more easily parseable.
 */
public class ORUnBlockConnection extends ORConnection {

   public ORUnBlockConnection(final Context context, ORHttpMethod httpMethod, boolean isNeedHttpBasicAuth, String url,
         ORConnectionDelegate delegateParam) {
      super(httpMethod, isNeedHttpBasicAuth, url, delegateParam, context);
      
   }
   
   /**
    * Initialize handler by a random thread looper.
    * 
    * @see org.openremote.android.console.net.ORConnection#initHandler(android.content.Context)
    */
   protected void initHandler(final Context context) {
      HandlerThread handlerThread = new HandlerThread(Math.random()+"");  
      handlerThread.start();  
      handler = new Handler(handlerThread.getLooper()) {
         @Override
         public void handleMessage(Message msg) {
            int statusCode = msg.what;
            if (statusCode == 0) {
               connectionDidFailWithException(context, new ORConnectionException("Httpclient execute httprequest fail."));
            } else {
               dealWithResponse();
            }
         }
     };
   }
}
