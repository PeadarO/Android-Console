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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.auth.Credentials;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

/**
 * Android specific connector capable of calling callback on the UI thread via
 * the Android Looper Handler
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class AndroidHttpConnector extends HttpConnector {
  private static final int HEARTBEAT_PERIOD = 10000;
  private final CustomAsyncHttpClient client = new CustomAsyncHttpClient();
  // private Timer heartBeatTimer;
  private AsyncControllerCallback<ControllerConnectionStatus> connectCallback;

  public AndroidHttpConnector() {
    client.addHeader("Accept", "application/json");
    client.setTimeout(getTimeout());
  }

  @Override
  protected void doRequest(URI uri, Map<String, String> headers, String content,
          final ControllerCallback callback, Integer timeout) {
    if (callback.command == RestCommand.DISCOVERY) {
      ResponseHandlerInterface handler = new AsyncHttpResponseHandler() {
        @Override
        public void onStart() {
          // Called when discovery is started
          AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback.callback;

          discoveryCallback.onDiscoveryStarted();
        }

        @Override
        public void onFinish() {
          // This will be called when discovery is stopped
          AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback.callback;

          discoveryCallback.onDiscoveryStopped();
        }

        @Override
        public void onSuccess(int code, Header[] headers, byte[] response) {
          // Called each time a controller is discovered
          handleResponse(callback, code, headers, response);
        }

        @Override
        public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
          // Called when discovery cannot be started
          AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback.callback;

          discoveryCallback.onStartDiscoveryFailed(ControllerResponseCode.UNKNOWN_ERROR);
        }
      };

      int tcpPort = (Integer) callback.data;
      client.startDiscovery(tcpPort, timeout, handler);
      return;
    }

    if (callback.command == RestCommand.STOP_DISCOVERY) {
      client.stopDiscovery();
      return;
    }

    if (callback.command == RestCommand.DISCONNECT) {
      // if (heartBeatTimer != null) {
      // heartBeatTimer.cancel();
      // heartBeatTimer = null;
      // }

      // Terminate any open polling connections
      client.cancelAllRequests(true);

      if (connectCallback != null) {
        connectCallback.onFailure(ControllerResponseCode.DISCONNECTED);
        connectCallback = null;
      }

      return;
    }

    boolean doHead = false;

    if (callback.command == RestCommand.GET_RESOURCE_DETAILS) {
      doHead = true;
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

          // // Create AsyncHttpResponseHandler to pass messages back to UI
          // Thread
          // // this means we have no compile time dependency on Android
          // final AsyncHttpResponseHandler heartBeatCallback = new
          // AsyncHttpResponseHandler() {
          // @Override
          // public boolean getUseSynchronousMode() {
          // return true;
          // }
          //
          // @Override
          // public void onFailure(int arg0, Header[] arg1, byte[] arg2,
          // Throwable arg3) {
          // // Call connect callback onFailure with NO_RESPONSE
          // connectCallback.onFailure(ControllerResponseCode.NO_RESPONSE);
          // }
          //
          // @Override
          // public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
          // connectCallback.onSuccess(new
          // ControllerConnectionStatus(ControllerResponseCode.OK));
          // }
          // };
          //
          // // Start heartbeat task
          // final TimerTask task = new TimerTask() {
          //
          // @Override
          // public void run() {
          // getPanelList(new AsyncControllerCallback<List<PanelInfo>>() {
          // @Override
          // public void onSuccess(List<PanelInfo> result) {
          // if (!isConnected()) {
          // heartBeatCallback.sendSuccessMessage(200, null, null);
          // }
          // }
          //
          // @Override
          // public void onFailure(ControllerResponseCode error) {
          // // Cancel heartbeat if auto-reconnect is disabled
          // if (!isAutoReconnect()) {
          // heartBeatTimer.cancel();
          // }
          //
          // heartBeatCallback.sendFailureMessage(404, null, null, null);
          // }
          // });
          //
          // }
          // };
          //
          // heartBeatTimer = new Timer();
          // heartBeatTimer.schedule(task, HEARTBEAT_PERIOD, HEARTBEAT_PERIOD);
        }

        handleResponse(callback, code, headers, response);
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
    } else {
      StringEntity entity = null;
      Header[] headerArr = null;

      if (headers != null) {
        List<Header> headerList = new ArrayList<Header>();

        for (Entry<String, String> entry : headers.entrySet()) {
          headerList.add(new BasicHeader(entry.getKey(), entry.getValue()));
        }

        headerArr = headerList.toArray(new Header[0]);
      }

      if (content != null) {
        try {
          entity = new StringEntity(content);
          entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        } catch (UnsupportedEncodingException e) {
          callback.callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
          return;
        }
      }

      client.setTimeout(timeout);
      try {
        client.post(null, uri.toURL().toString(), headerArr, entity, "application/json", handler);
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
}