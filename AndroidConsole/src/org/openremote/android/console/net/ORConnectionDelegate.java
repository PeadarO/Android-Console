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

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.xml.sax.InputSource;

/**
 * This declares all callback methods which ORConnection would notify.
 * 
 * @author handy 2010-04-27
 *
 */
public interface ORConnectionDelegate {

	/** This callback method is called in ORConnection fail condition. */
	public void urlConnectionDidFailWithException(Exception e);

	/** This callback method is involked while ORConnection getting http response (not 200). */
	public void urlConnectionDidReceiveResponse(HttpResponse httpResponse);

	/** This callback method is called while ORConnection receiving data included in http response (200). */
	public void urlConnectionDidReceiveData(InputSource inStream);

}
