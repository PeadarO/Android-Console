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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.openremote.java.console.controller.service.ControllerDiscoveryService;

import com.loopj.android.http.ResponseHandlerInterface;

/**
 * Controller auto discovery; this is a TCP server receiving responses from Controllers.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
class ControllerDiscoveryReceiver extends Thread {
  private final ResponseHandlerInterface responseHandler;
  private ServerSocket serverSocket;
  private boolean cancelled;
  private List<String> responses = new ArrayList<String>();
  
  public ControllerDiscoveryReceiver(ResponseHandlerInterface responseHandler, int tcpPort) throws IOException {
    this.responseHandler = responseHandler;
    serverSocket = new ServerSocket(tcpPort);
  }

  public void run() {
    // Start TCP server and wait for incoming connections
    try {
      while (!cancelled) {
        Socket socket = serverSocket.accept();
        String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
        if (!responses.contains(address)) {
          responses.add(address);
          new ControllerDiscoveryResponseHandler(responseHandler, socket).start();
        }
      }
    } catch (Exception e) {
    }
  }
    
  void cancel() {
    cancelled = true;
    this.interrupt();
  }
}
