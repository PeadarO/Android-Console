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
import org.openremote.entitites.controller.ControllerResponseCode;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

/**
 * Android specific connector capable of calling callback on the UI thread
 * via the Android Looper Handler
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class AndroidHttpConnector extends HttpConnector {
  private final AsyncHttpClient client = new AsyncHttpClient();

  public AndroidHttpConnector() {
    client.addHeader("Accept", "application/json");
  }

  @Override
  protected void doRequest(String url, final ControllerCallback callback, Integer timeout) {
    if (callback.command == Command.LOGIN) {
      client.clearBasicAuth();
      if (credentials != null) {
        client.setBasicAuth(credentials.getUsername(), credentials.getPassword());
      }
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
}