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
import java.net.Socket;

import com.loopj.android.http.ResponseHandlerInterface;

/**
 * Controller auto discovery; this is a Multicast UDP client broadcasting request to Controllers asking
 * for their credentials.
 * 
 * @authors Rich Turner
 * 
 */
public class ControllerDiscoveryResponseHandler extends Thread {
  private Socket socket = null;
  private ResponseHandlerInterface responseHandler;
  
  public ControllerDiscoveryResponseHandler(ResponseHandlerInterface responseHandler, Socket socket) {
    this.socket = socket;
    this.responseHandler = responseHandler;
  }
  
  public void run() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String inputLine;
      String response = "";
      while ((inputLine = in.readLine()) != null) {
        response += inputLine;
      }
      socket.close();
      
      if (!response.isEmpty()) {
        responseHandler.sendSuccessMessage(200, null, response.getBytes("UTF-8"));
      }
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}

