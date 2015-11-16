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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.java.console.controller.Controller;
import org.openremote.java.console.controller.auth.Credentials;

/**
 * Test controller connector that does things synchronously for ease of testing
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class SingleThreadHttpConnector extends HttpConnector {
  private final RequestConfig config = RequestConfig.custom()
          .setSocketTimeout(Controller.DEFAULT_TIMEOUT)
          .setConnectionRequestTimeout(Controller.DEFAULT_TIMEOUT)
          .setConnectTimeout(Controller.DEFAULT_TIMEOUT).build();
  private final CredentialsProvider creds = new BasicCredentialsProvider();
  private final HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(creds)
          .setDefaultRequestConfig(config).build();

  @Override
  protected void doRequest(String url, final ControllerCallback callback, Integer timeout) {
    boolean doHead = false;

    if (callback.command == Command.GET_RESOURCE_DETAILS) {
      // Determine if we should load data if not do a head request
      Object[] data = (Object[]) callback.data;
      boolean loadData = (Boolean) data[2];
      if (!loadData) {
        doHead = true;
      }
    }

    HttpUriRequest http;

    if (doHead) {
      HttpHead httpHead = new HttpHead(url);
      httpHead.setConfig(RequestConfig.custom().setSocketTimeout(timeout)
              .setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).build());
      http = httpHead;
    } else {
      HttpGet httpGet = new HttpGet(url);
      httpGet.addHeader("Accept", "application/json");
      httpGet.setConfig(RequestConfig.custom().setSocketTimeout(timeout)
              .setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).build());
      http = httpGet;
    }

    try {
      HttpResponse response = client.execute(http);
      byte[] responseData = null;

      if (response.getEntity() != null) {
        InputStream is = response.getEntity().getContent();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = is.read(buffer, 0, buffer.length)) != -1) {
          baos.write(buffer, 0, read);
        }
        baos.flush();
        is.close();
        responseData = baos.toByteArray();
      }

      // java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
      // String responseStr = s.hasNext() ? s.next() : "";

      if (callback.command == Command.LOGOUT) {
        creds.clear();
      }

      handleResponse(callback, response.getStatusLine().getStatusCode(), response.getAllHeaders(),
              responseData);
    } catch (Exception e) {
      if (callback.command == Command.DO_SENSOR_POLLING && e instanceof SocketTimeoutException) {
        callback.callback.onSuccess(null);
        return;
      }
      callback.callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
    }
  }

  @Override
  public void setCredentials(Credentials credentials) {
    creds.clear();
    if (credentials != null) {
      creds.setCredentials(new AuthScope("localhost", 8080), new UsernamePasswordCredentials(
              credentials.getUsername(), credentials.getPassword()));
    }
  }

  @Override
  public void logout(AsyncControllerCallback<Boolean> callback, int timeout) {
    creds.clear();
  }

  @Override
  public boolean isDiscoveryRunning() {
    // TODO Auto-generated method stub
    return false;
  }
}