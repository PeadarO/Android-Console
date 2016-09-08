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
package org.openremote.console.controller.connector;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.auth.Credentials;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Android specific connector capable of calling callback on the UI thread via
 * the Android Looper Handler
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class AndroidHttpConnector extends HttpConnector {
  private final CustomAsyncHttpClient client = new CustomAsyncHttpClient();
  // private Timer heartBeatTimer;
  private AsyncControllerCallback<ControllerConnectionStatus> connectCallback;

  public AndroidHttpConnector() {
    client.addHeader("Accept", "application/json");
    client.setTimeout(getTimeout());
  }
  
  private void doDisconnect() {
    // Terminate any open polling connections
    client.cancelAllRequests(true);

    connected = false;
    
    if (connectCallback != null) {
      connectCallback.onFailure(ControllerResponseCode.DISCONNECTED);
      connectCallback = null;
    }
  }

  @Override
  protected void doRequest(URI uri, Map<String, String> headers, String content,
          final ControllerCallback callback, Integer timeout) {
    if (callback.command == RestCommand.DISCOVERY) {
      int tcpPort = (Integer) callback.data;
      client.startDiscovery(
          tcpPort,
          timeout,
          new ControllerDiscoveryResponseHandler() {
              @Override
              public void sendStartMessage() {
                  // Called when discovery is started
                  AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback.callback;
                  discoveryCallback.onDiscoveryStarted();
              }

              @Override
              public void sendFinishMessage() {
                  // This will be called when discovery is stopped
                  AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback.callback;
                  discoveryCallback.onDiscoveryStopped();
              }

              @Override
              public void sendSuccessMessage(byte[] responseBody) {
                  // Called each time a controller is discovered
                  handleResponse(callback, 0, null, responseBody);
              }

              @Override
              public void sendFailureMessage(Exception ex) {
                  // Called when discovery cannot be started
                  AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback.callback;
                  discoveryCallback.onStartDiscoveryFailed(ControllerResponseCode.UNKNOWN_ERROR);
              }
          }
      );
      return;
    }

    if (callback.command == RestCommand.STOP_DISCOVERY) {
      client.stopDiscovery();
      return;
    }

    if (callback.command == RestCommand.DISCONNECT) {
      doDisconnect();
      return;
    }

    boolean doHead = false;
    boolean doGet = false;

    if (callback.command == RestCommand.GET_RESOURCE_DETAILS) {
      doHead = true;
    }

    if (callback.command == RestCommand.GET_XML) {
      doGet = true;
    }

      ResponseHandlerInterface handler = new AsyncHttpResponseHandler() {

      @Override
      public void onCancel() {

      }

      @Override
      public void onSuccess(int code, Header[] headers, byte[] response) {
        if (callback.command == RestCommand.LOGOUT) {
          client.clearCredentialsProvider();
          return;
        }

        if (callback.command == RestCommand.CONNECT) {
          connectCallback = (AsyncControllerCallback<ControllerConnectionStatus>) callback.callback;
        }

        Map<String,String> headerMap = new HashMap<String, String>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }

        handleResponse(callback, code, headerMap, response);
      }

      @Override
      public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable exception) {
        if (callback.command == RestCommand.DO_SENSOR_POLLING
                && exception.getCause() instanceof ConnectTimeoutException) {
          callback.callback.onSuccess(null);
        } else {
          callback.callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
        }
      }
    };

    if (doHead) {
      client.setTimeout(timeout);
      try {
        client.head(uri.toURL().toString(), handler);
      } catch (MalformedURLException e) {
        callback.callback.onFailure(ControllerResponseCode.INVALID_URL);
      }
    } else if (doGet) {
        client.setTimeout(timeout);
        try {
            client.get(null, uri.toURL().toString(), prepareHeaders(headers), new RequestParams(), handler);
        } catch (MalformedURLException e) {
            callback.callback.onFailure(ControllerResponseCode.INVALID_URL);
        }
    } else {
      client.setTimeout(timeout);
      try {
        client.post(null, uri.toURL().toString(), prepareHeaders(headers), prepareEntity(content), "application/json", handler);
      } catch (MalformedURLException e) {
        callback.callback.onFailure(ControllerResponseCode.INVALID_URL);
      }
    }
  }

  @Override
  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;

    client.clearCredentialsProvider();

    if (credentials != null) {
      client.setBasicAuth(credentials.getUsername(), credentials.getPassword());
    }
  }

  @Override
  public void logout(AsyncControllerCallback<Boolean> callback) {
    credentials = null;

    if (controllerUrl != null) {
      doRequest(buildRequestUri(RestCommand.LOGOUT), null, null, new ControllerCallback(
              RestCommand.LOGOUT, callback), getTimeout());
    } else {
      callback.onSuccess(true);
    }
  }

  @Override
  public boolean isDiscoveryRunning() {
    return client.isDiscoveryRunning();
  }

    protected Header[] prepareHeaders(Map<String, String> headers) {
        Header[] headerArr = null;
        if (headers != null) {
            List<Header> headerList = new ArrayList<Header>();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headerList.add(new BasicHeader(entry.getKey(), entry.getValue()));
            }
            headerArr = headerList.toArray(new Header[headerList.size()]);
        }
        return headerArr;
    }

    protected StringEntity prepareEntity(String content) {
        return content != null
            ? new StringEntity(content, ContentType.APPLICATION_JSON)
            : null;
    }

}