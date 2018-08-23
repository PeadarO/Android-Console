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
 * It enumerates the statuses of wifi connection.
 * 
 * @author handy 2010-04-28
 *
 */
public enum ORWifiConnectionStatus {
	UNREACHABLE("UNREACHABLE"),
	REACHABLE_VIA_CARRIER_DATANETWORK("REACHABLE_VIA_CARRIER_DATANETWORK"),
	REACHABLE_VIA_WIFINETWORK("REACHABLE_VIA_WIFINETWORK");
	
	private String statusValue;
	
	private ORWifiConnectionStatus(String status) {
		this.statusValue = status.toUpperCase();
	}
	
	@Override
	public String toString() {
		return this.statusValue;
	}
	
	public boolean equals(ORWifiConnectionStatus wifiConnectionStatus) {
		if (wifiConnectionStatus == null || wifiConnectionStatus.toString() == null || this.statusValue == null) {
			return false;
		}
		return this.statusValue.equals(wifiConnectionStatus.toString());
	}
}
