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

import org.apache.http.HttpRequest;
import org.openremote.android.console.model.UserCache;
import org.openremote.android.console.util.base64.Base64Coder;

import android.content.Context;

/**
 * This util is about security.
 * 
 * @author handy 2010-04-29
 *
 */
public class SecurityUtil {
   
	/** Add HTTP Basic Authentication header with base64encoded username and password. */
	public static void addCredentialToHttpRequest(Context context, HttpRequest httpRequest) {
		String username = UserCache.getUsername(context);
		String password = UserCache.getPassword(context);
		
//		BASE64Encoder base64Encoder = new BASE64Encoder();		
//		String encodedUsernameAndPassword = base64Encoder.encode((username+":"+password).getBytes());
		String encodedUsernameAndPassword = Base64Coder.encodeString(username+":"+password);
		httpRequest.addHeader("Authorization", "Basic " + encodedUsernameAndPassword);
   }

}
