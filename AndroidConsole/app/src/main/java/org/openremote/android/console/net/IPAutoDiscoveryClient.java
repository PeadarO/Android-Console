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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.openremote.android.console.Constants;

import android.util.Log;

/**
 * Controller IP auto discovery client, this is a Multicast UDP client broadcasting request to Controllers for asking
 * their IPs.
 *
 * @author Tomsky Wang
 *
 */
public class IPAutoDiscoveryClient implements Runnable {

  public static boolean isNetworkTypeWIFI;

 /**
  * To send a Multicast UDP client broadcasting request.
  *
  * @see java.lang.Runnable#run()
  */
  public void run() {
    try {
      DatagramSocket socket = new DatagramSocket();
      byte[] b = new byte[512];
      DatagramPacket dgram;
      dgram = new DatagramPacket(b, b.length, InetAddress.getByName(getMulticastAddress()), Constants.MULTICAST_PORT);
      socket.send(dgram);
      Log.i("OpenRemote-AUTO DISCOVER", "auto discovery on " + getMulticastAddress() + ":" + Constants.MULTICAST_PORT);
    } catch (Exception e) {
      Log.e("OpenRemote-AUTO DISCOVER", "auto discovery on " + getMulticastAddress() + ":" + Constants.MULTICAST_PORT
          + " failed", e);
    }
  }

  /**
   * Gets the multicast address by net type.
   *
   * @return the multicast address
   */
  public static String getMulticastAddress() {
    if (isNetworkTypeWIFI) {
      return Constants.MULTICAST_ADDRESS;
    }
    return Constants.NON_WIFI_MULTICAST_ADDRESS;
  }
}
