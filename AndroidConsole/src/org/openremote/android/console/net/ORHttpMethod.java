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

/**
 * This is responsible for enumerating legal httpmethods which android console used in OpenRemote.
 * 
 * @author handy 2010-04-27
 *
 */
public enum ORHttpMethod {
	POST("POST"),
	GET("GET");
	
	private String httpMethod;	
	
	private ORHttpMethod(String httpMethodParam) {
		this.httpMethod = httpMethodParam.toUpperCase();
	}
	
	@Override
	public String toString() {
		return httpMethod.toUpperCase();
	}
	
	public boolean equals(ORHttpMethod httpMethodParam) {
		if (httpMethodParam == null || this.httpMethod == null || "".equals(this.httpMethod)) {
			return false;
		}
		return this.httpMethod.equals(httpMethodParam.toString());
	}
	
}
