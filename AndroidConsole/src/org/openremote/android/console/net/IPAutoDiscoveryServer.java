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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.openremote.android.console.Constants;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Controller IP auto discovery server, this is a TCP server receiving IP from Controllers.
 *
 * @author Tomsky Wang
 *
 */
public class IPAutoDiscoveryServer extends AsyncTask<Void, Void, List<String>> {
  public static final String TAG = Constants.LOG_CATEGORY + "IPAutoDiscoveryServer";

  /** Interrupted the current discovery. */
  public static boolean isInterrupted;

  @Override
  protected List<String> doInBackground(Void... params) {
    publishProgress((Void) null);
    ArrayList<String> autoServers = new ArrayList<String>();
    ServerSocket srvr = null;

    try {
      srvr = new ServerSocket(Constants.LOCAL_SERVER_PORT);
      new IPAutoDiscoveryClient().run();
      srvr.setSoTimeout(Constants.LOCAL_DISCOVERY_SERVER_TIMEOUT);
    } catch (BindException e) {
      Log.e(TAG, "auto discovery server setup failed, the address is already in use");
      return autoServers;
    } catch (IOException e) {
      Log.e(TAG, "auto discovery server setup failed", e);
      return autoServers;
    }

    while (!isInterrupted) {
      try {
        Socket connectionSocket = srvr.accept();
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        String line = inFromClient.readLine();
        if (line != null && !"".equals(line)) {
          autoServers.add(line);
        }
        connectionSocket.close();
        Log.i(TAG, "auto discovery result: " + autoServers);
      } catch (SocketTimeoutException e) {
        Log.i(TAG, "SocketTimeoutException in doInBackground()");
        break;
      } catch (IOException e) {
        Log.i(TAG, "IOException in doInBackground: ", e);
        break;
      }
    }

    try {
      srvr.close();
    } catch (IOException e) {
      Log.e(TAG, "auto discovery ServerSocket close failed " , e);
    }

    return autoServers;
  }

}
