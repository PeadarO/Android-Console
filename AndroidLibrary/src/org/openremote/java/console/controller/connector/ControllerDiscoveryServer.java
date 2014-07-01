/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2014, OpenRemote Inc.
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
package org.openremote.java.console.controller.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.loopj.android.http.ResponseHandlerInterface;

class ControllerDiscoveryServer extends Thread {
  private final ResponseHandlerInterface responseHandler;
  private static final String MULTICAST_ADDRESS = "224.0.1.100";
  private static final int MULTICAST_PORT = 3333;
  private boolean cancelled;
  private int tcpPort;
  private int elapsedTime;
  private Integer duration;
  private ControllerDiscoveryReceiver receiver;
  
  ControllerDiscoveryServer(int tcpPort, Integer duration, ResponseHandlerInterface responseHandler) {
    this.tcpPort = tcpPort;
    this.duration = duration;
    this.responseHandler = responseHandler;
  }
  
  public void run() {
    cancelled = false;
    
    // Try and start the receiver
    try {
      receiver = new ControllerDiscoveryReceiver(responseHandler, tcpPort);
      receiver.start();
    } catch (IOException e) {
      // Notify start failure 
      responseHandler.sendFailureMessage(0, null, null, e);
      return;
    } 
    
    // Notify start
    responseHandler.sendStartMessage();
    
    // Start the multicast   
    while(!cancelled) {
      try {        
        DatagramSocket socket = new DatagramSocket();
        byte[] b = new byte[512];
        DatagramPacket dgram;
        dgram = new DatagramPacket(b, b.length, InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
        socket.send(dgram);
      } catch (Exception e) {
      }
      
      elapsedTime += 1000;
      
      if (duration != null && elapsedTime > duration) {
        break;
      }
      
      // Sleep
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        cancelled = true;
      }
    }
    
    // Stop the receiver
    receiver.cancel();
    
    // Notify finish
    responseHandler.sendFinishMessage();
  }
  
  void cancel() {
    cancelled = true;
    this.interrupt();
  }
}
