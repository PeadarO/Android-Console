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
package org.openremote.console.controller.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class is the main manager of the discovery process.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * 
 */
public class ControllerDiscoveryServer extends Thread {

  private final ControllerDiscoveryResponseHandler responseHandler;
  private static final String MULTICAST_ADDRESS = "224.0.1.100";
  private static final int MULTICAST_PORT = 3333;
  private boolean cancelled;
  private int tcpPort;
  private int elapsedTime;
  private Integer duration;
  private int pauseTime = 1000;
  private ControllerDiscoveryReceiver receiver;

  public ControllerDiscoveryServer(int tcpPort, Integer duration, ControllerDiscoveryResponseHandler responseHandler) {
    this.tcpPort = tcpPort;
    this.duration = duration;
    this.responseHandler = responseHandler;
  }

  @Override
  public void run() {
    cancelled = false;

    // Try and start the receiver
    try {
      receiver = new ControllerDiscoveryReceiver(responseHandler, tcpPort);
      receiver.start();

      // Notify start
      responseHandler.sendStartMessage();

      // Start the multicast
      while (!cancelled) {
        // Send UDP packet at increasing time interval up to 60s
        if (elapsedTime % pauseTime == 0) {
          try {
            DatagramSocket socket = new DatagramSocket();
            byte[] b = new byte[512];
            DatagramPacket dgram;
            dgram = new DatagramPacket(b, b.length, InetAddress.getByName(MULTICAST_ADDRESS),
                    MULTICAST_PORT);
            socket.send(dgram);
            socket.close();
          } catch (Exception e) {
          }
          if (pauseTime < 60000) {
            pauseTime *= 2;
          }
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

      // Notify finish
      responseHandler.sendFinishMessage();

    } catch (IOException e) {
      // Notify start failure
      responseHandler.sendFailureMessage(e);
      return;
    } finally {
      if (receiver != null) {
        // Stop the receiver
        receiver.cancel();
      }
    }
  }

  public void cancel() {
    cancelled = true;
    this.interrupt();
  }
}
