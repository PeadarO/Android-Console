/*
 * OpenRemote, the Home of the Digital Home.
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

import org.apache.http.Header;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.java.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.java.console.controller.auth.Credentials;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

/**
 * Android specific connector capable of calling callback on the UI thread
 * via the Android Looper Handler
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class AndroidHttpConnector extends HttpConnector {
  private final CustomAsyncHttpClient client = new CustomAsyncHttpClient();

  public AndroidHttpConnector() {
    client.addHeader("Accept", "application/json");
  }

  @Override
  protected void doRequest(String url, final ControllerCallback callback, Integer timeout) {
    if (callback.command == Command.DISCOVERY) {
      ResponseHandlerInterface handler = new AsyncHttpResponseHandler() {
        @Override
        public void onStart() {
          // Called when discovery is started
          AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback)callback.callback; 
          
          discoveryCallback.onDiscoveryStarted();
        }
        
        @Override
        public void onFinish() {
          // This will be called when discovery is stopped
          AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback)callback.callback; 
          
          discoveryCallback.onDiscoveryStopped();
        }
        
        @Override
        public void onFailure(int code, Throwable exception, String message) {
          // Called when discovery cannot be started
          AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback)callback.callback; 
          
          discoveryCallback.onStartDiscoveryFailed(ControllerResponseCode.UNKNOWN_ERROR);
        }
        
        @Override
        public void onSuccess(int code, Header[] headers, byte[] response) {
          // Called each time a controller is discovered
          handleResponse(callback, code, headers, response);
        }
      };
      
      int tcpPort = (Integer) callback.data;
      client.startDiscovery(tcpPort, timeout, handler);
      return;
    }
    
    if (callback.command == Command.STOP_DISCOVERY) {
      client.stopDiscovery();
      return;
    }

    boolean doHead = false;

    if (callback.command == Command.GET_RESOURCE) {
      // Determine if we should load data if not do a head request
      Object[] data = (Object[]) callback.data;
      boolean loadData = (Boolean) data[2];
      if (!loadData) {
        doHead = true;
      }
    }

    ResponseHandlerInterface handler = new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(int code, Header[] headers, byte[] response) {
        if (callback.command == Command.LOGOUT) {
          client.clearBasicAuth();
          return;
        }

        handleResponse(callback, code, headers, response);
      }

      @Override
      public void onFailure(int code, Throwable exception, String message) {
        // TODO: Provide better information about failure
        callback.callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      }
    };
    
    if (doHead) {
      client.head(url, handler);
    } else {
      client.get(url, handler);
    }
  }
  
  @Override
  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
    
    client.clearBasicAuth();

    if (credentials != null) {
      client.setBasicAuth(credentials.getUsername(), credentials.getPassword());
    }
  }
  
  @Override
  public void logout(AsyncControllerCallback<Boolean> callback, int timeout) {
    credentials = null;
    
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(Command.LOGOUT), new ControllerCallback(Command.LOGOUT, callback),
              timeout);
    }
    else {
      callback.onSuccess(true);
    }      
  }

  @Override
  public boolean isDiscoveryRunning() {
    return client.isDiscoveryRunning();
  }
}