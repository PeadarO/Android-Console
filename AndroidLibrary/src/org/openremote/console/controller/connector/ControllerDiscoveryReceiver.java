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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller auto discovery; this is a TCP server receiving responses from
 * Controllers.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * 
 */
public class ControllerDiscoveryReceiver extends Thread {
  private final ControllerDiscoveryResponseHandler responseHandler;
  private ServerSocket serverSocket;
  private boolean cancelled;
  private List<String> responses = new ArrayList<String>();

  public ControllerDiscoveryReceiver(ControllerDiscoveryResponseHandler responseHandler, int tcpPort)
          throws IOException {
    this.responseHandler = responseHandler;
    serverSocket = new ServerSocket(tcpPort);
  }

  @Override
  public void run() {
    // Start TCP server and wait for incoming connections
    try {
      while (!cancelled) {
          final Socket socket = serverSocket.accept();
          new Thread() {
              @Override
              public void run() {
                  try {
                      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                      String inputLine;
                      String response = "";
                      while ((inputLine = in.readLine()) != null) {
                          response += inputLine;
                      }
                      socket.close();
                      processResponse(response);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          }.start();
      }
    } catch (Exception e) {
    }
  }

  public void cancel() {
    cancelled = true;
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    this.interrupt();
  }

  synchronized protected void processResponse(String response) {
    // Check response isn't empty or hasn't already been received
    if (!response.isEmpty() && !responses.contains(response)) {
      responses.add(response);
      try {
        responseHandler.sendSuccessMessage(response.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
